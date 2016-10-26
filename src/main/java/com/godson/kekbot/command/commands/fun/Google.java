package com.godson.kekbot.command.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;

public class Google {
    public static Command google = new Command("google")
            .withCategory(CommandCategory.FUN)
            .withDescription("Performs a google search for the user.")
            .withUsage("{p}google <query>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getContent().split(" ", 2);
                if (rawSplit.length == 1) {
                    context.getTextChannel().sendMessage("You haven't given me anything to search for!");
                } else if (rawSplit.length == 2) {
                    String search = rawSplit[1].replace(" ", "+");
                    context.getTextChannel().sendMessage("http://google.com/#q=" + search);
                }
            });
}
