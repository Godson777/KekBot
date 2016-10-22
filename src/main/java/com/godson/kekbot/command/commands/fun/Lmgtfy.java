package com.godson.kekbot.command.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.EasyMessage;
import sx.blah.discord.handle.obj.IChannel;

public class Lmgtfy {
    public static Command lmgtfy = new Command("lmgtfy")
            .withAliases("lmg")
            .withCategory(CommandCategory.FUN)
            .withDescription("Allows the user to perform a google search for an idiot.")
            .withUsage("{p}lmgtfy <query>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getContent().split(" ", 2);
                IChannel channel = context.getMessage().getChannel();
                if (rawSplit.length == 1) {
                    EasyMessage.send(channel, "You haven't given me anything to search for!");
                } else if (rawSplit.length == 2) {
                    String search = rawSplit[1].replace(" ", "+");
                    EasyMessage.send(channel, "http://lmgtfy.com/?q=" + search);
                }
            });
}
