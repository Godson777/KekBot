package com.godson.kekbot.command.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.XMLUtils;
import net.dv8tion.jda.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class AllowedUsers {
    public static Command allowedUsers = new Command("allowedUsers")
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().getId().equals(XMLUtils.getBotOwner())) {
                    List<User> users = XMLUtils.getAllowedUsers();
                    List<String> usernames = users.stream().map(User::getUsername).collect(Collectors.toList());
                    context.getMessage().getChannel().sendMessage("List of Allowed Users:\n`" + StringUtils.join(usernames, ", ") + "`");
                }
            });
}
