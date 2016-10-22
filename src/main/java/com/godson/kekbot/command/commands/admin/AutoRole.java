package com.godson.kekbot.command.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.FailureReason;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.XMLUtils;
import org.jdom2.JDOMException;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Permissions;

import java.io.IOException;
import java.util.EnumSet;

public class AutoRole {
    public static Command autoRole = new Command("autorole")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Allows you to set a role in which KekBot will assign to all newcomers. You can also RESET this setting, disabling it.")
            .withUsage("{p}autorole <role | reset>")
            .userRequiredPermissions(EnumSet.of(Permissions.MANAGE_ROLES))
            .onExecuted(context -> {
                String args[] = context.getArgs();
                IChannel channel = context.getMessage().getChannel();
                IGuild server = context.getMessage().getGuild();
                if (channel.getModifiedPermissions(context.getMessage().getAuthor()).contains(Permissions.MANAGE_ROLES)) {
                    if (args.length == 0) {
                        EasyMessage.send(channel, "Which role am I gonna automatically give newcomers? :neutral_face:");
                    } else {
                        if (server.getRolesByName(args[0]).size() == 0) {
                            EasyMessage.send(channel, "Unable to find any roles by the name of \"" + args[0] + "\"!");
                        } else {
                            try {
                                XMLUtils.setAutoRole(server, server.getRolesByName(args[0]).get(0));
                            } catch (JDOMException | IOException e) {
                                e.printStackTrace();
                            }
                            EasyMessage.send(channel, "Got it! I will now give newcomers the role \"" + server.getRolesByName(args[0]).get(0).getName() + "\"!");
                        }
                    }
                } else {
                    EasyMessage.send(channel, context.getMessage().getAuthor().mention() + ", you do not have the `Manage Roles` permission!");
                }
            })
            .onFailure((context, reason) -> {
                if (reason.equals(FailureReason.AUTHOR_MISSING_PERMISSIONS))
                    EasyMessage.send(context.getMessage().getChannel(), context.getMessage().getAuthor().mention() + ", you do not have the `Manage Roles` permission!");
            });
}
