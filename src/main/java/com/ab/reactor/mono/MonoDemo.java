package com.ab.reactor.mono;


import reactor.core.publisher.Mono;


/**
 * @author xiaozhao
 * @date 2019/4/42:12 PM
 */
public class MonoDemo {

    /**
     * 最简答的创建Mono
     */
    private void simplestCreate() {

        // just方法只接受一个参数
        Mono<String> stringMono = Mono.just("Hello");

        stringMono.subscribe(msg -> System.out.println(msg));
    }


    public static void main(String[] args) {
        MonoDemo demo = new MonoDemo();
        demo.simplestCreate();
    }
}
