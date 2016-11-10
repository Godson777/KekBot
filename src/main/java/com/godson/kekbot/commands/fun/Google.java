package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;

public class Google {
    public static Command google = new Command("google")
            .withCategory(CommandCategory.FUN)
            .withDescription("Performs a google search for the user.")
            .withUsage("{p}google <query>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                if (rawSplit.length == 1) {
                    context.getTextChannel().sendMessageAsync("You haven't given me anything to search for!", null);
                } else if (rawSplit.length == 2) {
                    String search = rawSplit[1].replace(" ", "+");
                    context.getTextChannel().sendMessageAsync("http://google.com/#q=" + search, null);
                }
            });
}
