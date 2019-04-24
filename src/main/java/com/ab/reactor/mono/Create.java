package com.ab.reactor.mono;

import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * @author xiaozhao
 * @date 2019/4/2410:17 AM
 */
public class Create {

    /**
     * 简单创建
     * 1）just
     * 2）empty
     * 3）fromSupplier
     * 4）justOrEmpty
     */
    private static void simpleCreate() {

//        Mono.just("Mono").subscribe(System.out::println);


//        Mono.empty().subscribe(x -> {
//                    System.out.println("empty");
//                    System.out.println(x);
//                },
//                e -> System.out.println(e),
//                () -> System.out.println("Complete")
//        );

//        Mono.fromSupplier(() -> "Hello").subscribe(System.out::println);

//        Mono.justOrEmpty(Optional.of("Hello")).subscribe(System.out::println);
    }


    /**
     * 高级创建
     */
    private static void advanceCreate() {
        Mono.create(sink -> sink.success("Hello")).subscribe(System.out::println);
    }


    public static void main(String[] args) {
        advanceCreate();
    }

}
