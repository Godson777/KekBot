package com.godson.kekbot.command.commands.weeb;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import me.duncte123.weebJava.exceptions.ImageNotFoundException;
import me.duncte123.weebJava.models.WeebApi;
import me.duncte123.weebJava.types.NSFWType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

import java.io.File;
import java.util.Random;

import static com.godson.kekbot.KekBot.weebApi;

public class Hug extends WeebCommand.MentionCommand {

    public Hug(WeebApi api) {
        super(api);
        name = "hug";
        description = "Hugs a person.";
        usage.add("hug <@user>");
        type = "hug";
        message = "command.weeb.hug";
    }
}
