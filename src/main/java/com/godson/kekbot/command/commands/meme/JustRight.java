package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;

import java.io.File;
import java.util.Random;

public class JustRight extends Command {

    public JustRight() {
        name = "justright";
        description = "When you need Discord to be just right...";
        usage.add("justright");
        category = CommandCategories.meme;
        cooldownScope = CooldownScope.USER_GUILD;
        cooldown = 5;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        File justRights[] = new File("resources/justright").listFiles();
        Random random = new Random();
        int index = random.nextInt(justRights.length);
        event.getChannel().sendTyping().queue();
        event.getChannel().sendFile(justRights[index], justRights[index].getName(), null).queue();
    }
}
