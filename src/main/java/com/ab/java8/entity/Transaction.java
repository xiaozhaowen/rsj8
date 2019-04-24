package com.ab.java8.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author xiaozhao
 * @date 2019/4/2010:59 PM
 */
@Data
@AllArgsConstructor
public class Transaction {
    private final Trader trader;
    private final int year;
    /**
     * 交易额
     */
    private final int value;
}
