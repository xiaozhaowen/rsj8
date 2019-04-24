package com.ab.java8.entity;

/**
 * 苹果的实体类
 *
 * @author xiaozhao
 * @date 2019/4/168:37 AM
 */
public class Apple {
    /**
     * 颜色
     */
    private String color;
    /**
     * 重量
     */
    private int weight;
    /**
     * 产地
     */
    private String addr;

    public Apple() {
        this.color = "Red";
        this.weight = 150;
    }

    public Apple(String color) {
        this.color = color;
    }

    public Apple(String color, int weight) {
        this.color = color;
        this.weight = weight;
    }

    public Apple(String color, int weight, String addr) {
        this.color = color;
        this.weight = weight;
        this.addr = addr;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Apple{" +
                "color='" + color + '\'' +
                ", weight=" + weight +
                ", addr='" + addr + '\'' +
                '}';
    }
}
