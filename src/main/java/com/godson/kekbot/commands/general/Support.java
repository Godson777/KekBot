package com.godson.kekbot.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;

public class Support {
    public static Command support = new Command("support")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Shows how you can support KekBot.")
            .withUsage("{p}support")
            .onExecuted(context -> {
                String support = "Want to show your support for KekBot? There are plenty of ways to do so!\n" +
                        "You can join KekBot's official server here: https://discord.gg/3nbqavE\n" +
                        "There, you can suggest features, memes, and other things that'll help impact KekBot's progress!" +
                        "And you can also visit KekBot's Patreon: https://www.patreon.com/KekBot";
                context.getTextChannel().sendMessageAsync(support, null);
            });
}
