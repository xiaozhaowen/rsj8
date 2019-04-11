package com.ab.reactor.flux;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

/**
 * @author xiaozhao
 * @date 2019/4/39:50 AM
 */
public class Start {

    /**
     * 创建简单队列的Flux
     * 创建后得到了一个队列，然后订阅来触发执行，消费的形式是输出到控制台
     */
    private static void simpleFlux() {
//        Flux.just("Hello", "World").subscribe(System.out::println);

//        String[] arr={"Hello","World","Array"};
//        Flux.fromArray(arr).subscribe(System.out::println);

//        Flux.empty().subscribe(System.out::println);


//        Flux.range(1,10).subscribe(System.out::println);


        Flux.interval(Duration.of(10, ChronoUnit.SECONDS)).subscribe(System.out::println);

    }


    /**
     * 序列的生成需要复杂的逻辑:generate
     */
    private static void generateFlux() {

        /**
         * next()方法只能最多被调用一次
         * complete方法用于结束序列，否则序列无限
         */

        Flux.generate(sink -> {
            sink.next("Hello");
            sink.complete();
        }).subscribe(System.out::println);


        /**
         * 第二个序列的生成逻辑中的状态对象是一个 ArrayList 对象。
         * 实际产生的值是一个随机数。产生的随机数被添加到 ArrayList 中。当产生了 10 个数时，通过 complete()方法来结束序列。
         */
        final Random random = new Random();
        Flux.generate(ArrayList::new, (list, sink) -> {
            int value = random.nextInt(100);
            list.add(value);
            sink.next(value);
            if (list.size() == 10) {
                sink.complete();
            }
            return list;
        }).subscribe(System.out::println);
    }


    /**
     * 序列的生成需要复杂的逻辑:create
     */
    private static void createFlux() {
        Flux.create(sink -> {
            for (int i = 0; i < 10; i++) {
                sink.next(i);
            }
            sink.complete();
        }).subscribe(System.out::println);
    }


    /**
     * Mono示例
     */
    private static void monoTest() {
        Mono.fromSupplier(() -> "Hello").subscribe(System.out::println);
        Mono.justOrEmpty(Optional.of("Hello")).subscribe(System.out::println);
        Mono.create(sink -> sink.success("Hello")).subscribe(System.out::println);
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
                .map((msg)->msg.toUpperCase())
                .map((msg)->msg+"--")

                .subscribe(System.out::println);


    }


    public static void main(String[] args) {
        pipeLineTest();
    }
}
