package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;

import java.io.File;
import java.util.Random;

public class YouTried extends Command {

    public YouTried() {
        name = "youtried";
        description = "Here's a gold star.";
        usage.add("youtried");
        category = CommandCategories.meme;
        cooldownScope = CooldownScope.USER_GUILD;
        cooldown = 5;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        File[] stars = new File("resources/youtried").listFiles();
        Random random = new Random();
        int index = random.nextInt(stars.length);
        event.getChannel().sendTyping().queue();
        event.getChannel().sendFile(stars[index], stars[index].getName()).queue();
    }
}
