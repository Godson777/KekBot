package com.godson.kekbot.command.commands;


import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.EasyMessage;
import sx.blah.discord.handle.obj.IMessage;

import java.util.Random;

public class ping {
    public static Command test = new Command("test")
            .withCategory(CommandCategory.TEST)
            .withDescription("Just a test command.")
            .withUsage("{p}test")
            .caseSensitive(true)
            .onExecuted(context -> {
                Random random = new Random();
                int number = random.nextInt(20);
                if (number < 19) {
                    EasyMessage.send(context.getMessage().getChannel(), "k");
                } else {
                    EasyMessage.send(context.getMessage().getChannel(), "no u");
                }
            });
}
