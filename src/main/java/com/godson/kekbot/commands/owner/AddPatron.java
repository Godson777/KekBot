package com.godson.kekbot.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.GSONUtils;

public class AddPatron {
    public static Command addPatron = new Command("addpatron")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                if (rawSplit.length == 1) {
                    context.getMessage().getChannel().sendMessage("No name specified.").queue();
                } else {
                    GSONUtils.getConfig().addPatron(rawSplit[1]).save();
                    context.getTextChannel().sendMessage("Successfully added patron.").queue();
                }
            });
}
