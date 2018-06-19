package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Sleepy extends WeebCommand {

    public Sleepy(WeebApi api) {
        super(api);
        name = "sleepy";
        description = "I'm not sleepy, YOU'RE sleepy!";
        usage.add("sleepy");
        type = "sleepy";
        message = "command.weeb.sleepy";
    }

}
