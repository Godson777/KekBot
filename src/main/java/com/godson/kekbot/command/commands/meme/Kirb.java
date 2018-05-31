package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;

import java.io.File;
import java.util.Random;

public class Kirb extends Command {

    public Kirb() {
        name = "kirb";
        description = "POYO";
        usage.add("kirb");
        category = CommandCategories.meme;
        cooldownScope = CooldownScope.USER_GUILD;
        cooldown = 5;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        File kirbs[] = new File("resources/kirb").listFiles();
        Random random = new Random();
        int index = random.nextInt(kirbs.length);
        event.getChannel().sendTyping().queue();
        event.getChannel().sendFile(kirbs[index], kirbs[index].getName(), null).queue();
    }
}
