package com.ab.java8.lambda;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 消费兰姆达示例
 *
 * @author xiaozhao
 * @date 2019/4/154:23 PM
 */
public class ConsumerDemo {

    /**
     * 消费数据
     *
     * @param list
     * @param consumer
     * @param <T>
     */
    private static <T> void forEach(List<T> list, Consumer<T> consumer) {
        for (T t : list) {
            consumer.accept(t);
        }
    }



    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        forEach(list, (value) -> System.out.println(value * 2));
    }
}
