package com.ab.reactor.flux;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程相关的例子
 *
 * @author xiaozhao
 * @date 2019/4/512:10 AM
 */
public class ThreadingAndScheduler {


    /**------------------------------默认单线程----------------------------------------------------------------------------------*/

    /**
     * Reactor默认是单线程的，运行在调用线程上
     * 可以看到从生产者、操作符、钩子、消费者都是一个线程
     */
    private void runInTheCallerThread() {
        // 生产者
        Flux<Long> fibonacciGenerator = generatorFibonacci();

        // 操作符、钩子、消费者
        fibonacciGenerator.filter(x -> {
            print("执行过滤:" + x);
            return x < 100;
        })
                .doOnNext(x -> print("Next钩子函数:" + x))
                .doFinally(x -> print("Finally钩子函数"))
                .subscribe(x -> print("订阅者收到：" + x));
    }

    /**
     * 多个订阅者也是同一个线程
     */
    private void multiConsumerStillSingleThread() {
        // 生产者
        Flux<Long> fibonacciGenerator = generatorFibonacci();

        // 1号--操作符、钩子、消费者
        fibonacciGenerator.filter(x -> {
            print("执行过滤:" + x);
            return x < 100;
        })
                .doOnNext(x -> print("Next钩子函数:" + x))
                .doFinally(x -> print("Finally钩子函数"))
                .subscribe(x -> print("订阅者收到：" + x));

        // 2号--操作符、钩子、消费者
        fibonacciGenerator.filter(x -> {
            print("【2】执行过滤:" + x);
            return x < 100;
        })
                .doOnNext(x -> print("【2】Next钩子函数:" + x))
                .doFinally(x -> print("【2】Finally钩子函数"))
                .subscribe(x -> print("【2】订阅者收到：" + x));
    }

    /**
     * 在一个独立线程中运行，生产、操作符、钩子、消费者都是一个线程
     */
    private void sonThread() {
        String threadName = "TestSubThread";
        Thread thread = new Thread(() -> runInTheCallerThread(), threadName);
        thread.start();
    }

    /**
     * 子流水线的线程默认就是父流水线的线程
     */
    private void sonFlux() {
        Flux.just(1, 3, 5, 7, 9)
                .map(x -> {
                    print("map处理：" + x);
                    Flux.just("Hello")
                            .map(word -> {
                                print("【子Flux】的Map");
                                return word + x;
                            })
                            .subscribe(word -> print("【子消费】" + word));
                    return x + 1;
                })
                .doOnNext(x -> print("Next钩子函数:" + x))
                .doFinally(x -> print("Finally钩子函数"))
                .subscribe(x -> print("订阅者收到：" + x));
    }


    /**-------------------------------Schedulers----------------------------------------------------------------------------------*/


