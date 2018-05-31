package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Deredere extends WeebCommand {

    public Deredere(WeebApi api) {
        super(api);
        name = "deredere";
        description = "Because we couldn't get a tsundere command";
        usage.add("deredere");
        type = "deredere";
        message = "command.weeb.deredere";
    }

}
