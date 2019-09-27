package com.ab.reactor.flux;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * @author xiaozhao
 * @date 2019/5/209:36 AM
 */
public class Bridge {

    private Flux<Integer> test() {
        return Flux.range(1, 3)
                .doOnSubscribe(x -> {
                    System.out.println("触发订阅");
                });

    }


    private Flux<String> doIt(Flux<String> srcFlux) {

//        Flux<String> flux = Flux.from(srcFlux);
//        flux.doOnSubscribe(x -> {
//            System.out.println("触发订阅");
//        });
//        return flux;

        FluxSink fluxSink = null;
//        Flux<String> resultFlux = Flux.create(stringFluxSink -> {
//
//        }).subscriberContext(ctx->{
//            ctx.put("",stringFluxSink);
//        });

        Flux<String> resultFlux =null;
//        Flux<String> resultFlux = Flux.create(stringFluxSink -> {stringFluxSink.next()});

         resultFlux.doOnSubscribe(x -> {
            System.out.println("路由Flux--触发订阅");
            srcFlux.subscribe(sx -> {
                System.out.println("原始:" + sx);
//                if ("ABC".equals(sx)) {
//                    System.out.println("发现ABC");
//                    resultFlux.map(tt -> {
//                        System.out.println("发现银保通");
//                        return tt;
//                    });
//                } else {
//                    System.out.println("没有发现ABC");
//                    resultFlux.map(tt -> {
//                        System.out.println("不是银泰吧");
//                        return tt;
//                    });
//                }


            });
        });

         return resultFlux;
    }

    public static void main(String[] args) {
        Bridge bridge = new Bridge();

//        Flux<Integer> flux = bridge.test();
//
//        flux.subscribe(x -> {
//            System.out.println(x);
//        });

        Flux<String> srcFlux = Flux.just("ABC2");

        Flux<String> resultFlux = bridge.doIt(srcFlux);
        resultFlux.subscribe(x -> {
            System.out.println("客户端执行：" + x);
        });


    }
}
