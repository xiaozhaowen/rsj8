package com.ab.reactor.flux;

import reactor.core.publisher.Flux;

/**
 * @author xiaozhao
 * @date 2019/4/41:32 PM
 */
public class ReactorDemo {


    public void pipeLineTest(ReactorDemo reactorDemo) {
        /**
         * 待处理消息
         */
        String[] messageArray = {"_Hello_", "_Reactor_", "_in_", "_Action_"};

        /**
         * 构建流水线
         */
        Flux<String> pipeLine = Flux.fromArray(messageArray)
                .map(reactorDemo::unzip)
                .map(reactorDemo::decode);

        /**
         * 开始订阅消费
         */
        pipeLine.subscribe((String finalMsg) -> convertToPerson(finalMsg));
    }


    /**
     * 模拟解压缩
     *
     * @param rawMessage
     * @return
     */
    public String unzip(String rawMessage) {
        return rawMessage.replaceAll("_", "");
    }

    /**
     * 模拟解密
     *
     * @param rawMessage
     * @return
     */
    public String decode(String rawMessage) {
        return rawMessage.toUpperCase();
    }

    /**
     * 模拟转换
     *
     * @param name
     * @return
     */
    private Person convertToPerson(String name) {
        Person person = new Person(name, 20);
        System.out.println(person);
        return person;
    }


    public static void main(String[] args) {
        ReactorDemo demo = new ReactorDemo();
        demo.pipeLineTest(demo);
    }
}

/**
 * 业务模块需要的POJO类
 */
class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}

@FunctionalInterface
interface Unzip {
    String unzip(String raw);
}

class MyUnzip implements Unzip {
    @Override
    public String unzip(String raw) {
        return raw.replaceAll("_", "");
    }
}


