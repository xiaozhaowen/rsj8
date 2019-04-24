package com.ab.java8.stream.container;

import com.ab.java8.entity.Repository;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author xiaozhao
 * @date 2019/4/198:29 AM
 */
public class ContainerDemo {

    /**
     * 来自List的流
     */
    private void streamFromList() {
        List<String> books = Arrays.asList("Java", "Ruby", "Python");
        books.stream()
                .map(book -> book.toUpperCase())
                .forEach(System.out::println);
    }

    /**
     * 来自Set的流
     */
    private void streamFromSet() {
        Set<String> bookSet = new HashSet<>(3);
        bookSet.add("Java");
        bookSet.add("Ruby");
        bookSet.add("Python");

        bookSet.stream()
                .map(book -> book.toUpperCase())
                .forEach(System.out::println);
    }


    /**
     * 来自数组的流
     */
    private void streamFromArray() {
        String[] bookArray = {"Java", "Python", "Ruby"};
        Stream<String> stringStream = Arrays.stream(bookArray);
        stringStream.map(x -> x.toUpperCase())
                .forEach(System.out::println);
    }

    /**
     * 流处理map
     */
    private void streamFromMap() {
        Map<String, Integer> bookMap = new HashMap<>(4);
        bookMap.put("Java", 100);
        bookMap.put("Python", 200);
        bookMap.put("Ruby", 300);
        bookMap.entrySet()
                .stream()
                .map(kv -> kv.getKey())
                .forEach(System.out::println);
    }

    /**
     * 选出热量超过300的3个菜肴，不是最高的3个
     */
    private void threeDishBigThan300() {
        List<String> threeHighCaloricDishNames = Repository.getDishList()
                .stream()
                .filter(dish -> dish.getCalories() > 300)
//                .sorted(dish -> dish.getCalories())
                .map(dish -> dish.getName())
                .limit(3)
                .collect(toList());

        System.out.println(threeHighCaloricDishNames);
    }

    /**
     * 流只可以消费一次，理解为时间线
     */
    private void streamOnlyCanConsumeOnce() {
        List<String> books = Arrays.asList("Java", "Ruby", "Python");
        Stream<String> bookStream = books.stream();
        bookStream.forEach(System.out::println);

        // 抛出异常： stream has already been operated upon or closed
        // bookStream.forEach(System.out::println);
    }


    public static void main(String[] args) {
        ContainerDemo demo = new ContainerDemo();
        demo.streamFromMap();
    }
}
