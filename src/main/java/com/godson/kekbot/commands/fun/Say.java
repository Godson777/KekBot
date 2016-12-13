package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;

public class Say {
    public static Command say = new Command("say")
            .withCategory(CommandCategory.FUN)
            .withDescription("Makes KekBot say whatever you want it to say.")
            .withUsage("{p}say <message>")
            .deleteCommand(true)
            .onExecuted(context -> {
                String[] contents = context.getMessage().getRawContent().split(" ", 2);
                if (contents.length == 1) {
                    context.getTextChannel().sendMessage(":anger: " + context.getMessage().getAuthor().getAsMention() + ", could you at *least* give me something to *say*?").queue();
                } else {
                    context.getTextChannel().sendMessage(contents[1]).queue();
                }
            });
}