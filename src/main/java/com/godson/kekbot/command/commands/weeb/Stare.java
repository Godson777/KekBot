package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Stare extends WeebCommand {

    public Stare(WeebApi api) {
        super(api);
        name = "stare";
        description = "\uD83D\uDC40";
        usage.add("stare");
        type = "stare";
        message = "command.weeb.stare";
    }

}
