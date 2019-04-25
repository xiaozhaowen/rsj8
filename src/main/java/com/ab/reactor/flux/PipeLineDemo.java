package com.ab.reactor.flux;

import com.ab.reactor.entity.Book;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 流水线正常结束和异常的示例
 *
 * @author xiaozhao
 * @date 2019/4/510:40 PM
 */
public class PipeLineDemo {

    private void pipeLine() {
        List<Book> books = new ArrayList<>(3);
        books.add(new Book("Java", 80));
        books.add(new Book("Go", 120));
        books.add(new Book("Python", 60));

        Flux<Book> bookFlux = Flux.fromIterable(books)
                .filter(book -> book.getPrice() < 100)
                .map(book -> {
                    book.setName("《" + book.getName() + "》");
                    return book;
                });

        bookFlux.subscribe(book -> System.out.println(book.getName()));
    }


    /**
     * 流水线正常结束
     */
    private void completeOk() {

        Flux<String> flux = Flux.fromIterable(getList());
        flux.map(item -> {
            if ("Ruby".equals(item)) {

            }
            return item;
        })
                .subscribe(System.out::println);
    }

    /**
     * 前3个可以成功处理，第4个发生错误，然后流水线停止，第5个也就不执行了
     */
    private void errorOnTheForth() {
        Flux.fromIterable(getList())
                .map(book -> {
                    if ("Kotlin".equals(book)) {
                        throw new RuntimeException("书缺失封面");
                    }
                    return book;
                })
                .subscribe(System.out::println);
    }

    /**
     * 处理流水线的错误，处理第4个的错误，但是流水线依然停止
     */
    private void errorHandle() {
        Flux.fromIterable(getList())
                .map(book -> {
                    if ("Kotlin".equals(book)) {
                        throw new RuntimeException("书缺失封面");
                    }
                    return book;
                })
                .subscribe(System.out::println, e -> {
                    System.out.println(e);
                });
    }


    private void continueThePipeLine() {
        Flux.fromIterable(getList())
                .map(book -> {
                    if ("Kotlin".equals(book)) {
                        throw new RuntimeException("书缺失封面");
                    }
                    return book;
                })
                .onErrorContinue((e, o) -> {
                    System.out.println("------错误继续 BEGIN------");
                    System.out.println(e);
                    System.out.println(o);
                    System.out.println("------错误继续 END------");
                })
                .subscribe(System.out::println, e -> {
                    System.out.println("消费者打印错误：" + e);
                });
    }

    private List<String> getList() {
        return Arrays.asList("Java", "Python", "Ruby", "Kotlin", "Swift");
    }


    public static void main(String[] args) {
        PipeLineDemo pipeLineDemo = new PipeLineDemo();
        pipeLineDemo.pipeLine();
    }
}
