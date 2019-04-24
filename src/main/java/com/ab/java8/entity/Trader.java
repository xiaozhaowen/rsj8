package com.ab.java8.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author xiaozhao
 * @date 2019/4/2010:58 PM
 */
@Data
@AllArgsConstructor
public class Trader {
    private final String name;
    private final String city;
}
