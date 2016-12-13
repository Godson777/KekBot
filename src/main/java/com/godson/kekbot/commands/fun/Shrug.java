package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;

public class Shrug {
    public static Command shrug = new Command("shrug")
            .withCategory(CommandCategory.FUN)
            .withDescription("\"¯\\_(ツ)_/¯\"")
            .withUsage("{p}lenny")
            .deleteCommand(true)
            .onExecuted(context -> context.getTextChannel().sendMessage("¯\\_(ツ)_/¯").queue());
}
