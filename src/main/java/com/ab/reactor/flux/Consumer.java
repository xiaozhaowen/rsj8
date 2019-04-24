package com.ab.reactor.flux;

import reactor.core.publisher.Flux;

/**
 * 消费示例
 *
 * @author xiaozhao
 * @date 2019/4/2410:28 AM
 */
public class Consumer {

    private static void baseConsumer() {
        Flux.just("Hello Reactor")
                .subscribe(System.out::println);
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






    public static void main(String[] args) {
        empty();
    }
}
