package com.godson.kekbot.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;

public class Changelog {
    public static Command changelog = new Command("changelog")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Shows you the changelogs for this version of KekBot.")
            .withUsage("{p}changelog")
            .onExecuted(context -> context.getTextChannel().sendMessage("https://github.com/Godson777/KekBot/releases/tag/v" + KekBot.version).queue());
}
