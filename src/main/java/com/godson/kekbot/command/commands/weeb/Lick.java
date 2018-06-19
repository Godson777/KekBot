package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Lick extends WeebCommand.MentionCommand {

    public Lick(WeebApi api) {
        super(api);
        name = "lick";
        description = "Licks a person";
        usage.add("lick <@user>");
        type = "lick";
        message = "command.weeb.lick";
    }

}
