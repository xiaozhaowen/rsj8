package com.ab.reactor.flux;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Flux的几种创建形式
 *
 * @author xiaozhao
 * @date 2019/4/39:50 AM
 */
public class Create {

    /**
     * 简单创建：
     * 1）just
     * 2) fromArray
     * 3) empty
     * 4) range
     * 5) interval
     * 6) fromIterable
     */
    private static void simpleCreate() {

//        Flux.just("OneElement").subscribe(System.out::println);
//        Flux.just("Hello", "World").subscribe(System.out::println);


//        String[] arr={"Hello","World","Array"};
//        Flux.fromArray(arr).subscribe(System.out::println);

        /**
         * empty不会推送任何元素，只有一个完成的事件
         */
//        Flux.empty().subscribe(System.out::println);

//        Flux.range(1,10).subscribe(System.out::println);

//        Flux.interval(Duration.of(10, ChronoUnit.SECONDS)).subscribe(System.out::println);

//        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
//        Flux.fromIterable(list)
//                .map(x -> x * 2)
//                .subscribe(System.out::println);


        Flux.just("ABC", null)
                .filter(x -> x != null)
                .subscribe(System.out::println);
    }

    /**
     * 使用 generate(Consumer<SynchronousSink<T>> generator) 形式
     */
    private static void generateConsumerForm() {
        /**
         * next()方法只能最多被调用一次
         * complete方法用于结束序列，否则序列无限
         */
        Flux.generate(sink -> {
            sink.next("Hello");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 如果不调用complete的话，则序列无限
//            sink.complete();
        }).subscribe(System.out::println);
    }

    private static void generateInitAndState_0() {
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
     * 第一个参数用于保存状态。2个作用：初始化和每个步骤的结果存储。初始值为0，然后在每次操作后供下一次使用
     * 第二个参数是具体的生成逻辑，使用next，error，complete 3个方法来生成
     */
    private static void generateInitAndState_1() {
        Flux<String> flux = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3 * state);
                    if (state == 10) {
                        sink.complete();
                    }
                    return state + 1;
                });
        flux.subscribe(System.out::println);
    }

    /**
     * 编程方式生成序列
     */
    private static void generateInitAndState_2() {
        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) {
                        sink.complete();
                    }
                    return state;
                });
        flux.subscribe(System.out::println);
    }

    /**
     * 编程方式生成序列
     * 当state需要一些清理工作的时候使用这种方式，例如关闭数据库连接
     */
    private static void generateInitAndState_3() {
        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) {
                        sink.complete();
                    }
                    return state;
                },
                (state) -> System.out.println("state:的清理工作 " + state));
        flux.subscribe(System.out::println);
    }

    /**
     * 异步创建和多线程
     * 还可以指定背压的策略
     */
    private void createTest() {
        Flux.create((FluxSink<Integer> sink) -> {
            for (int i = 0; i < 10; i++) {
                sink.next(i);
            }
            sink.complete();
        }).subscribe(System.out::println);
    }

    /**
     * 异步单线程
     * TODO 示例
     */
    private void push() {

    }


    /**
     * handle
     */
    private static void handle() {
        Flux<String> alphabet = Flux.just(-1, 30, 13, 9, 20)
                .handle((i, sink) -> {
                    String letter = alphabet(i);
                    if (letter != null) {
                        sink.next(letter);
                    }
                });

        alphabet.subscribe(System.out::println);
    }

    /**
     * 把数字转换为对应的字母，如果超出字母的数字范围，则返回null
     *
     * @param letterNumber
     * @return
     */
    private static String alphabet(int letterNumber) {
        if (letterNumber < 1 || letterNumber > 26) {
            return null;
        }
        int letterIndexAscii = 'A' + letterNumber - 1;
        return "" + (char) letterIndexAscii;
    }


    public static void main(String[] args) {
        handle();
    }
}
