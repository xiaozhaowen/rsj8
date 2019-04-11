package com.ab.reactor.flux;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

/**
 * 自定义的订阅处理类，当需要控制每次的请求数量时，推荐这种方式来替换lambda形式
 *
 * @author xiaozhao
 * @date 2019/4/43:19 PM
 */
public class SampleSubscriber<T> extends BaseSubscriber<T> {

    /**
     * @param subscription
     */
    @Override
    public void hookOnSubscribe(Subscription subscription) {
        System.out.println("Subscribed");
        request(1);
    }

    /**
     * 触发next的钩子
     *
     * @param value
     */
    @Override
    public void hookOnNext(T value) {
        System.out.println(value);
        request(1);
    }

    /**
     * 流执行完毕的钩子
     */
    @Override
    protected void hookOnComplete() {
        System.out.println("请求完毕的钩子执行");
    }


    /**
     * 处理错误
     *
     * @param throwable
     */
    @Override
    protected void hookOnError(Throwable throwable) {
//        super.hookOnError(throwable);
        System.out.println("错误处理的钩子执行");
    }
}
