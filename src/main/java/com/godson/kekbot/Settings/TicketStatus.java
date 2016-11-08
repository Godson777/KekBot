package com.godson.kekbot.Settings;

public enum TicketStatus {
    OPEN("Open"), AWAITING_REPLY("Awaiting Reply"), RECEIVED_REPLY("Recieved Reply");

    private String name;

    TicketStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
