package com.godson.kekbot.command.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.EasyMessage;
import net.dv8tion.jda.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Bots {
    public static Command bots = new Command("bots")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Sends a list of bots found on ths server the command was used in.")
            .withUsage("{p}bots")
            .onExecuted(context -> {
                List<User> users = context.getGuild().getUsers();
                List<String> bots = new ArrayList<>();
                users.stream().filter(User::isBot).forEach(user -> {
                    try {
                        bots.add(user.getUsername());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                });
                context.getTextChannel().sendMessage("```List of Bots:\n\n" + StringUtils.join(bots, ", ") + "```");
            });
}
