package com.ab.reactor.processor;

/**
 * @author xiaozhao
 * @date 2019/4/148:23 PM
 */
public class Message {
    private String head;
    private String body;

    public Message(String head, String body) {
        this.head = head;
        this.body = body;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Message{" +
                "head='" + head + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    public static Message HTTP_MESSAGE = new Message("http", "Hello World from http");
    public static Message SOCKET_MESSAGE = new Message("socket", "Hello World from socket");
}
