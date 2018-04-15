package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;

import java.io.File;
import java.util.Random;

public class Lean extends Command {

    public Lean() {
        name = "lean";
        description = "Leans in your discord.";
        usage.add("lean");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        File leans[] = new File("resources/lean").listFiles();
        Random random = new Random();
        int index = random.nextInt(leans.length);
        event.getChannel().sendTyping().queue();
        event.getChannel().sendFile(leans[index], leans[index].getName(), null).queue();
    }
}
