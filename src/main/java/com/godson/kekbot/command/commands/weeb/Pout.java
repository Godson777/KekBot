package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Pout extends WeebCommand {

    public Pout(WeebApi api) {
        super(api);
        name = "pout";
        description = ":(";
        usage.add("pout");
        type = "pout";
        message = "command.weeb.pout";
    }

}
