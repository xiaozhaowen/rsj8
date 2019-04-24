package com.ab.java8.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author xiaozhao
 * @date 2019/4/198:30 AM
 */
@Data
@AllArgsConstructor
public class Dish {
    private final String name;
    private final boolean vegetarian;
    private final int calories;
    private final Type type;
}

