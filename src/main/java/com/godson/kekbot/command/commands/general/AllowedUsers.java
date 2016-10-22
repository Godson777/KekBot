package com.godson.kekbot.command.commands.general;

import com.darichey.discord.api.Command;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;
import java.util.stream.Collectors;

public class AllowedUsers {
    public static Command allowedUsers = new Command("allowedUsers")
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().getID().equals(XMLUtils.getBotOwner())) {
                    List<IUser> users = XMLUtils.getAllowedUsers();
                    List<String> usernames = users.stream().map(IUser::getName).collect(Collectors.toList());
                    EasyMessage.send(context.getMessage().getChannel(), "List of Allowed Users:\n`" + StringUtils.join(usernames, ", ") + "`");
                }
            });
}
