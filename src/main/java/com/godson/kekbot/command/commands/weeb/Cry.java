package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Cry extends WeebCommand {

    public Cry(WeebApi api) {
        super(api);
        name = "cry";
        description = ":(((((((";
        usage.add("cry");
        type = "cry";
        message = "command.weeb.cry";
    }

}
