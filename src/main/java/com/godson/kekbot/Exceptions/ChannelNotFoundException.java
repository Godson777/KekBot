package com.godson.kekbot.Exceptions;

public class ChannelNotFoundException extends RuntimeException {
    public ChannelNotFoundException() {}

    public ChannelNotFoundException(String message) {
        super(message);
    }
}
