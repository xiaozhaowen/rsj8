package com.ab.reactor.flux;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基本操作符使用
 *
 * @author xiaozhao
 * @date 2019/4/42:11 PM
 */
public class Operators {


    /**
     * 线程测试
     */
    private void threadTest() {
        final Mono<String> mono = Mono.just("hello ");

        Thread thread = new Thread(() -> mono
                .map(msg -> msg + "thread ")
                .subscribe(v ->
                        System.out.println(v + Thread.currentThread().getName())
                )
        );

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread().getName());
    }


    private void publishOnTest() {
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux
                .range(1, 2)
                .map(i -> 10 + i)
                .publishOn(s)
                .map(i -> "value " + i);

        new Thread(() -> flux.subscribe(System.out::println)).start();
    }


    private void intervalTest() {
        Flux<Long> flux = Flux.interval(Duration.ofMillis(100));

        flux.subscribe(t -> {
            System.out.println(t);
        });
    }

    private void multiPublisher() {
        Flux<String> flux = Flux.interval(Duration.ofMillis(100)).map(t -> "1_" + t);
        Flux<String> flux2 = Flux.interval(Duration.ofMillis(100)).map(t -> "2_" + t);


        BaseSubscriber<String> stringBaseSubscriber = new BaseSubscriber<String>() {
            @Override
            protected void hookOnNext(String value) {
                System.out.println(value);
            }
        };

        flux.subscribe(stringBaseSubscriber);
        flux2.subscribe(stringBaseSubscriber);
    }


    private static void bufferTest() {
        Flux.range(1, 100).buffer(20).subscribe(System.out::println);
//        Flux.interval(Duration.of(10,ChronoUnit.SECONDS)).bufferTimeout(1001,Duration.ofMillis(100)).take(2).toStream().forEach(System.out::println);
//        Flux.range(1, 10).bufferUntil(i -> i % 2 == 0).subscribe(System.out::println);
//        Flux.range(1, 10).bufferWhile(i -> i % 2 == 0).subscribe(System.out::println);
    }

    /**
     * 输出偶数
     */
    private static void filterTest() {
        Flux.range(1, 10).filter(i -> i % 2 == 0).subscribe(System.out::println);
    }

    /**
     * window 操作符的作用类似于 buffer，所不同的是 window 操作符是把当前流中的元素收集到另外的 Flux 序列中，因此返回值类型是 Flux<Flux<T>>
     */
    private static void windowTest() {
        Flux.range(1, 100).window(20).subscribe(System.out::println);
    }

    /**
     * zipWith 操作符把当前流中的元素与另外一个流中的元素按照一对一的方式进行合并
     */
    private static void zipWithTest() {
        Flux.just("a", "b")
                .zipWith(Flux.just("c", "d"))
                .subscribe(System.out::println);

        Flux.just("a", "b")
                .zipWith(Flux.just("c", "d"), (s1, s2) -> String.format("%s-%s", s1, s2))
                .subscribe(System.out::println);
    }

    private static void takeTest() {
        Flux.range(1, 1000).take(10).subscribe(System.out::println);
        System.out.println();
        Flux.range(1, 1000).takeLast(10).subscribe(System.out::println);
        System.out.println();
        Flux.range(1, 1000).takeWhile(i -> i < 10).subscribe(System.out::println);
        System.out.println();
        Flux.range(1, 1000).takeUntil(i -> i == 10).subscribe(System.out::println);
    }

    private static void reduceTest() {
        Flux.range(1, 100).reduce((x, y) -> x + y).subscribe(System.out::println);
        Flux.range(1, 100).reduceWith(() -> 100, (x, y) -> x + y).subscribe(System.out::println);
    }


    private static void dispatchTest() {
        Flux.create(sink -> {
            sink.next(Thread.currentThread().getName());
            sink.complete();
        }).publishOn(Schedulers.single())
                .map(x -> String.format("[%s] %s", Thread.currentThread().getName(), x))
                .publishOn(Schedulers.elastic())
                .map(x -> String.format("[%s] %s", Thread.currentThread().getName(), x))
                .subscribeOn(Schedulers.parallel())
                .toStream()
                .forEach(System.out::println);
    }


    private static void logTest() {
        Flux.range(1, 2).log("Range").subscribe(System.out::println);
    }


    private static void hotFlux() {
        final Flux<Long> source = Flux.interval(Duration.ofMillis(1000))
                .take(10)
                .publish()
                .autoConnect();
        source.subscribe();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        source.toStream()
                .forEach(System.out::println);
    }


    private static void pipeLineTest() {
        String[] messageArray = {"Hello", "Reactor", "in", "Action"};
//        Flux<String> ringBuffer = Flux.fromArray(messageArray)
//                .map((String msg)-> msg+"__")
//                .map((String msg)-> msg.toUpperCase())
//                .subscribe(System.out::println);

        Flux.fromArray(messageArray)
                .map((msg) -> msg.toUpperCase())
                .map((msg) -> msg + "--")

                .subscribe(System.out::println);


    }


    public static void main(String[] args) {
        Operators demo = new Operators();
        demo.intervalTest();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


