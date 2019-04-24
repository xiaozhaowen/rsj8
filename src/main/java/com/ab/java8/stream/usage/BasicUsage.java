package com.ab.java8.stream.usage;

import com.ab.java8.entity.Dish;
import com.ab.java8.entity.Repository;
import com.ab.java8.entity.Transaction;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 流的基本使用
 *
 * @author xiaozhao
 * @date 2019/4/1911:07 AM
 */
public class BasicUsage {

    public static void main(String[] args) {
        BasicUsage usage = new BasicUsage();
        usage.practise();
    }


    //----------------------------转换------------------------------

    /**
     * 过滤出所有的素菜
     */
    private void filterTest() {
        List<Dish> dishList = Repository.getDishList();
        List<String> vegetarianNames = dishList.stream()
                .filter(dish -> dish.isVegetarian())
                .map(dish -> dish.getName())
                .collect(Collectors.toList());
        System.out.println(vegetarianNames);
    }

    /**
     * 得到所有不同的偶数
     */
    private void distinctTest() {
        List<Integer> numbers = Arrays.asList(1, 2, 1, 3, 3, 2, 4);
        List<Integer> result = numbers.stream()
                .filter(x -> x % 2 == 0)
                .distinct()
                .collect(Collectors.toList());
        System.out.println(result);
    }

    /**
     * 截短流
     */
    private void limitTest() {
        List<String> dishList = Repository.getDishList()
                .stream()
                .filter(dish -> dish.getCalories() > 300)
                .map(dish -> dish.getName())
                .limit(3)
                .collect(Collectors.toList());
        System.out.println(dishList);
    }

    /**
     * 跳过前多少个元素
     */
    private void skipTest() {
        List<String> nameList = Repository.getDishList()
                .stream()
                .filter(dish -> dish.getCalories() > 400)
                .map(dish -> dish.getName())
                .skip(2)
                .collect(Collectors.toList());
        System.out.println(nameList);
    }

    /**
     * 映射，提取
     */
    private void mapTest() {
        List<String> words = Arrays.asList("Java 8", "Lambdas", "In", "Action");
        List<Integer> wordLengths = words.stream()
                .map(String::length)
                .collect(Collectors.toList());
        System.out.println(wordLengths);
    }

