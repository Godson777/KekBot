package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Smug extends WeebCommand {

    public Smug(WeebApi api) {
        super(api);
        name = "smug";
        description = "Because all you weebs ever do is smug >:C";
        usage.add("smug");
        type = "smug";
        message = "command.weeb.smug";
    }

}
