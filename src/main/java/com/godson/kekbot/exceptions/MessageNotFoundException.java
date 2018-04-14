package com.godson.kekbot.exceptions;

public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException() {}

    public MessageNotFoundException(String message) {
        super(message);
    }
}
