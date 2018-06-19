package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Pat extends WeebCommand.MentionCommand {

    public Pat(WeebApi api) {
        super(api);
        name = "pat";
        description = "Pats a person.";
        usage.add("pat <@user>");
        type = "pat";
        message = "command.weeb.pat";
    }
}
