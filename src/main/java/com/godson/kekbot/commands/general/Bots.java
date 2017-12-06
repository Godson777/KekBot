package com.godson.kekbot.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Bots {
    public static Command bots = new Command("bots")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Sends a list of bots found on the server the command was used in.")
            .withUsage("{p}bots")
            .onExecuted(context -> {
                List<String> bots = context.getGuild().getMembers().stream().map(Member::getUser).filter(User::isBot).map(User::getName).collect(Collectors.toList());
                context.getTextChannel().sendMessage("```List of Bots:\n\n" + StringUtils.join(bots, ", ") + "```").queue();
            });
}
