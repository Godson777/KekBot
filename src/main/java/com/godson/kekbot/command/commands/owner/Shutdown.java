package com.godson.kekbot.command.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.XMLUtils;


public class Shutdown {
    public static Command shutdown = new Command("shutdown")
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().getId().equals(XMLUtils.getBotOwner())) {
                        context.getMessage().getChannel().sendMessage("That's all from me! Hope you had a gay old time!");
                        KekBot.client.shutdown();
                        System.exit(0);
                }
            });
}