    /**
     * delayElements操作符可以开启新的线程，也可以不开启，取决于Scheduler策略
     * <p>
     * 1）生产者仍然在主线程运行
     * 2）过滤操作符也在主线程运行（在【delayElements】操作符之前）
     * 3）delayElements操作符会开启新的线程池，数量为当前电脑的CPU核数
     * 4）delayElements之后的环节都在新的线程池中执行
     * 5)TODO 由于 delayElements(Duration.ofMillis(200))的不同设置，导致doFinally可能在主线程也可能在子线程中
     * delayElements(Duration.ofMillis(200))  子线程
     * delayElements(Duration.ZERO) 主线程
     */
    private void delayOperatorCreateNewThread() {
        Flux<Long> fibonacciGenerator = generatorFibonacci();
        fibonacciGenerator.filter(x -> {
            print("执行过滤:" + x);
            return x < 100;
        })
                .delayElements(Duration.ofMillis(200))
                .doOnNext(x -> print("Next钩子函数:" + x))
                .doFinally(x -> print("Finally钩子函数"))
                .subscribe(x -> print("订阅者收到：" + x));

        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //------------------------------Schedulers.immediate---------------------

    /**
     * 在调用者的线程上运行
     * TODO 例子不能运行
     */
    private void runOnCurrentThread() {

        Thread thread = new Thread(() -> {
            Flux<Long> fibonacciGenerator = generatorFibonacci();
            fibonacciGenerator.filter(x -> {
                print("执行过滤:" + x);
                return x < 100;
            })
                    .delayElements(Duration.ofNanos(10), Schedulers.immediate())
                    .doOnNext(x -> print("Next钩子函数:" + x))
                    .doFinally(x -> print("Finally钩子函数"))
                    .subscribe(x -> print("订阅者收到：" + x));
        }, "TestSubThread");
        thread.start();
    }


    //------------------------------Schedulers.single-------------------------

    /**
     * 线程池中只有一个线程，这个线程可以复用
     */
    private void runOnSingleReuseThread() {
        Flux<Long> fibonacciGenerator = generatorFibonacci();
        fibonacciGenerator.filter(x -> {
            print("执行过滤:" + x);
            return x < 100;
        })
                .delayElements(Duration.ofNanos(10), Schedulers.single())
                .doOnNext(x -> print("Next钩子函数:" + x))
                .doFinally(x -> print("Finally钩子函数"))
                .subscribe(x -> print("订阅者收到：" + x));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Schedulers.single() 不能使用阻塞的Reactor相关API，如果调用则会抛出异常
     */
    private void singleSchedulerBlockWillGetException() {
        Flux<Long> fibonacciGenerator = generatorFibonacci();
        fibonacciGenerator.filter(x -> {
            print("执行过滤:" + x);
            return x < 100;
        })
                .delayElements(Duration.ofNanos(10), Schedulers.single())
                .window(10)
                .doOnNext(x -> print("Next钩子函数:" + x))
                .doFinally(x -> print("Finally钩子函数"))
                .subscribe(x -> print("订阅者收到：" + x.blockFirst()));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void singeThreadPool() {


        Flux<Integer> flux = Flux.range(1, 10)
                .map(x -> {
                    print("map1:" + x);
                    return x;
                });

        Scheduler scheduler = Schedulers.newSingle("AA");
        flux.publishOn(scheduler)
                .map(x -> {
                    print("map2:" + x);
                    return x;
                })
                .subscribe(x -> print("消费1:" + x));

        flux.publishOn(scheduler)
                .map(x -> {
                    print("map2_2:" + x);
                    return x;
                })
                .subscribe(x -> print("消费2:" + x));

    }


    //------------------------------Schedulers.parallel-----------------------

    /**
     * 会创建一个线程池，池中的线程数就是CPU的核数
     */
    private void parallel() {
        Flux<Long> fibonacciGenerator = generatorFibonacci();
        fibonacciGenerator.filter(x -> {
            print("执行过滤:" + x);
            return x < 100;
        })
                .delayElements(Duration.ofNanos(10), Schedulers.parallel())
                .doOnNext(x -> print("Next钩子函数:" + x))
                .doFinally(x -> print("Finally钩子函数"))
                .subscribe(x -> print("订阅者收到：" + x));
    }

    //------------------------------Schedulers.elastic------------------------

    /**
     * 可以调用阻塞API，线程池动态调整。当线程池中没有可用线程时创建新的线程，当线程闲置过久时销毁闲置的线程。
     * 适用于IO密集型
     */
    private void elasticScheduler() {
        Flux<Long> fibonacciGenerator = generatorFibonacci();
        fibonacciGenerator.filter(x -> {
            print("执行过滤:" + x);
            return x < 100;
        })
                .delayElements(Duration.ofNanos(10), Schedulers.elastic())
                .window(10)
                .doOnNext(x -> print("Next钩子函数:" + x))
                .doFinally(x -> print("Finally钩子函数"))
                .subscribe(x -> print("订阅者收到：" + x.blockFirst()));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //------------------------------Schedulers.fromExecutor--------------------

    /**
     * 从Java的线程池来创建，不建议使用。应该优先使用其他的几个Scheduler：single、parallel、elastic等
     */
    private void executorScheduler() {
        Flux<Long> fibonacciGenerator = generatorFibonacci();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        fibonacciGenerator.filter(x -> {
            print("执行过滤:" + x);
            return x < 100;
        })
                .delayElements(Duration.ofNanos(10), Schedulers.fromExecutor(executorService))
                .doOnNext(x -> print("Next钩子函数:" + x))
                .doFinally(x -> print("Finally钩子函数"))
                .subscribe(x -> print("订阅者收到：" + x));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        print("Is shutdown? " + executorService.isShutdown());
    }


    /**------------------------------并行----------------------------------------------------------------------------------------------*/

    //------------------------------publishOn----------------------------------

    /**
     * 流水线默认运行在启动线程上，如果调用publishOn了，之后的环节就都在新线程上运行
     */
    private void publishOnSimple() {
        Flux.just("tom").map(s -> {
            print("切换前的Map");
            return s.toUpperCase();
        }).publishOn(Schedulers.newSingle("son-thread", true))
                .map(s -> {
                    print("切换后的Map");
                    return s;
                })
                .filter(s -> {
                    print("切换后的Filter");
                    return true;
                })
                .subscribe(x -> print("消费方" + x));
    }


    /**
     * 从publishOn开始直到流水线终点，包括操作符、钩子、订阅者都是在新的线程上执行
     */
    private void publishOnSimple2() {
        Flux<Long> fibonacciGenerator = generatorFibonacci();
        fibonacciGenerator.publishOn(Schedulers.single())
                .filter(x -> {
                    print("执行过滤:" + x);
                    return x < 100;
                })
                .doOnNext(x -> print("Next钩子函数:" + x))
                .doFinally(x -> print("Finally钩子函数"))
                .subscribe(x -> print("订阅者收到：" + x));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void publishOnSimple3() {
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux
                .range(1, 2)
                .map(i -> {
                    print("map1--" + i);
                    return 10 + i;
                })
                .publishOn(s)
                .map(i -> {
                    print("map2--" + i);
                    return "value " + i;
                });

        new Thread(() -> flux.subscribe(x -> print("消费者:" + x)), "SON").start();
    }


    //------------------------------subscribeOn---------------------------------

    /**
     * subscribeOn 是从源头开始生效，而是由subscribeOn指定的线程来运行
     */
    private void subscribeOnSimple() {
        // 创建一个线程池
        Scheduler s = Schedulers.parallel();

        final Flux<String> flux = Flux
                .range(1, 20)
                .map(i -> {
                    print("map1:" + i);
                    return i;
                })
                .subscribeOn(s)
                .map(i -> {
                    print("map2:" + i);
                    return "value " + i;
                });

        Thread thread = new Thread(() -> {
            flux.subscribe(x -> print("订阅者：" + x));
        }, "SonThread");

        // 流水线并没有运行在thread 这个线程上，而是取决于subscribeOn指定的线程
        thread.start();


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 最简单的subscribeOn
     */
    private void subscribeOnSimple2() {
        Flux.just("tom")
                .map(s -> {
                    print("map:" + s);
                    return s;
                })
                .filter(s -> {
                    print("filter:" + s);
                    return true;
                })
                .subscribeOn(Schedulers.single())
                .subscribe(x -> print("订阅者：" + x));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从源头开始影响，包括生产者
     */
    private void subscribeOnSimple3() {
        Flux<Long> fibonacciGenerator = generatorFibonacci();
        fibonacciGenerator
                .filter(x -> {
                    print("执行过滤:" + x);
                    return x < 100;
                })
                .doOnNext(x -> print("Next钩子函数:" + x))
                .doFinally(x -> print("Finally钩子函数"))
                .subscribeOn(Schedulers.single())
                .subscribe(x -> print("订阅者收到：" + x));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果出现多次subscribeOn，则只有首个subscribeOn有效
     */
    private void subscribeOnOnlyTheFirstWork() {
        Flux.just("tom")
                .map(s -> {
                    print("map" + s);
                    return s.toUpperCase();
                })
                .subscribeOn(Schedulers.newSingle("source4"))
                .subscribeOn(Schedulers.elastic())
                .subscribeOn(Schedulers.parallel())
                .subscribeOn(Schedulers.single())
                .subscribe(x -> print("订阅者收到：" + x));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * publishOn 会切换到其他的线程，之后的流程也切换出去了。
     */
    private void publishOnSubscribeOn() {
        Flux.just("tom")
                .map(s -> {
                    print("map1:" + s);
                    return s.concat("@qq.com");
                })
                .publishOn(Schedulers.newSingle("thread-a"))
                .map(s -> {
                    print("map2 在publishOn--1后：" + s);
                    return s.concat("foo");
                })
                .filter(s -> {
                    print("filter 在publishOn--1后：" + s);
                    return s.startsWith("t");
                })
                .publishOn(Schedulers.newSingle("thread-b"))
                .map(s -> {
                    print("map3 在publishOn---2 后：" + s);
                    return s.length();
                })
                .subscribeOn(Schedulers.newSingle("source"))
                .doOnComplete(() -> print("Complete钩子"))
                .doFinally(s -> print("Finally钩子"))
                .subscribe(x -> print("订阅者收到：" + x));

    }

    //------------------------------parallelFlux---------------------------------

    /**
     * 并行处理测试
     */
    private void parallelTest() {
        Flux<Long> fibonacciGenerator = generatorFibonacci();
        fibonacciGenerator
                .parallel()
                .runOn(Schedulers.parallel())
                .filter(x -> {
                    print("执行过滤:" + x);
                    return x < 100;
                })
                .doOnNext(x -> print("Next钩子函数:" + x))
                .subscribe(x -> print("订阅者收到：" + x));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**------------------------------公共方法----------------------------------------------------------------------------------------------*/

    /**
     * 构建一个斐波那契数列
     */
    private Flux<Long> generatorFibonacci() {
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.of(0L, 1L),
                (state, sink) -> {
                    if (state.getT1() < 0) {
                        sink.complete();
                    } else {
                        sink.next(state.getT1());
                    }
                    print("生产数据：" + state.getT2());
                    return Tuples.of(state.getT2(), state.getT2() + state.getT1());
                });
        return fibonacciGenerator;
    }

    private static void print(String text) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + text);
    }

    /**
     * 订阅形式的不同会导致多线程行为不同
     */
    private void validate() {
        Flux.range(1, 100)
                .subscribeOn(Schedulers.parallel())
                .parallel()
                .runOn(Schedulers.parallel())
//                .map(x -> {
//                    print("map");
//                    return x;
//                })
                .subscribe(x -> {
                    print("订阅者" + x);
                });
//                .subscribe(new Subscriber<Integer>() {
//                    @Override
//                    public void onSubscribe(Subscription s) {
//                        s.request(100);
//                    }
//
//                    @Override
//                    public void onNext(Integer integer) {
//                        print("订阅者");
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//                    }
//
//                    @Override
//                    public void onComplete() {
//                    }
//                });
    }


    /**
     * 如果父Flux的某个操作符中使用了一个子Flux.  只要子Flux不开启新线程的话，
     */
    private void sonFluxInParentMap() {
        Flux.range(1, 100)
                .parallel()
                .runOn(Schedulers.parallel())
                .map(x -> {
                    print("父map：" + x);
                    Flux.just(x)
//                            .publishOn(Schedulers.parallel())
                            .map(item -> {
                                print("子map----------------:" + item);
                                return item;
                            })
                            .subscribe();
                    print("父map2222222：" + x);
                    return x;
                })
                .subscribe();
    }


    private void fluxInFlux() {
        Flux.just("Hello")
                .map(x -> {
                    Flux.range(1, 16)
                            .doOnNext(tt -> {
                            })
                            .parallel()
                            .runOn(Schedulers.parallel())
                            .map(s -> {
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                print("map2 内部数据:" + s);
                                return s;
                            })
                            .sequential()
                            .blockLast();

                    print("外部1--" + x);
                    return x;
                })
                .map(x -> {
                    print("外部map2--" + x);
                    return x;
                })
                .subscribe(t -> {
                }, e -> {
                    System.out.println(e);
                });
    }


    private void rightBlock() {

        DirectProcessor<String> processor = DirectProcessor.create();


        Flux.just("ABC").map(x -> {
            String result = "加解密--" + x;
            print(result);
            return result;
        })
                .map(x -> {
                    Scheduler scheduler = Schedulers.parallel();
                    Flux.range(1, 25)
                            .subscribeOn(scheduler)
                            .parallel()
                            .runOn(scheduler)
                            .map(t -> {
                                print("转换--" + t);
                                return t;
                            })
                            .sequential()
                            .doFinally(t -> {
                                print("执行结束");
                                processor.onNext("传递给SPI");
                                processor.onComplete();
                            })
                            .subscribe();
                    return x;
                })
                .
                        subscribe(x -> {

                        });


        processor.subscribe(x -> {
            print("SPI层--" + x);
        });

    }









    public static void main(String[] args) {

        ThreadingAndScheduler demo = new ThreadingAndScheduler();
        demo.rightBlock();
        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
