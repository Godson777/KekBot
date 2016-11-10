package com.godson.kekbot.Exceptions;

public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException() {}

    public MessageNotFoundException(String message) {
        super(message);
    }
}
