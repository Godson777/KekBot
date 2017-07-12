package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;

public class Lmgtfy {
    public static Command lmgtfy = new Command("lmgtfy")
            .withAliases("lmg")
            .withCategory(CommandCategory.FUN)
            .withDescription("Allows the user to perform a google search for an idiot.")
            .withUsage("{p}lmgtfy <query>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                if (rawSplit.length == 1) {
                    context.getTextChannel().sendMessage("You haven't given me anything to search for!").queue();
                } else if (rawSplit.length == 2) {
                    String search = rawSplit[1].replace(" ", "+").replace("@everyone", "@\u200Beveryone");
                    context.getTextChannel().sendMessage("http://lmgtfy.com/?q=" + search).queue();
                }
            });
}
