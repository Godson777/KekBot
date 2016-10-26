package com.godson.kekbot.command.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.FailureReason;
import com.godson.kekbot.XMLUtils;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import org.jdom2.JDOMException;

import java.io.IOException;

public class AutoRole {
    public static Command autoRole = new Command("autorole")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Allows you to set a role in which KekBot will assign to all newcomers. You can also RESET this setting, disabling it.")
            .withUsage("{p}autorole <role | reset>")
            .userRequiredPermissions(Permission.MANAGE_ROLES)
            .onExecuted(context -> {
                String args[] = context.getArgs();
                TextChannel channel = context.getTextChannel();
                Guild guild = context.getGuild();
                    if (args.length == 0) {
                        channel.sendMessage("Which role am I gonna automatically give newcomers? :neutral_face:");
                    } else {
                        if (guild.getRolesByName(args[0]).size() == 0) {
                            channel.sendMessage("Unable to find any roles by the name of \"" + args[0] + "\"!");
                        } else {
                            try {
                                XMLUtils.setAutoRole(guild, guild.getRolesByName(args[0]).get(0));
                            } catch (JDOMException | IOException e) {
                                e.printStackTrace();
                            }
                            channel.sendMessage("Got it! I will now give newcomers the role \"" + guild.getRolesByName(args[0]).get(0).getName() + "\"!");
                        }
                    }
            })
            .onFailure((context, reason) -> {
                if (reason.equals(FailureReason.AUTHOR_MISSING_PERMISSIONS))
                    context.getTextChannel().sendMessage(context.getMessage().getAuthor().getAsMention() + ", you do not have the `Manage Roles` permission!");
            });
}
