package com.ab.reactor.mono;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Optional;

/**
 * @author xiaozhao
 * @date 2019/4/164:14 PM
 */
public class ContextDemo {


    /**
     * 在流水线中共享参数
     */
    private void start() {
        String key = "message";
        Mono<String> mono = Mono.just("Hello")
                .flatMap(s -> Mono.subscriberContext()
                        .map(ctx -> s + " " + ctx.get(key))
                )
                .subscriberContext(ctx -> ctx.put(key, "World"));

        mono.subscribe(x -> System.out.println(x));
    }

    /**
     * 只有在subscriberContext之前的操作符才可以使用
     */
    private void onlyOperatorsAboveCanSee() {
        String key = "message";
        Mono<String> r = Mono.just("Hello")
                .subscriberContext(ctx -> ctx.put(key, "World"))
                .flatMap(s -> Mono.subscriberContext()
                        .map(ctx -> s + " " + ctx.getOrDefault(key, "Stranger")));

        Mono<String> r2 = Mono.just("Hello")
                .flatMap(s -> Mono.subscriberContext()
                        .map(ctx -> s + " " + ctx.getOrDefault(key, "Stranger")))
                .subscriberContext(ctx -> ctx.put(key, "World"));


        // 输出 Hello Stranger
        r.subscribe(x -> System.out.println(x));

        // 输出 Hello World
        r2.subscribe(x -> System.out.println(x));
    }


    /**
     * 不可变
     */
    private void test3() {
        String key = "message";

        Mono<String> r = Mono.subscriberContext()
                .map(ctx -> ctx.put(key, "Hello"))
                // 这里返回了一个新的，因此上面的设置失效了
                .flatMap(ctx -> Mono.subscriberContext())
                .map(ctx -> ctx.getOrDefault(key, "Default"));

        r.subscribe(x -> System.out.println(x));
    }


    /**
     * 如果多个连续的subscriberContext的话，最上面的为准，也就是距离operator最近的一个为准
     */
    private void test4() {
        String key = "message";
        Mono<String> r = Mono.just("Hello")
                .flatMap(s -> Mono.subscriberContext()
                        .map(ctx -> s + " " + ctx.get(key)))
                .subscriberContext(ctx -> ctx.put(key, "Reactor"))
                .subscriberContext(ctx -> ctx.put(key, "World"));

        StepVerifier.create(r)
                .expectNext("Hello Reactor")
                .verifyComplete();
    }


    /**
     *
     */
    private void test5() {
        String key = "message";
        Mono<String> r = Mono.just("Hello")
                .flatMap(s -> Mono.subscriberContext()
                        .map(ctx -> s + " " + ctx.get(key)))
                .subscriberContext(ctx -> ctx.put(key, "Reactor"))
                .flatMap(s -> Mono.subscriberContext()
                        .map(ctx -> s + " " + ctx.get(key)))
                .subscriberContext(ctx -> ctx.put(key, "World"));

        StepVerifier.create(r)
                .expectNext("Hello Reactor World")
                .verifyComplete();
    }


    /**
     *
     */
    private void test6() {
        String key = "message";
        Mono<String> r =
                Mono.just("Hello")
                        .flatMap(s -> Mono.subscriberContext()
                                .map(ctx -> s + " " + ctx.get(key))
                        )
                        .flatMap(s -> Mono.subscriberContext()
                                .map(ctx -> s + " " + ctx.get(key))
                                .subscriberContext(ctx -> ctx.put(key, "Reactor"))
                        )
                        .subscriberContext(ctx -> ctx.put(key, "World"));

        StepVerifier.create(r)
                .expectNext("Hello World Reactor")
                .verifyComplete();
    }


    private void myTest() {
        Mono<String> mono = definePipeline();
        mono
                .subscriberContext(ctx -> ctx.put("message", "Java"))
                .subscribe(x -> System.out.println(x));

    }

    private Mono<String> definePipeline() {
        String key = "message";
        Mono<String> mono = Mono.just("Hello");
//                .flatMap(s -> Mono.subscriberContext()
//                        .map(ctx -> s + " " + ctx.get(key))
//                );
        return mono;
    }


    static final String HTTP_CORRELATION_ID = "reactive.http.library.correlationId";

    Mono<Tuple2<Integer, String>> doPut(String url, Mono<String> data) {
        Mono<Tuple2<String, Optional<Object>>> dataAndContext =
                data.zipWith(Mono.subscriberContext()
                        .map(c -> c.getOrEmpty(HTTP_CORRELATION_ID)));

        return dataAndContext
                .<String>handle((dac, sink) -> {
                    if (dac.getT2().isPresent()) {
                        sink.next("PUT <" + dac.getT1() + "> sent to " + url + " with header X-Correlation-ID = " + dac.getT2().get());
                    }
                    else {
                        sink.next("PUT <" + dac.getT1() + "> sent to " + url);
                    }
                    sink.complete();
                })
                .map(msg -> Tuples.of(200, msg));
    }

    public void contextForLibraryReactivePut() {
        Mono<String> put = doPut("www.example.com", Mono.just("Walter"))
                .subscriberContext(Context.of(HTTP_CORRELATION_ID, "2-j3r9afaf92j-afkaf"))
                .filter(t -> t.getT1() < 300)
                .map(Tuple2::getT2);

        StepVerifier.create(put)
                .expectNext("PUT <Walter> sent to www.example.com with header X-Correlation-ID = 2-j3r9afaf92j-afkaf")
                .verifyComplete();
    }


    public static void main(String[] args) {
        ContextDemo demo = new ContextDemo();
        demo.contextForLibraryReactivePut();

    }
}
