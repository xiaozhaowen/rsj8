package com.ab.java8.entity;

import java.util.Arrays;
import java.util.List;

/**
 * 数据仓库，快捷返回一些模拟数据
 *
 * @author xiaozhao
 * @date 2019/4/198:34 AM
 */
public class Repository {
    public static List<Dish> getDishList() {
        return Arrays.asList(
                new Dish("猪肉", false, 800, Type.MEAT),
                new Dish("牛肉", false, 700, Type.MEAT),
                new Dish("鸡肉", false, 400, Type.MEAT),
                new Dish("炸土豆片", true, 530, Type.OTHER),
                new Dish("米饭", true, 350, Type.OTHER),
                new Dish("水果", true, 120, Type.OTHER),
                new Dish("匹萨", true, 550, Type.OTHER),
                new Dish("大虾", false, 300, Type.FISH),
                new Dish("鲑鱼", false, 450, Type.FISH)
        );
    }

    public static List<Transaction> getTransactionList() {
        Trader raoul = new Trader("Raoul", "Cambridge");
        Trader mario = new Trader("Mario", "Milan");
        Trader alan = new Trader("Alan", "Cambridge");
        Trader brian = new Trader("Brian", "Cambridge");
        List<Transaction> transactions = Arrays.asList(
                new Transaction(brian, 2011, 300),
                new Transaction(raoul, 2012, 1000),
                new Transaction(raoul, 2011, 400),
                new Transaction(mario, 2012, 710),
                new Transaction(mario, 2012, 700),
                new Transaction(alan, 2012, 950)
        );
        return transactions;
    }
}
