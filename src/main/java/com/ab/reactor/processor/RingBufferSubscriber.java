package com.ab.reactor.processor;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

/**
 * 消费者处理的类
 *
 * @author xiaozhao
 * @date 2019/4/1411:02 PM
 */
public class RingBufferSubscriber<T> extends BaseSubscriber<T> {

    private String name;

    public RingBufferSubscriber(String name) {
        this.name = name;
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        super.hookOnSubscribe(subscription);
    }

    @Override
    protected void hookOnNext(T value) {
        super.hookOnNext(value);
        print("消费者处理数据：" + value);
        System.out.println();
    }

    @Override
    protected void hookOnComplete() {
        super.hookOnComplete();
    }

    @Override
    protected void hookOnError(Throwable throwable) {
//            super.hookOnError(throwable);
        print("消费者错误捕获：" + throwable.getMessage());
    }

    private static void print(String text) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + text);
    }
}