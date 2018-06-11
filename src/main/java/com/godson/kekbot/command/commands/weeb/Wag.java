package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Wag extends WeebCommand {

    public Wag(WeebApi api) {
        super(api);
        name = "wag";
        description = "AWOOO 2: Electric Boogaloo";
        usage.add("wag");
        type = "wag";
        message = "command.weeb.wag";
    }

}
