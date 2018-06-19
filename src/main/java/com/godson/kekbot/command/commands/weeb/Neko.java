package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Neko extends WeebCommand {

    public Neko(WeebApi api) {
        super(api);
        name = "neko";
        description = "Because we all need nekos in our lives.";
        usage.add("neko");
        type = "neko";
        message = "command.weeb.neko";
    }

}
