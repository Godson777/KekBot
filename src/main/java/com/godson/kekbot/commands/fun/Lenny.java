package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;

public class Lenny {
    public static Command lenny = new Command("lenny")
            .withCategory(CommandCategory.FUN)
            .withDescription("\"( ͡° ͜ʖ ͡°)\"")
            .withUsage("{p}lenny")
            .deleteCommand(true)
            .onExecuted(context -> context.getTextChannel().sendMessageAsync("( ͡° ͜ʖ ͡°)", null));
}
