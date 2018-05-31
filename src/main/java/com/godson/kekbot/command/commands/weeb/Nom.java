package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Nom extends WeebCommand.MentionCommand {

    public Nom(WeebApi api) {
        super(api);
        name = "nom";
        description = "nomnomnomnomnomnomnomnom";
        usage.add("nom <@user>");
        type = "nom";
        message = "command.weeb.nom";
    }
}
