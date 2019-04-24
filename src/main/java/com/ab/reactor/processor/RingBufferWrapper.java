package com.ab.reactor.processor;

import reactor.core.Exceptions;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.WorkQueueProcessor;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 必须要保证单例
 *
 * @author xiaozhao
 * @date 2019/4/148:02 PM
 */
public class RingBufferWrapper {

    /**
     * TODO 线程安全问题？
     */
    private FluxSink<Message> fluxSink;

    public RingBufferWrapper() {
        WorkQueueProcessor<Message> workQueueProcessor = WorkQueueProcessor.<Message>builder().build();

        // 构建流水线
        workQueueProcessor
                .parallel(2)
                .runOn(Schedulers.newParallel("rb-parallel-", 2))
                .map(message -> {
                    print("【map1】处理 " + message.getHead());

                    if ("head5".equals(message.getHead())) {
                        print("-------------准备抛出异常----------------");
//                        throw new RuntimeException("自定义异常：" + message.getHead());
                        throw Exceptions.propagate(new RuntimeException("自定义异常：" + message.getHead()));
                    }

                    message.setBody(message.getBody() + "-map1");
                    return message;
                })
                .map(message -> {
                    print("【map2】处理 " + message.getHead());
                    message.setBody(message.getBody() + "-map2");
                    return message;
                })
                .doOnError(error -> {
                    print("【错误钩子】" + error.getMessage());
                })
                .sequential()
//                .onErrorReturn(new Message("HEADER_FALLBACK", "BODY"))
                .onErrorContinue((error, value) -> {
                    print("【onErrorContinue】执行");
                    print(error.getMessage() + "---【onErrorContinue】----" + value);
                    System.out.println();
                })

                .subscribe(new RingBufferSubscriber("消费者"));

        fluxSink = workQueueProcessor.sink();
    }

    /**
     * 工具方法，打印线程名称和消息
     *
     * @param text
     */
    private void print(String text) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + text);
    }

    /**
     * 往RingBuffer上添加数据
     *
     * @param value
     */
    public void next(Message value) {
        print("往RingBuffer上添加数据： " + value);
        this.fluxSink.next(value);
    }

    public static void main(String[] args) {
        RingBufferWrapper ringBufferWrapper = new RingBufferWrapper();

        // 模拟几个生产者
        AtomicInteger integer = new AtomicInteger(1);

        Runnable runnable = () -> {
            while (true) {
                ringBufferWrapper.next(new Message("head" + integer.getAndIncrement(), "body"));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(runnable, "生产者-1").start();

//        new Thread(runnable, "生产者-2").start();

    }
}
