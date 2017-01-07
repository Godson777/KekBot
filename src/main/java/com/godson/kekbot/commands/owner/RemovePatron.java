package com.godson.kekbot.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.GSONUtils;

public class RemovePatron {
    public static Command removePatron = new Command("removepatron")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                if (rawSplit.length == 1) {
                    context.getMessage().getChannel().sendMessage("No name specified.").queue();
                } else {
                    try {
                        GSONUtils.getConfig().removePatron(rawSplit[1]).save();
                        context.getTextChannel().sendMessage("Successfully added patron.").queue();
                    } catch (IllegalArgumentException e) {
                        context.getTextChannel().sendMessage("Patron not found.").queue();
                    }
                }
            });
}
