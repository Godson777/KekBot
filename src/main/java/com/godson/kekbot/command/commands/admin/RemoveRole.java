package com.godson.kekbot.command.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.FailureReason;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.managers.GuildManager;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RemoveRole {
    public static Command removeRole = new Command("removerole")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Removes a role to a specified user (or users).")
            .withUsage("{p}removerole <role> <@user> {@user}...")
            .userRequiredPermissions(Permission.MANAGE_ROLES)
            .botRequiredPermissions(Permission.MANAGE_ROLES)
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                if (rawSplit.length == 1) {
                    channel.sendMessageAsync("No role specified!", null);
                } else {
                    String params[] = rawSplit[1].split("\\u007c", 2);
                    if (params[0].startsWith(" ")) params[0] = params[0].replaceFirst("([ ]+)", "");
                    if (params[0].endsWith(" ")) params[0] = params[0].replaceAll("([ ]+$)", "");
                    if (context.getGuild().getRolesByName(params[0]).size() == 0) {
                        channel.sendMessageAsync("Unable to find any roles by the name of \"" + params[0] + "\"!", null);
                    } else {
                        if (params.length == 1) {
                            channel.sendMessageAsync("Who am I supposed to take this role from? :neutral_face:", null);
                        } else {
                            if (context.getMessage().getMentionedUsers().size() == 0) {
                                channel.sendMessageAsync("The user(s) you want to remove this role from __**must**__ be in the form of a mention!", null);
                            } else if (context.getMessage().getMentionedUsers().size() == 1) {
                                User user = context.getMessage().getMentionedUsers().get(0);
                                if (context.getGuild().getRolesForUser(user).contains(context.getGuild().getRolesByName(params[0]).get(0))) {
                                    try {
                                        context.getGuild().getManager().removeRoleFromUser(user, context.getGuild().getRolesByName(params[0]).get(0)).update();
                                        channel.sendMessageAsync("Successfully removed role from `" + user.getUsername() + "#" + user.getDiscriminator() + "`. :thumbsup:", null);
                                    } catch (PermissionException e) {
                                        channel.sendMessageAsync("That role is higher than mine! I cannot remove it from any users!", null);
                                    }
                                } else {
                                    channel.sendMessageAsync("This user doesn't have the role you specified!", null);
                                }
                            } else {
                                List<User> users = context.getMessage().getMentionedUsers();
                                GuildManager manager = context.getGuild().getManager();
                                Role role = context.getGuild().getRolesByName(params[0]).get(0);
                                List<String> success = new ArrayList<String>();
                                List<String> exist = new ArrayList<String>();
                                boolean failed = false;
                                for (User user : users) {
                                    if (context.getGuild().getRolesForUser(user).contains(role)) {
                                        try {
                                            manager.removeRoleFromUser(user, context.getGuild().getRolesByName(params[0]).get(0));
                                            success.add(user.getUsername() + "#" + user.getDiscriminator());
                                        } catch (PermissionException e) {
                                            failed = true;
                                            break;
                                        }
                                    } else {
                                        exist.add(user.getUsername() + "#" + user.getDiscriminator());
                                    }
                                }
                                manager.update();
                                if (failed) {
                                    channel.sendMessageAsync("That role is higher than mine! I cannot remove it from any users!", null);
                                } else {
                                    if (success.size() != 0) {
                                        channel.sendMessageAsync("Successfully removed role from " + (success.size() == 1 ? "user" : "users") + ": `" + StringUtils.join(success, ", ") + "`." +
                                                (exist.size() != 0 ? "\nHowever, " + exist.size() + (exist.size() == 1 ? "user" : "users") + ": `" + StringUtils.join(exist, ", ") + "` don't have this role. So they were ignored." : ""), null);
                                    } else {
                                        channel.sendMessageAsync("All users you specified don't have this role!", null);
                                    }
                                }
                            }
                        }
                    }
                }
            })
            .onFailure((context, failureReason) -> {
                if (failureReason.equals(FailureReason.AUTHOR_MISSING_PERMISSIONS)) context.getTextChannel().sendMessageAsync(context.getMessage().getAuthor().getAsMention() + ", you don't have the `Manage Roles` permission!", null);
                else context.getTextChannel().sendMessageAsync("I seem to be lacking the `Manage Roles` permission!", null);
            });
}
