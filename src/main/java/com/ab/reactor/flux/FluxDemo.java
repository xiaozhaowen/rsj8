package com.ab.reactor.flux;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xiaozhao
 * @date 2019/4/42:11 PM
 */
public class FluxDemo {

    /**
     * 订阅消费
     */
    private void consumeEveryOne() {
        Flux<Integer> integerFlux = Flux.range(1, 5);

        // 依次输出每个元素
        integerFlux.subscribe(i -> System.out.println(i));
    }


    /**
     * 错误处理
     */
    private void errorHandle() {
        Flux<Integer> integerFlux = Flux.range(1, 4)
                .map(i -> {
                    if (i == 2) {
                        throw new RuntimeException("go 2");
                    }
                    return i;
                });

        integerFlux.subscribe(i -> System.out.println(i),
                error -> System.out.println("Error" + error));
    }


    /**
     * 整个流结束时候，做一个业务处理，如果出错，那么这个流是没有完成的
     */
    private void pipeLineDone() {
        Flux<Integer> integerFlux = Flux.range(1, 4);

        integerFlux.subscribe(i -> System.out.println(i),
                error -> System.out.println("Error" + error),
                () -> System.out.println("All Done"));
    }


    /**
     * 利用订阅句柄来明确请求多少个消息或者取消接收
     */
    private void subscribeHandler() {
        Flux<Integer> ints = Flux.range(1, 40);
        ints.subscribe(i -> System.out.println(i),
                error -> System.err.println("Error " + error),
                () -> System.out.println("Done"),
                sub -> sub.request(10));
    }


    /**
     * 使用一个完整的类来替代lambda，适合定制请求，提供了很多钩子函数
     */
    private void lambdaAlternative() {
        SampleSubscriber<Integer> ss = new SampleSubscriber<>();
        Flux<Integer> ints = Flux.range(1, 4).map(i -> {
            if (i == 2) {
                throw new RuntimeException("ERROR");
            }
            return i;
        });

        // lambda形式
      /*  ints.subscribe(i -> System.out.println(i),
                error -> System.err.println("Error " + error),
                () -> {
                    System.out.println("Done");
                },
                s -> s.request(10));*/


//        System.out.println();

        // 类的形式
        ints.subscribe(ss);
    }


    /**
     * 在收到一个元素后，取消订阅
     */
    private void cancelSubscribe() {
        Flux.range(1, 10)
                .doOnRequest(r -> System.out.println("request of " + r))
                .subscribe(new BaseSubscriber<Integer>() {

                    @Override
                    public void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @Override
                    public void hookOnNext(Integer integer) {
                        System.out.println("Cancelling after having received " + integer);
                        cancel();
                    }
                });
    }


    //----------------------------------程序方式同步创建 BEGIN------------------------------------

    /**
     * 编程方式生成序列
     */
    private void generate_1() {

        /**
         * 第一个参数用于保存状态。2个作用：初始化和每个步骤的结果存储。初始值为0，然后在每次操作后供下一次使用
         * 第二个参数是具体的生成逻辑，使用next，error，complete 3个方法来生成
         */
        Flux<String> flux = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3 * state);
                    if (state == 10) {
                        sink.complete();
                    }
                    return state + 1;
                });
        flux.subscribe(System.out::println);
    }

    /**
     * 编程方式生成序列
     */
    private void generate_2() {

        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) {
                        sink.complete();
                    }
                    return state;
                });
        flux.subscribe(System.out::println);
    }

    /**
     * 编程方式生成序列
     * 当state需要一些清理工作的时候使用这种方式，例如关闭数据库连接
     */
    private void generate_3() {

        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) {
                        sink.complete();
                    }
                    return state;
                }, (state) -> System.out.println("state:的清理工作 " + state));
        flux.subscribe(System.out::println);
    }
    //----------------------------------程序方式同步创建 END------------------------------------


    //----------------------------------程序方式异步创建 BEGIN------------------------------------

    /**
     * 异步创建和多线程
     * 还可以指定背压的策略
     */
    private void createTest() {
        Flux.create((FluxSink<Integer> sink) -> {
            for (int i = 0; i < 10; i++) {
                sink.next(i);
            }
            sink.complete();
        }).subscribe(System.out::println);
    }


    //----------------------------------程序方式异步创建 END------------------------------------


    /**
     * 线程测试
     */
    private void threadTest() {
        final Mono<String> mono = Mono.just("hello ");

        Thread thread = new Thread(() -> mono
                .map(msg -> msg + "thread ")
                .subscribe(v ->
                        System.out.println(v + Thread.currentThread().getName())
                )
        );

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread().getName());
    }


    private void publishOnTest() {
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux
                .range(1, 2)
                .map(i -> 10 + i)
                .publishOn(s)
                .map(i -> "value " + i);

        new Thread(() -> flux.subscribe(System.out::println)).start();
    }


    private void intervalTest() {
        Flux<Long> flux = Flux.interval(Duration.ofMillis(100));

        flux.subscribe(t -> {
            System.out.println(t);
        });
    }

    private void multiPublisher() {
        Flux<String> flux = Flux.interval(Duration.ofMillis(100)).map(t -> "1_" + t);
        Flux<String> flux2 = Flux.interval(Duration.ofMillis(100)).map(t -> "2_" + t);


        BaseSubscriber<String> stringBaseSubscriber = new BaseSubscriber<String>() {
            @Override
            protected void hookOnNext(String value) {
                System.out.println(value);
            }
        };

        flux.subscribe(stringBaseSubscriber);
        flux2.subscribe(stringBaseSubscriber);


    }


    public static void main(String[] args) {
        FluxDemo demo = new FluxDemo();
        demo.multiPublisher();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


