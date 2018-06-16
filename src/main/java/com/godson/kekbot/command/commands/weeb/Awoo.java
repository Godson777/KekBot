package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Awoo extends WeebCommand {

    public Awoo(WeebApi api) {
        super(api);
        name = "awoo";
        description = "AWOOOOOOOOO";
        usage.add("awoo");
        type = "awoo";
        message = "command.weeb.awoo";
    }

}
