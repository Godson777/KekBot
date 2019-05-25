package com.godson.kekbot.command.commands.weeb;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import me.duncte123.weebJava.models.WeebApi;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

import java.io.File;
import java.util.Random;

public class Slap extends WeebCommand.MentionCommand {

    public Slap(WeebApi weebApi) {
        super(weebApi);
        name = "slap";
        description = "Slaps a person.";
        usage.add(name + " <@user>");
        type = "slap";
        message = "command.weeb.slap";
    }
}
