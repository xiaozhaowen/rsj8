package com.ab.reactor.processor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;
import reactor.util.concurrent.Queues;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author xiaozhao
 * @date 2019/4/712:16 PM
 */
public class UnicastProcessorDemo {
    public void testUnicastProcessor() throws InterruptedException {
        UnicastProcessor<Integer> unicastProcessor = UnicastProcessor.create(Queues.<Integer>get(8).get());
        Flux<Integer> flux = unicastProcessor
                .map(e -> e)
                .doOnError(e -> {
                    System.out.println(e);
                });

        IntStream.rangeClosed(1, 12)
                .forEach(e -> {
                    System.out.println("emit:" + e);
                    unicastProcessor.onNext(e);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                });
        System.out.println("begin to sleep 7 seconds");
        TimeUnit.SECONDS.sleep(7);
        //UnicastProcessor allows only a single Subscriber
        flux.subscribe(e -> {
            System.out.println("flux subscriber:" + e);
        });

        unicastProcessor.onComplete();
        TimeUnit.SECONDS.sleep(10);
//        unicastProcessor.blockLast(); //blockLast也是一个subscriber
    }

    public static void main(String[] args) throws InterruptedException {
        new UnicastProcessorDemo().testUnicastProcessor();
    }
}