    /**
     * 返回一个平方
     */
    private void mapTest2() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        list.stream()
                .map(x -> x * x)
                .forEach(System.out::println);
    }

    /**
     * 流的扁平化，接受一个元素，然后返回一个流
     */
    private void flatMapTest() {
        String[] words = {"Hello", "world"};
        List<String> list = Arrays.stream(words)
                .map(word -> word.split(""))
                .flatMap(charArray -> Arrays.stream(charArray))
                .distinct()
                .collect(Collectors.toList());
        System.out.println(list);
    }

    /**
     * 返回2个数组的数对
     */
    private void flatMapTest2() {
        List<Integer> number1 = Arrays.asList(1, 2, 3);
        List<Integer> number2 = Arrays.asList(3, 4);
        number1.stream()
                .flatMap(x -> number2.stream()
                        .filter(y -> (x + y) % 3 == 0)
                        .map(y -> new int[]{x, y})
                )
                .forEach(arr -> {
                    System.out.println(arr[0] + "," + arr[1]);
                });
    }

    //--------------------------查找------------------------------

    /**
     * 查找
     */
    private void matchTest() {
        // 菜单中是否有素菜
        List<Dish> dishes = Repository.getDishList();
        boolean hasVegetarian = dishes.stream()
                .anyMatch(Dish::isVegetarian);
        if (hasVegetarian) {
            System.out.println("菜单中有素菜");
        }

        // 菜单中是否全部都是健康的：低热量的
        boolean isHealthy = dishes.stream()
                .allMatch(dish -> dish.getCalories() < 1000);
        if (isHealthy) {
            System.out.println("所有的菜的热量都是小于1000的");
        }


        boolean isHealthy2 = dishes.stream()
                .noneMatch(dish -> dish.getCalories() > 1000);
        if (isHealthy2) {
            System.out.println("所有的菜的热量没有大于1000的");
        }
    }

    private void findTest() {
        List<Dish> dishes = Repository.getDishList();

        // 找到任一个素菜
        dishes.stream()
                .filter(Dish::isVegetarian)
                .findAny()
                .ifPresent(dish -> System.out.println(dish.getName()));


        // 找到第一个符合要求的元素
        List<Integer> numberList = Arrays.asList(1, 2, 3, 4, 5);
        Optional<Integer> firstSquareDivisibleByThree =
                numberList.stream()
                        .map(x -> x * x)
                        .filter(x -> x % 3 == 0)
                        .findFirst();
        if (firstSquareDivisibleByThree.isPresent()) {
            System.out.println(firstSquareDivisibleByThree.get());
        }
    }

    //--------------------------归约------------------------------

    /**
     * 归约操作，就是把流中的元素累积操作：
     * 1）求和
     * 2）找最大
     * 3）找最小
     * 4）统计个数
     */
    private void reduceTest() {
        List<Integer> numberList = Arrays.asList(1, 2, 3, 4, 5);

        // 1)求和
        int sum = numberList.stream()
                .reduce(0, (a, b) -> a + b);
        System.out.println(sum);

        int sum2 = numberList.stream()
                .reduce(0, Integer::sum);
        System.out.println(sum2);

        Optional<Integer> sum3 = numberList.stream()
                .reduce((a, b) -> (a + b));
        if (sum3.isPresent()) {
            System.out.println(sum3.get());
        }


        // 最大值、最小值
        Optional<Integer> max = numberList.stream()
                .reduce(Integer::max);
        Optional<Integer> min = numberList.stream()
                .reduce(Integer::min);
        System.out.println("最大值：" + max.get());
        System.out.println("最小值：" + min.get());


        // 统计个数
        int count = numberList.stream()
                .map(x -> 1)
                .reduce(0, (a, b) -> a + b);
        long count2 = numberList.stream().count();
        System.out.println("统计个数第一种：" + count);
        System.out.println("统计个数第二种：" + count2);
    }


    //--------------------------实战------------------------------

    private void practise() {
        List<Transaction> transactions = Repository.getTransactionList();

        // 2011年的所有交易，并且按照交易额排序
        List<Transaction> all_2011_sort_by_value = transactions.stream()
                .filter(x -> x.getYear() == 2011)
                .sorted((a, b) -> {
                    if (a.getValue() == b.getValue()) {
                        return 0;
                    }
                    if (a.getValue() > b.getValue()) {
                        return 1;
                    } else {
                        return -1;
                    }
                })
                .collect(Collectors.toList());
        System.out.println("2011年的交易：");
        System.out.println(all_2011_sort_by_value);


        // 所有来自剑桥的交易员，按照姓名排序
        List<Transaction> all_from_combridge = transactions.stream()
                .filter(transaction -> "Cambridge".equals(transaction.getTrader().getCity()))
                .sorted((t1, t2) -> {
                    return t2.getTrader().getName().compareTo(t2.getTrader().getName());
                })
                .collect(Collectors.toList());
        System.out.println("来自康桥的交易员：");
        System.out.println(all_from_combridge);


        // 获取所有的交易员名称，按名称排序
        List<String> allTraderNameList = transactions.stream()
                .map(transaction -> transaction.getTrader().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        System.out.println("所有交易员姓名：");
        System.out.println(allTraderNameList);


        // 是否有来自米兰的交易员
        boolean hasTraderFromMilan = transactions.stream()
                .anyMatch(transaction -> "Milan".equals(transaction.getTrader().getCity()));
        System.out.println("有来自米兰的交易员：" + hasTraderFromMilan);


//        int max =
    }
}
