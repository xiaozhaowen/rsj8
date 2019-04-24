package com.ab.java8.lambda;

import com.ab.java8.entity.Apple;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 方法引用
 *
 * @author xiaozhao
 * @date 2019/4/154:49 PM
 */
public class MethodReference {

    public static String hello(String name) {
        return "Hello " + name;
    }

    private void staticMethodTest() {
        Function<String, Integer> function = (value) -> Integer.parseInt(value);
        int a = function.apply("1");

//        Function<String,Integer> function2 = (value)->Integer::parseInt;
//        System.out.println(a);
    }


    //------------------------构造函数---------------------------

    /**
     * 构造函数的方法引用
     */
    private void constructTest() {
        // 无参构造函数
        Supplier<Apple> appleSupplier = Apple::new;
        Apple apple = appleSupplier.get();
        System.out.println(apple);

        // 1个参构造函数
        Function<String, Apple> colorFunc = Apple::new;
        Apple greenApple = colorFunc.apply("Green");
        System.out.println(greenApple);

        // 2个参构造函数
        BiFunction<String, Integer, Apple> colorAndWeightFunc = Apple::new;
        Apple yellowWeightApple = colorAndWeightFunc.apply("Yellow", 200);
        System.out.println(yellowWeightApple);

        // 3个参构造函数
        ThreeParamFunction<String, Integer, String, Apple> threeParamFunction = Apple::new;
        Apple fullPropertyApple = threeParamFunction.apply("Gold", 300, "Beijing");
        System.out.println(fullPropertyApple);

    }

    public static void main(String[] args) {
        MethodReference demo = new MethodReference();
        demo.constructTest();
    }

    /**
     * 代表了一个函数，接受3个参数并且生成一个结果，描述了方法签名
     *
     * @param <T>
     * @param <U>
     * @param <V>
     * @param <R>
     */
    @FunctionalInterface
    interface ThreeParamFunction<T, U, V, R> {

        /**
         * 接收参数并且返回
         *
         * @param t 第1个参数
         * @param u 第2个参数
         * @param v 第3个参数
         * @return
         */
        R apply(T t, U u, V v);
    }

}


