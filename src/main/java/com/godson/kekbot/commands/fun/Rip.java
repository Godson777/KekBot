package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;

public class Rip {
    public static Command rip = new Command("rip")
            .withCategory(CommandCategory.FUN)
            .withDescription("Is someone kill? Mourn them with this command.")
            .withUsage("{p}rip <text>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                if (rawSplit.length == 1) {
                    context.getTextChannel().sendMessage("Wait, so who is kill?").queue();
                } else if (rawSplit.length == 2) {
                    String search = rawSplit[1].replace(" ", "%20").replace("@everyone", "@\u200Beveryone");
                    context.getTextChannel().sendMessage("http://ripme.xyz/#" + search).queue();
                }
            });
}
