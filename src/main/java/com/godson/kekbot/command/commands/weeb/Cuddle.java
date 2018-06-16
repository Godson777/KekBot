package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Cuddle extends WeebCommand.MentionCommand {

    public Cuddle(WeebApi api) {
        super(api);
        name = "cuddle";
        description = "Cuddles a person.";
        usage.add("cuddle <@user>");
        type = "cuddle";
        message = "command.weeb.cuddle";
    }
}
