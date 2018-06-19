package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Poke extends WeebCommand.MentionCommand {

    public Poke(WeebApi api) {
        super(api);
        name = "poke";
        description = "Lets you poke a user and annoy them. >:3";
        usage.add("poke <@user>");
        type = "poke";
        message = "command.weeb.poke";
    }
}
