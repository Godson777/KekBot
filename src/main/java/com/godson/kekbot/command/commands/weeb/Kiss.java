package com.godson.kekbot.command.commands.weeb;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import me.duncte123.weebJava.models.WeebApi;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

import java.io.File;
import java.util.Random;

public class Kiss extends WeebCommand.MentionCommand {

    public Kiss(WeebApi weebApi) {
        super(weebApi);
        name = "kiss";
        description = "Kisses a person.";
        usage.add("kiss <@user>");
        type = "kiss";
        message = "command.weeb.kiss";
    }

}
