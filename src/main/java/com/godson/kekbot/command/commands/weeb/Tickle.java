package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Tickle extends WeebCommand.MentionCommand {

    public Tickle(WeebApi api) {
        super(api);
        name = "tickle";
        description = "Tickles a person.";
        usage.add("tickle <@user>");
        type = "tickle";
        message = "command.weeb.tickle";
    }
}
