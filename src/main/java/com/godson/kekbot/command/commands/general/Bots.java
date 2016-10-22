package com.godson.kekbot.command.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.EasyMessage;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.List;

public class Bots {
    public static Command bots = new Command("bots")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Sends a list of bots found on ths server the command was used in.")
            .withUsage("{p}bots")
            .onExecuted(context -> {
                List<IUser> users = context.getMessage().getGuild().getUsers();
                List<String> bots = new ArrayList<>();
                users.stream().filter(IUser::isBot).forEach(user -> {
                    try {
                        bots.add(user.getName());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                });
                EasyMessage.send(context.getMessage().getChannel(), "```List of Bots:\n\n" + StringUtils.join(bots, ", ") + "```");
            });
}
