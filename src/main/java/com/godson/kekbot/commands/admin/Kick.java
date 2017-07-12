package com.godson.kekbot.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.FailureReason;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class Kick {
    public static Command kick = new Command("kick")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Kicks a specified user or users.")
            .withUsage("{p}kick <@user> {can @mention more than one person}")
            .userRequiredPermissions(Permission.KICK_MEMBERS)
            .botRequiredPermissions(Permission.KICK_MEMBERS)
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                Guild server = context.getGuild();
                if (rawSplit.length == 1) {
                    channel.sendMessage(KekBot.respond(context, Action.KICK_EMPTY)).queue();
                } else {
                    if (context.getMessage().getMentionedUsers().size() == 0) {
                        channel.sendMessage(context.getMessage().getAuthor().getAsMention() + " The user you want to kick __**must**__ be in the form of a mention!").queue();
                    } else if (context.getMessage().getMentionedUsers().size() == 1) {
                        if (context.getMessage().getMentionedUsers().get(0) == context.getJDA().getSelfUser()) {
                            channel.sendMessage("How would I kick myself? :thinking:").queue();
                        } else if (context.getMessage().getMentionedUsers().get(0).equals(context.getMessage().getAuthor())) {
                            channel.sendMessage("You can't kick yourself, it just doesn't work that way.").queue();
                        } else {
                            if (context.getGuild().getMember(context.getMessage().getMentionedUsers().get(0)).getRoles().stream().map(Role::getPositionRaw).max(Integer::compareTo).get() >= context.getMember().getRoles().stream().map(Role::getPositionRaw).max(Integer::compareTo).get()) {
                                channel.sendMessage("You can't kick someone who's highest role is the same as or is higher than yours.").queue();
                            } else {
                                try {
                                    server.getController().kick(context.getGuild().getMember(context.getMessage().getMentionedUsers().get(0))).reason("Kicked by: " + context.getAuthor().getName() + "#" + context.getAuthor().getDiscriminator() + " (" + context.getAuthor().getId() + ")").queue();
                                    channel.sendMessage(KekBot.respond(context, Action.KICK_SUCCESS, context.getMessage().getMentionedUsers().get(0).getName())).queue();
                                } catch (PermissionException e) {
                                    channel.sendMessage(context.getMessage().getMentionedUsers().get(0).getName() + "'s role is higher than mine. I am unable to kick them.").queue();
                                }
                            }
                        }
                    } else {
                        List<String> users = new ArrayList<>();
                        List<String> failed = new ArrayList<>();
                        for (int i = 0; i < context.getMessage().getMentionedUsers().size(); i++) {
                            if (context.getMessage().getMentionedUsers().get(i) != context.getJDA().getSelfUser()) {
                                Member member = context.getGuild().getMember(context.getMessage().getMentionedUsers().get(i));
                                if (member.getRoles().stream().map(Role::getPositionRaw).max(Integer::compareTo).get() >= context.getMember().getRoles().stream().map(Role::getPositionRaw).max(Integer::compareTo).get()) {
                                    failed.add(context.getMessage().getMentionedUsers().get(i).getName());
                                } else {
                                    try {
                                        server.getController().kick(member).reason("Mass Kicked by: " + context.getAuthor().getName() + "#" + context.getAuthor().getDiscriminator() + " (" + context.getAuthor().getId() + ")").queue();
                                        users.add(context.getMessage().getMentionedUsers().get(i).getName());
                                    } catch (PermissionException e) {
                                        failed.add(context.getMessage().getMentionedUsers().get(i).getName());
                                    }
                                }
                            }
                        }
                        if (users.size() >= 1) {
                            channel.sendMessage(KekBot.respond(context, Action.KICK_SUCCESS, "`" + StringUtils.join(users, ", ") + "`")).queue();
                            if (failed.size() > 0) {
                                channel.sendMessage("However, " + failed.size() + (failed.size() == 1 ? " user" : " users") + "(`" + StringUtils.join(failed, ", ") + "`) couldn't be kicked due to having a higher rank than I do. ¯\\_(ツ)_/¯").queue();
                            }
                        } else {
                            if (failed.size() >= 1) {
                                channel.sendMessage("All of the users you have specified could not be kicked due to having a higher rank than I do. ¯\\_(ツ)_/¯").queue();
                            }
                        }
                    }
                }
            })
            .onFailure((context, reason) -> {
                if (reason.equals(FailureReason.AUTHOR_MISSING_PERMISSIONS))
                    context.getTextChannel().sendMessage(KekBot.respond(context, Action.NOPERM_USER, "`Kick Members`")).queue();
                else context.getTextChannel().sendMessage(KekBot.respond(context, Action.NOPERM_BOT, "`Kick Members`")).queue();
            });
}
