package com.ab.java8.lambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author xiaozhao
 * @date 2019/4/154:28 PM
 */
public class FunctionDemo {

    /**
     * @param list
     * @param f
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> List<R> map(List<T> list, Function<T, R> f) {
        List<R> result = new ArrayList<>();
        for (T s : list) {
            result.add(f.apply(s));
        }
        return result;
    }

    public static void main(String[] args) {
        List<Integer> lenList = map(Arrays.asList("Java", "Python"), (s) -> s.length());
        System.out.println(lenList);
    }
}
