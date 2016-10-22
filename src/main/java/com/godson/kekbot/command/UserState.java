package com.godson.kekbot.command;

public enum UserState {
    TEST_STATE("TEST");

    private String name;

    UserState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
