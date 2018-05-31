package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Shrug extends WeebCommand {

    public Shrug(WeebApi api) {
        super(api);
        name = "shrug";
        description = "¯\\_(ツ)_/¯";
        usage.add("shrug");
        type = "shrug";
        message = "command.weeb.shrug";
    }

}
