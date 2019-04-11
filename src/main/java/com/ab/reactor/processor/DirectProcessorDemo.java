package com.ab.reactor.processor;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;

import java.util.stream.IntStream;


/**
 * 最简单的Processor，不支持背压
 *
 * @author xiaozhao
 * @date 2019/4/712:09 PM
 */
public class DirectProcessorDemo {
    private void demo1() {
        DirectProcessor<Integer> directProcessor = DirectProcessor.create();
        Flux<Integer> flux = directProcessor.filter(i -> i % 2 == 0)
                .map(item -> item + 1);

        flux.subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println(integer);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });

        IntStream.range(1,20)
                .forEach(e -> {
                    directProcessor.onNext(e);
                });

        directProcessor.onComplete();
        directProcessor.blockLast();
    }

    public static void main(String[] args) {
        new DirectProcessorDemo().demo1();
    }
}
