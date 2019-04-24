package com.ab.reactor.flux;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;

/**
 * @author xiaozhao
 * @date 2019/4/128:42 AM
 */
public class ErrorHandle {

    public static void main(String[] args) {
        ErrorHandle errorHandle = new ErrorHandle();
        errorHandle.retryTest2();
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
     * 生成斐波那契数列
     *
     * @return
     */
    private Flux<Long> generatorFibonacci() {
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.<Long, Long>of(0L, 1L),
                (state, sink) -> {
                    if (state.getT1() < 0) {
                        sink.error(new RuntimeException("数据值超越了边界"));
                    } else {
                        sink.next(state.getT1());
                    }
                    return Tuples.of(state.getT2(), state.getT2() + state.getT1());
                });
        return fibonacciGenerator;
    }


    /**
     * 捕获明确类型的异常，注意Exception的声明顺序
     * 特定异常类型在前，普通在后。也就是说子类在前，父类在后
     */
    private void onErrorReturnTestCaptureExplicitException() {
        Flux<Long> flux = Flux.generate(() -> Tuples.<Long, Long>of(0L, 1L),
                (state, sink) -> {
                    if (state.getT1() < 0) {
                        sink.error(new IllegalStateException("数据值超越了边界"));
                    } else {
                        sink.next(state.getT1());
                    }
                    return Tuples.of(state.getT2(), state.getT2() + state.getT1());
                });

        flux
                .onErrorReturn(IllegalStateException.class, -1L)
                .onErrorReturn(RuntimeException.class, 0L)
                .subscribe(item -> {
                    System.out.println(item);
                });
    }


    private void onErrorMapTest() {
        Flux<Long> flux = Flux.generate(() -> Tuples.<Long, Long>of(0L, 1L),
                (state, sink) -> {
                    if (state.getT1() < 0) {
                        sink.error(new RuntimeException("数据值超越了边界"));
                    } else {
                        sink.next(state.getT1());
                    }
                    return Tuples.of(state.getT2(), state.getT2() + state.getT1());
                });
        flux.onErrorMap(e -> {
            return new IllegalStateException("我是转后之后的异常");
        })
//                .subscribe(System.out::println,System.out::print);
                .subscribe(item -> {
                    System.out.println(item);
                }, e -> {
                    System.out.println("异常:" + e.getMessage());
                });
    }


