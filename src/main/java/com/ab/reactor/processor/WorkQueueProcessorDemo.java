package com.ab.reactor.processor;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.WorkQueueProcessor;
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


    private void multiUpstreamPublisher() {
        WorkQueueProcessor<String> workQueueProcessor = WorkQueueProcessor.share("test", 256);

        workQueueProcessor.subscribe(t -> System.out.println(t));

        Flux<String> flux = Flux.interval(Duration.ofSeconds(1)).map(t -> "1_" + t);
        Flux<String> flux2 = Flux.interval(Duration.ofSeconds(1)).map(t -> "2_" + t);

//        flux.subscribe(workQueueProcessor);

        flux2.subscribe(workQueueProcessor);
    }






    public static void main(String[] args) {
        new WorkQueueProcessorDemo().multiUpstreamPublisher();
    }
}
