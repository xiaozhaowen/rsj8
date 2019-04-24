package com.ab.reactor.processor;

import org.reactivestreams.Subscription;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * @author xiaozhao
 * @date 2019/4/511:07 PM
 */
public class WorkQueueProcessorDemo {


    private void start() {
//        WorkQueueProcessor<Long> workQueueProcessor = WorkQueueProcessor.<Long>builder().build();
        WorkQueueProcessor<Long> workQueueProcessor = WorkQueueProcessor.share("test", 256);


        workQueueProcessor.subscribe(new BaseSubscriber<Long>() {
            Subscription s;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                this.s = subscription;
                subscription.request(1);
            }

            @Override
            protected void hookOnNext(Long value) {
                System.out.println("第1个值：" + value);

                s.request(1);
                if (value > 12) {
                    throw new RuntimeException("自定义错误");
                }
            }

            @Override
            protected void hookOnComplete() {
                System.out.println("第1个:DONE");
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                System.out.println("第1个ERROR:" + throwable.getMessage());
                s.request(1);
            }

            @Override
            protected void hookOnCancel() {
                System.out.println("第1个:Cancel");
            }
        });

       /* workQueueProcessor.subscribe(new BaseSubscriber<Long>() {
            Subscription s;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                this.s = subscription;
                subscription.request(1);
            }

            @Override
            protected void hookOnNext(Long value) {
                System.out.println("第2个值：" + value);

                s.request(1);
//                if (value > 2) {
//                    throw new RuntimeException("自定义错误");
//                }
            }

            @Override
            protected void hookOnComplete() {
                System.out.println("第2个:DONE");
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                super.hookOnError(throwable);
            }

            @Override
            protected void hookOnCancel() {
                super.hookOnCancel();
            }
        });*/


        FluxSink<Long> sink = workQueueProcessor.sink();
        for (int i = 1; i <= 50; i++) {
            sink.next(Long.valueOf(i));
        }

    }


    /**
     * 教程原始代码，每一个订阅者都会开启一个新的线程.
     * 生产者默认使用轮询的方式把每一个消息推给其中一个订阅者
     */
    private void bookDemo() {
        WorkQueueProcessor<String> workQueueProcessor = WorkQueueProcessor.<String>builder().build();

        workQueueProcessor.subscribe(t -> print("1. " + t));
        workQueueProcessor.subscribe(t -> print("2. " + t));

        FluxSink<String> fluxSink = workQueueProcessor.sink();
        fluxSink.next("A");
        fluxSink.next("B");
        fluxSink.next("C");
    }

    /**
     * 书本代码简单变种，如果使用Flux的话，则默认为单线程，也就是主线程
     */
    private void bookDemoSimpleVariant() {
        Flux<String> flux = Flux.just("A", "B", "C");

        // 构建流水线
        flux.map(x -> {
            print("map1");
            return x;
        }).subscribe(x -> {
            print("消费者接收：" + x);
        });
    }


    /**
     * WorkQueueProcessor默认开启新线程
     */
    private void workQueueProcessorOpenThread() {
        WorkQueueProcessor<String> workQueueProcessor = WorkQueueProcessor.<String>builder().build();

        // 构建流水线
        workQueueProcessor.map(x -> {
            print("map1");
            return x;
        }).subscribe(x -> {
            print("消费者接收：" + x);
        });

        // 发布数据
        FluxSink<String> fluxSink = workQueueProcessor.sink();
        fluxSink.next("A");
        fluxSink.next("B");
        fluxSink.next("C");
    }

    /**
     * WorkQueueProcessor并行处理
     */
    private void parallelTest() {
        WorkQueueProcessor<String> workQueueProcessor = WorkQueueProcessor.create();

        // 构建流水线
        workQueueProcessor
                .parallel()
                .runOn(Schedulers.parallel())
                .map(x -> {
                    print("map1");
                    return x;
                })
                .subscribe(x -> print("消费者接收：" + x));

        // 发布数据
        FluxSink<String> fluxSink = workQueueProcessor.sink();
        for (int i = 1; i <= 100; i++) {
            fluxSink.next("value" + i);
        }
    }


    /**
     *
     */
    private void realTest() {
        WorkQueueProcessor<Message> workQueueProcessor = WorkQueueProcessor.create();

        // 构建流水线
        workQueueProcessor
                .parallel(1)
                .runOn(Schedulers.newParallel("pp", 1))
                .map(message -> {
                    print("map1 " + message.getHead());
                    if ("head3".equals(message.getHead())) {
                        print("-----------------------------");
                        throw new RuntimeException("自定义异常：" + message.getHead());
                    }
                    message.setHead(message.getHead() + "-map1");
                    return message;
                })
                .map(message -> {
                    print("map2 " + message.getHead());
                    message.setHead(message.getHead() + "-map2");
                    return message;
                })
                .sequential()
                .onErrorContinue((e, value) -> {
                    print("【onErrorContinue】" + e.getMessage());
                    print("【onErrorContinue】" + value);
                    System.out.println();
                })
                .doOnError(e -> print("【错误钩子】" + e.getMessage()))
                .subscribe(new RingBufferSubscriber("消费者"));


        // 发布数据
        FluxSink<Message> fluxSink = workQueueProcessor.sink();
        for (int i = 1; i <= 10; i++) {
            fluxSink.next(new Message("head" + i, "body" + i));
        }
    }


    private static void print(String text) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + text);
    }

    public static void main(String[] args) {
        new WorkQueueProcessorDemo().realTest();
    }
}
