package com.godson.kekbot.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.FailureReason;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.GuildManager;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AddRole {
    public static Command addRole = new Command("addrole")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Adds a role to a specified user (or users).")
            .withUsage("{p}addrole <role> | <@user> {@user}...")
            .userRequiredPermissions(Permission.MANAGE_ROLES)
            .botRequiredPermissions(Permission.MANAGE_ROLES)
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                if (rawSplit.length == 1) {
                    channel.sendMessage("No role specified!").queue();
                } else {
                    String params[] = rawSplit[1].split("\\u007c", 2);
                    if (params[0].startsWith(" ")) params[0] = params[0].replaceFirst("([ ]+)", "");
                    if (params[0].endsWith(" ")) params[0] = params[0].replaceAll("([ ]+$)", "");
                    if (context.getGuild().getRolesByName(params[0], true).size() == 0) {
                        channel.sendMessage("Unable to find any roles by the name of \"" + params[0] + "\"!").queue();
                    } else {
                        if (params.length == 1) {
                            channel.sendMessage("Who am I supposed to give this role to? :neutral_face:").queue();
                        } else {
                            if (context.getMessage().getMentionedUsers().size() == 0) {
                                channel.sendMessage("The user(s) you want to assign this role to __**must**__ be in the form of a mention!").queue();
                            } else if (context.getMessage().getMentionedUsers().size() == 1) {
                                Member member = context.getGuild().getMember(context.getMessage().getMentionedUsers().get(0));
                                if (!member.getRoles().contains(context.getGuild().getRolesByName(params[0], true).get(0))) {
                                    if (member.getRoles().stream().map(Role::getPositionRaw).max(Integer::compareTo).get() >= context.getMember().getRoles().stream().map(Role::getPositionRaw).max(Integer::compareTo).get()) {
                                        channel.sendMessage("You can't edit someone's roles when their highest role is the same as or is higher than yours.").queue();
                                    } else {
                                        try {
                                            context.getGuild().getController().addRolesToMember(member, context.getGuild().getRolesByName(params[0], true).get(0)).reason("Role Given by: " + context.getAuthor().getName() + "#" + context.getAuthor().getDiscriminator() + " (" + context.getAuthor().getId() + ")").queue();
                                            channel.sendMessage(KekBot.respond(context, Action.ROLE_ADDED, context.getMessage().getMentionedUsers().get(0).getName() + "#" + context.getMessage().getMentionedUsers().get(0).getDiscriminator())).queue();
                                        } catch (PermissionException e) {
                                            channel.sendMessage("That role is higher than mine! I cannot assign it to any users!").queue();
                                        }
                                    }
                                } else {
                                    channel.sendMessage("This user already has the role you specified!").queue();
                                }

                            } else {
                                List<User> users = context.getMessage().getMentionedUsers();
                                GuildController controller = context.getGuild().getController();
                                Role role = context.getGuild().getRolesByName(params[0], true).get(0);
                                List<String> success = new ArrayList<String>();
                                List<String> exist = new ArrayList<String>();
                                boolean failed = false;
                                for (User user : users) {
                                    Member member = context.getGuild().getMember(user);
                                    if (member.getRoles().contains(role)) {
                                        try {
                                            controller.addRolesToMember(member, role).reason("Mass Role Given by: " + context.getAuthor().getName() + "#" + context.getAuthor().getDiscriminator() + " (" + context.getAuthor().getId() + ")").queue();
                                            success.add(user.getName() + "#" + user.getDiscriminator());
                                        } catch (PermissionException e) {
                                            failed = true;
                                            break;
                                        }
                                    } else {
                                        exist.add(user.getName() + "#" + user.getDiscriminator());
                                    }
                                }
                                if (failed) {
                                    channel.sendMessage("That role is higher than mine! I cannot assign it to any users!").queue();
                                } else {
                                    if (success.size() != 0) {
                                        channel.sendMessage(KekBot.respond(context, Action.ROLE_ADDED, StringUtils.join(success, ", ")) +
                                                (exist.size() != 0 ? "\nHowever, " + exist.size() + (exist.size() == 1 ? "user" : "users") + ": `" + StringUtils.join(exist, ", ") + "` already have this role. So they were ignored." : "")).queue();
                                    } else {
                                        channel.sendMessage("All users you specified already have this role!").queue();
                                    }
                                }
                            }
                        }
                    }
                }
            })
            .onFailure((context, failureReason) -> {
                if (failureReason.equals(FailureReason.AUTHOR_MISSING_PERMISSIONS)) context.getTextChannel().sendMessage(KekBot.respond(context, Action.NOPERM_USER, "`Manage Roles`")).queue();
                else context.getTextChannel().sendMessage(KekBot.respond(context, Action.NOPERM_BOT, "`Manage Roles`")).queue();
            });
}
