package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Bite extends WeebCommand.MentionCommand {

    public Bite(WeebApi api) {
        super(api);
        name = "bite";
        description = "Bites the living HECK out of someone.";
        usage.add("bite <@user>");
        type = "bite";
        message = "command.weeb.bite";
    }
}
