package com.ab.java8.lambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * 断言示例
 *
 * @author xiaozhao
 * @date 2019/4/154:17 PM
 */
public class PredicateDemo {

    /**
     * 使用断言过滤
     *
     * @param list
     * @param predicate
     * @param <T>
     * @return
     */
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        List<T> results = new ArrayList<>();
        for (T s : list) {
            if (predicate.test(s)) {
                results.add(s);
            }
        }
        return results;
    }

    private void testGenericPredicate() {
        List<String> books = Arrays.asList("Java", "Python", "", "Ruby");
        Predicate<String> nonEmptyStringPredicate = (s) -> !s.isEmpty();
        List<String> result = filter(books, nonEmptyStringPredicate);
        System.out.println(result);
    }

    /**
     * ----------------------------原始类型特化-----------------------------
     */


    private void testPrimitive() {
        IntPredicate predicate = (int i) -> i % 2 == 0;
        System.out.println(predicate.test(1000));

        // 会有装箱
        Predicate<Integer> oddNumbers = (Integer i) -> i % 2 == 1;
        System.out.println(oddNumbers.test(1000));
    }


    public static void main(String[] args) {
        PredicateDemo demo = new PredicateDemo();
        demo.testPrimitive();
    }
}
