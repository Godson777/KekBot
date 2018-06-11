package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Dance extends WeebCommand {

    public Dance(WeebApi api) {
        super(api);
        name = "dance";
        description = "ᕕ( ᐛ )ᕗ";
        usage.add("dance");
        type = "dance";
        message = "command.weeb.dance";
    }

}
