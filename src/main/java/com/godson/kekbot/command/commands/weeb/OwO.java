package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class OwO extends WeebCommand {

    public OwO(WeebApi api) {
        super(api);
        name = "owo";
        description = "OwO WHAT THE FUCK IS THIS";
        usage.add("owo");
        type = "owo";
        message = "command.weeb.owo";
    }

}