    /**
     * 超时
     */
    private void timeOutTest() {
        Flux<Long> flux = generatorFibonacci();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        flux.delayElements(Duration.ofMillis(1000))
                .timeout(Duration.ofMillis(500), Flux.just(-1L))
                .subscribe(System.out::println, e -> {
                    System.out.println("超时错误：" + e.getMessage());
                    countDownLatch.countDown();
                });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private void retryTest() {
//        Flux<Long> flux = generatorFibonacci();
//        CountDownLatch countDownLatch = new CountDownLatch(1);
//        flux.delayElements(Duration.ofMillis(1000))
//                .timeout(Duration.ofMillis(500))
//                .retry(1)
//                .subscribe(System.out::println, e -> {
//                    System.out.println("超时错误：" + e.getMessage());
//                    countDownLatch.countDown();
//                });
//        try {
//            countDownLatch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 只要产生凑无，流就会停止
     */
    private void sequenceWillTerminateWhenErrorRaise() {
        Flux<String> flux = Flux.interval(Duration.ofMillis(250))
                .map(value -> {
                    if (value < 3) {
                        return "tick " + value;
                    }
                    throw new RuntimeException("boom");
                })
                .onErrorReturn("uh oh");

        flux.subscribe(System.out::println);
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void retryTest2() {
        Flux.interval(Duration.ofMillis(250))
                .map(input -> {
                    if (input < 3) {
                        return "tick " + input;
                    }
                    throw new RuntimeException("boom");
                })
                .retry(1)
                .elapsed()
                .subscribe(System.out::println, System.err::println);

        try {
            Thread.sleep(2100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**------------------------------产生错误----------------------------------*/

    /**
     * 生产者产生异常
     */
    private void producerCreateError() {
        Flux<Integer> flux = Flux.generate(() -> Integer.valueOf(0), (state, sink) -> {
            Integer b = state + 1;
            if (b == 10) {
//                sink.complete();
                RuntimeException runtimeException = new RuntimeException("生产者生成的异常");
//                sink.error(runtimeException);
                throw runtimeException;
            }
            sink.next(b);
            return b;
        });

        flux.subscribe(System.out::println, e -> System.out.println("消费者处理异常：" + e.getMessage()));
    }

    private void producerCreateError2() {
        Flux<Integer> flux = Flux.generate(() -> Integer.valueOf(0), (state, sink) -> {
            Integer b = state + 1;
            if (b == 10) {
                throw Exceptions.propagate(new RuntimeException("往下游传播错误"));
            }
            sink.next(b);
            return b;
        });

        flux.subscribe(x -> System.out.println(x),
                e -> System.out.println(e.getMessage()));

    }

    /**
     * 消费者产生异常
     */
    private void consumerCreateError() {
        Flux<Integer> flux = Flux.generate(() -> Integer.valueOf(0), (state, sink) -> {
            Integer b = state + 1;
            if (b == 10) {
                sink.complete();
            }
            sink.next(b);
            return b;
        });
        flux.subscribe(x -> {
                    throw new RuntimeException("【消费者】产生异常");
                },
                e -> System.out.println("消费者处理异常：" + e.getMessage()));
    }

    /**
     * 生产者和订阅者同时抛出异常，那么只有消费者的异常被捕获到
     */
    private void producerAndConsumerBothThrow() {
        Flux<Integer> flux = Flux.generate(() -> Integer.valueOf(0), (state, sink) -> {
            Integer b = state + 1;
            if (b == 10) {
//                sink.complete();
                RuntimeException runtimeException = new RuntimeException("生产者生成的异常");
//                sink.error(runtimeException);
                throw runtimeException;
            }
            sink.next(b);
            return b;
        });

        flux.subscribe(item -> {
//                    System.out.println(item);
                    throw new RuntimeException("自定【义消费者】错误");
                },
                e -> System.out.println("消费者处理异常：" + e.getMessage()));
    }


    /**
     * 测试可检查异常
     */
    private void testCheckedException() {
        Flux<Long> fibonacciGenerator = Flux.generate(() -> Tuples.<Long, Long>of(0L, 1L),
                (state, sink) -> {
                    try {
                        raisedCheckedException();
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                    return Tuples.of(state.getT2(), state.getT2() + state.getT1());
                });
        fibonacciGenerator.subscribe(System.out::println, e -> {
            System.out.println(Exceptions.unwrap(e));
        });
    }

    private void raisedCheckedException() throws IOException {
        throw new IOException("抛出可检查异常");
    }


    /**------------------------------错误处理----------------------------------*/


    /**
     * 流是否能够正常结束
     */
    private void testFluxCompleteNormally() {
        Flux<Integer> flux = getIntFlux();
        flux
                .doFinally(x -> System.out.println("Finally"))
                .doOnComplete(() -> System.out.println("Complete"))
                .subscribe(item -> {
                    System.out.println(item);
                }, e -> System.out.println("消费者处理异常：" + e.getMessage()));
    }


    /**
     * 生命周期的错误处理消费者和钩子都会处理
     */
    private void doOnErrorHook() {
        Flux<Integer> errorFlux = getIntFluxWithException();
        errorFlux
//                .doOnError(e -> System.out.println("生命周期处理错误：" + e.getMessage()))
//                .subscribe(x -> System.out.println(x));
                .subscribe(x -> System.out.println(x), e -> System.out.println("消费者处理错误：" + e.getMessage()));
    }

    /**------------------------------错误恢复----------------------------------*/

    /**
     * 出现错误时，把错误的数据值替换为一个指定的值
     */
    private void onErrorReturnTest() {
        Flux<Integer> flux = getNormalFlux();
        flux
                .map(x -> {
                    if (x == 5) {
                        throw Exceptions.propagate(new RuntimeException("数据异常"));
                    }
                    return x;
                })
                .onErrorReturn(15)
                .doOnError(e -> System.out.println("钩子处理错误：" + e))
                .subscribe(item -> {
                    System.out.println(item);
                }, e -> {
                    System.out.println("消费者错误处理：" + e);
                });
    }


    /**
     * 出现错误时，把错误的数据值替换为一个指定的流
     */
    private void onErrorResumeTest() {
        Flux<Integer> flux = getNormalFlux();

        flux
                .map(x -> {
                    if (x == 5) {
                        throw new RuntimeException("数据异常:" + x);
                    }
                    return x;
                })
                .onErrorResume(x -> Flux.just(100, 200, 300))
                .doOnError(e -> System.out.println("钩子处理错误：" + e))
                .subscribe(System.out::println, e -> {
                    System.out.println("消费者错误处理：" + e);
                });
    }

    /**
     * 错误恢复，当流中某个元素发生错误后，丢弃这个元素，然后继续执行后续的元素
     */
    private void onErrorContinueTest() {
        Flux<Integer> flux = getNormalFlux();

        flux
                .map(x -> {
                    if (x == 5) {
                        throw new RuntimeException("数据转换异常:" + x);
                    }
                    return x;
                })
                .onErrorContinue((e, x) -> {
                    System.out.println("错误恢复：" + e + "   错误元素：" + x);
                })
                .doOnError(e -> System.out.println("钩子处理错误：" + e))
                .subscribe(System.out::println, e -> {
                    System.out.println("消费者错误处理：" + e);
                });
    }

    /**
     * ------------------------------公共方法----------------------------------
     */

    private Flux<Integer> getIntFlux() {
        Flux<Integer> flux = Flux.generate(() -> Integer.valueOf(0), (state, sink) -> {
            Integer b = state + 1;
            if (b == 10) {
                sink.complete();
//                RuntimeException runtimeException = new RuntimeException("生产者生成的异常");
//                sink.error(runtimeException);
//                throw runtimeException;
            }
            sink.next(b);
            return b;
        });
        return flux;
    }

    private Flux<Integer> getIntFluxWithException() {
        Flux<Integer> flux = Flux.generate(() -> Integer.valueOf(0), (state, sink) -> {
            Integer b = state + 1;
            if (b == 10) {
                sink.error(new RuntimeException("错误处理"));
            }
            sink.next(b);
            return b;
        });
        return flux;
    }

    private Flux<Integer> getNormalFlux() {
        Flux<Integer> flux = Flux.generate(() -> Integer.valueOf(0), (state, sink) -> {
            Integer b = state + 1;
            if (b == 15) {
                sink.complete();
            }
            sink.next(b);
            return b;
        });
        return flux;
    }

    private void tttt() {

    }

}
