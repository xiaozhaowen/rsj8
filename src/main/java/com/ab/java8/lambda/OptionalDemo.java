package com.ab.java8.lambda;

import com.ab.java8.entity.Apple;

import java.util.*;

/**
 * @author xiaozhao
 * @date 2019/4/289:28 AM
 */
public class OptionalDemo {


    /**
     * 基本用法
     */
    private static void baseUsage() {
        // 1）创建一个空对象
        Optional<Car> emptyCar = Optional.empty();

        // 2）创建一个非空对象
        Car car = new Car();
        Optional<Car> hasCar = Optional.of(car);

        // 3）创建一个可以为空的对象
        Optional<Car> canBeEmpty = Optional.ofNullable(null);

        //4）map使用:map函数在Optional为空的时候，什么也不做;有值的话才操作
        Insurance anBang = new Insurance("AnBang");
        Optional<Insurance> insuranceOptional = Optional.ofNullable(anBang);
        Optional<String> optionalName = insuranceOptional.map(insurance -> insurance.getName());
        optionalName.ifPresent(name -> System.out.println(name));

    }

    /**
     * flatMap示例
     */
    private static void flatMapTest() {
        Insurance ab = new Insurance("AnBang");

        Car car = new Car();
        car.setInsurance(Optional.ofNullable(ab));

        Person kobe = new Person();
        kobe.setCar(Optional.ofNullable(car));

        Optional<Person> personOptional = Optional.of(kobe);

        // 下面代码会出现编译错误
//        Optional<String> name = personOptional.map(Person::getCar)
//                                .map(Car::getInsurance)
//                                .map(Insurance::getName);

        String insuranceName = personOptional.flatMap(Person::getCar)
                .flatMap(Car::getInsurance)
                .map(Insurance::getName)
                .orElse("未知公司");
        System.out.println(insuranceName);


    }


    private static void test() {
        List<Apple> list = Arrays.asList(
                new Apple("Red"),
                new Apple("Yellow"),
                new Apple("Blue")
        );

        String sumNameString = Optional.ofNullable(list)
                .map(myList -> {
                    System.out.println("翻转列表");
                    Collections.reverse(myList);
                    return myList;
                })
                .orElseGet(() -> {
                    System.out.println("延迟执行");
                    List<Apple> defaultList = new ArrayList<>(1);
                    defaultList.add(new Apple());
                    return defaultList;
                })
                .stream()
                .map(apple -> apple.getColor())
                .reduce("ALL:", (c1, c2) -> c1 + "--" + c2);

        System.out.println(sumNameString);
//        sumNameString.ifPresent(x -> System.out.println(x));
    }

    public static void main(String[] args) {
        flatMapTest();
    }
}


class Person {
    /**
     * 人可能有车也可能没有车
     */
    private Optional<Car> car;

    public Optional<Car> getCar() {
        return car;
    }

    public void setCar(Optional<Car> car) {
        this.car = car;
    }
}

class Car {
    /**
     * 车可能入了保险，也可能没有保险
     */
    private Optional<Insurance> insurance;

    public Optional<Insurance> getInsurance() {
        return insurance;
    }

    public void setInsurance(Optional<Insurance> insurance) {
        this.insurance = insurance;
    }
}

class Insurance {
    /**
     * 保险公司必须有名称
     */
    private String name;

    public Insurance(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
