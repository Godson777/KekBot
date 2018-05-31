package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Punch extends WeebCommand.MentionCommand {

    public Punch(WeebApi api) {
        super(api);
        name = "punch";
        description = "Punch someone in the face!";
        usage.add("punch <@user>");
        type = "punch";
        message = "command.weeb.punch";
    }
}
