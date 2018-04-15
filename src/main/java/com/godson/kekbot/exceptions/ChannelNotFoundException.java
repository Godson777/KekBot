package com.godson.kekbot.exceptions;

public class ChannelNotFoundException extends RuntimeException {
    public ChannelNotFoundException() {}

    public ChannelNotFoundException(String message) {
        super(message);
    }
}
