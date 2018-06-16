package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Dab extends WeebCommand {

    public Dab(WeebApi api) {
        super(api);
        name = "dab";
        description = "<o/";
        usage.add("dab");
        type = "dab";
        message = "command.weeb.dab";
    }

}
