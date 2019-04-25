package com.ab.reactor.flux;

import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * 消费示例
 *
 * @author xiaozhao
 * @date 2019/4/2410:28 AM
 */
public class Consumer {


    /**
     * 仅仅只是启动流水线，map打印为了说明流水线确实启动了
     */
    private static void justTrigger() {
        Flux<Integer> flux = Flux.range(1, 3)
                .map(x -> {
                    System.out.println("流水线map工作：" + x);
                    return x;
                });
        flux.subscribe();
    }

    /**
     * 只有消费逻辑
     */
    private static void onlyConsumeLogic() {
        Flux.range(1, 3)
                .subscribe(System.out::println);
    }

    /**
     * 消费逻辑与错误处理
     */
    private static void consumeAndErrorHandle() {
        Flux.range(1, 3)
                .map(x -> {
                    if (x <= 2) {
                        return x;
                    }
                    throw new RuntimeException("自定义错误:" + x);
                })
                .subscribe(
                        value -> System.out.println(value),
                        error -> System.out.println(error.getMessage())
                );
    }

    /**
     * 消费+错误处理+流完成
     * <p>
     * 错误处理与完成事件是互斥的，不可能同时执行的
     */
    private static void consume_error_complete() {
        Flux.range(1, 4)
//                .map(x -> {
//                    if (x <= 3) {
//                        return x;
//                    }
//                    throw new RuntimeException("自定义错误:" + x);
//                })
                .subscribe(
                        value -> System.out.println(value),
                        error -> System.out.println(error.getMessage()),
                        () -> System.out.println("流执行完毕了")
                );
    }

    /**
     * 消费+错误处理+流完成+请求逻辑
     * 请求逻辑中必须写请求逻辑，否则流水线不启动
     */
    private static void consume_error_complete_request() {
        Flux.range(1, 4)
                .map(x -> {
                    System.out.println("map:" + x);
                    return x;
                })
                .subscribe(
                        value -> System.out.println(value),
                        error -> System.out.println(error.getMessage()),
                        () -> System.out.println("流执行完毕了"),
                        subscription -> {

                            // 必须写请求逻辑
                            subscription.request(10);
                        }
                );
    }


    /**
     * Flux.empty() 不会推送任何数据，只有一个完成的信号推给消费者
     */
    private static void empty() {
        Flux.empty()
                .subscribe(
                        x -> System.out.println(x),
                        error -> System.out.println(error),
                        () -> System.out.println("收到了流的完成信号")
                );
    }


    /**
     * 使用一个完整的类来替代lambda，适合定制请求，提供了很多钩子函数
     */
    private static void subscribeInClassForm() {
        SampleSubscriber<Integer> sampleSubscriber = new SampleSubscriber<>();
        Flux<Integer> integerFlux = Flux.range(1, 4).map(i -> {
            if (i == 2) {
                throw new RuntimeException("ERROR");
            }
            return i;
        });

        /**
         *  lambda形式
         */

        /**  integerFlux.subscribe(i -> System.out.println(i),
         error -> System.err.println("Error " + error),
         () -> {
         System.out.println("Done");
         },
         s -> s.request(10));
         */


        /**
         * 类的形式
         */
        integerFlux.subscribe(sampleSubscriber);
    }


    /**
     * 在收到一个元素后，取消订阅
     * 类形式的订阅
     */
    private static void cancelSubscribe() {
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

    /**
     * 通过Disposable接口来取消
     */
    private static void cancelByDispose() {
        Disposable disposable = Flux.range(1, 2000)
                .delayElements(Duration.ofMillis(500))
                .subscribe(x -> System.out.println(x));


        new Thread(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("子线程醒来，停止订阅");
                disposable.dispose();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();

        try {
            Thread.sleep(10000);
            System.out.println("主线程退出");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        cancelByDispose();
    }
}
