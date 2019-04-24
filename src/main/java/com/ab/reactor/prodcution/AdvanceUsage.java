package com.ab.reactor.prodcution;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Flux的高级用法
 *
 * @author xiaozhao
 * @date 2019/4/179:59 AM
 */
public class AdvanceUsage {

    /**
     * Flux中套Flux
     */
    private void fluxNest() {
        Flux<String> flux = Flux.just("Java", "Python", "Ruby")
                .map(x -> {
                    Flux.just(x)
                            .map(subX -> subX.toUpperCase())
                            .subscribe(subX -> {
                                System.out.println("内层Flux " + subX);
                            });
                    return x;
                });

        flux.subscribe(System.out::println);
    }


    private void testSum() {
        Flux<Integer> flux = Flux.range(1, 3);
        Mono<Integer> mono = sum(flux);
        mono.subscribe(System.out::println);
    }

    /**
     * 把流中的元素合并起来
     *
     * @param flux
     * @return
     */
    private Mono<Integer> sum(Flux<Integer> flux) {
        return flux.reduce((result, current) -> {
            System.out.println(result + "+" + current);
            return result + current;
        });
    }


    /**
     * 测试flatMap的使用，一对多。把每一个元素变为一个流
     */
    private void flatMapTest() {
        Flux.just(1, 10, 15)
                .flatMap(value -> {
                    Collection<Integer> collection = findFactor(value);
                    return Flux.fromIterable(collection);
                });


    }

    private Collection<Integer> findFactor(int number) {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= number; i++) {
            if (number % i == 0) {
                list.add(i);
            }
        }
        System.out.println(list);
        return list;
    }


    public static void main(String[] args) {
        AdvanceUsage advanceUsage = new AdvanceUsage();
        advanceUsage.findFactor(24);
    }

}
