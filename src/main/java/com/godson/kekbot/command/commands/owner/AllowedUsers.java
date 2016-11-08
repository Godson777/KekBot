package com.godson.kekbot.command.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Settings.Config;
import com.godson.kekbot.XMLUtils;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AllowedUsers {
    public static Command allowedUsers = new Command("allowedUsers")
            .onExecuted(context -> {
                Config config = GSONUtils.getConfig();
                if (context.getMessage().getAuthor().getId().equals(config.getBotOwner())) {
                    List<String> users = config.getAllowedUsers();
                    List<String> usernames = new ArrayList<String>();
                    users.forEach(user -> {
                        for (JDA jda : KekBot.jdas) {
                            try {
                                usernames.add(jda.getUserById(user).getUsername());
                                break;
                            } catch (NullPointerException e) {
                                //do nothing
                            }
                        }
                    });
                    context.getMessage().getChannel().sendMessageAsync("List of Allowed Users:\n`" + StringUtils.join(usernames, ", ") + "`", null);
                }
            });
}
