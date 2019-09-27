package com.ab.reactor.mono;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


/**
 * @author xiaozhao
 * @date 2019/4/42:12 PM
 */
public class MonoDemo {

    private static final Logger log = LoggerFactory.getLogger(MonoDemo.class);


    private static final ExecutorService es = Executors.newSingleThreadExecutor(
            new DefaultThreadFactory("outer"));

    private static final ExecutorService es2 = Executors.newSingleThreadExecutor(
            new DefaultThreadFactory("inner"));


    /**
     * 线程退出
     */
    private void threadExit() {


        es.execute(() -> {
            doWork("第1任务", 200);
        });


        es.execute(() -> {
            doWork("第2任务", 3000);
        });


        es.execute(() -> {
            doWork("第3任务", 300);

        });
        es.execute(() -> {
            doWork("第4任务", 400);
        });
        es.execute(() -> {
            doWork("第5任务", 2700);
        });
        es.execute(() -> {
            doWork("第6任务", 100);
        });

    }

    private void doWork(String name, int time) {
        System.out.println();
        AtomicReference<Thread> thread = new AtomicReference<>();
        Flux.just(name)
                .map(x -> {
                    log.info("任务开始:{}", x);

                    Future<String> stringFuture = getName(time);
                    thread.set(Thread.currentThread());
                    log.info("Map:{}", x);
                    try {
                        String son = stringFuture.get();
                        log.info("返回值是:{}", son);
                    } catch (InterruptedException e) {
                        log.info("map异常：线程被打断");
                        stringFuture.cancel(true);
                    } catch (ExecutionException e) {
                        log.info("map异常ExecutionException");
                    }

                    return x;
                })
                .timeout(Duration.ofMillis(1000))
                .doOnError(throwable -> {
                    log.info("我是doOnError");
                    if (throwable instanceof TimeoutException) {
                        log.info("线程名称:" + thread.get().getName());
                        thread.get().interrupt();
                    }
                })
                .subscribe(x -> {
                    log.info("订阅者消费:{}", x);
                }, error -> {
                    log.info("订阅者错误:");
                });
    }


    private Future<String> getName(int time) {
        return es2.submit(() -> {
            Thread.sleep(time + 500);
            log.info("其他组件的线程返回值");
            return "Curry";
        });
    }


    public static void main(String[] args) {
        MonoDemo demo = new MonoDemo();
        demo.threadExit();

//        AtomicReference<Thread> thread = new AtomicReference<>();
//        Flux.just(1000, 5000, 1500)
//                .doOnNext(integer -> System.out.println("begin " + integer + "ms!"))
//                .map(integer -> {
//                    try {
//                        thread.set(Thread.currentThread());
//                        Thread.sleep(integer);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    return integer;
//                })
//                .timeout(Duration.ofSeconds(2))
//                .doOnError(throwable -> Optional.of(throwable).filter(TimeoutException.class::isInstance).map(TimeoutException.class::cast).ifPresent(e -> thread.get().interrupt()))
//                .retry(2)
//                .doOnError(Throwable::printStackTrace)
//                .subscribe(integer -> System.out.println("end " + integer + "ms!"));

    }

    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory(String prefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = prefix + "-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
