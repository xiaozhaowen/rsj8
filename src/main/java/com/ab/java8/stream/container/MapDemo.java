package com.ab.java8.stream.container;

import com.ab.java8.entity.Apple;

import java.util.HashMap;

/**
 * @author xiaozhao
 * @date 2019/4/2610:08 AM
 */
public class MapDemo {
    public static void main(String[] args) {
        HashMap<String, Apple> appleHashMap = new HashMap<>(8);

        appleHashMap.put("red", new Apple("Red",100));
        appleHashMap.put("Yellow", new Apple("Banana",100));

        appleHashMap.computeIfAbsent("Yellow",(color)->{
           return new Apple(color,200);
        });

        System.out.println(appleHashMap.get("Yellow"));




    }
}
