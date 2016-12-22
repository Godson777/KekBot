package com.godson.kekbot.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.FailureReason;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Ban {
    public static Command ban = new Command("ban")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Bans a specified user or users.")
            .withUsage("{p}ban <@user> {can @mention more than one person}")
            .userRequiredPermissions(Permission.BAN_MEMBERS)
            .botRequiredPermissions(Permission.BAN_MEMBERS)
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                Guild server = context.getGuild();
                TextChannel channel = context.getTextChannel();
                if (rawSplit.length == 1) {
                    channel.sendMessage(KekBot.respond(context, Action.BAN_EMPTY)).queue();
                } else {
                    if (context.getMessage().getMentionedUsers().size() == 0) {
                        channel.sendMessage(context.getMessage().getAuthor().getAsMention() + " The user you want to ban __**must**__ be in the form of a mention!").queue();
                    } else if (context.getMessage().getMentionedUsers().size() == 1) {
                        if (context.getMessage().getMentionedUsers().get(0) == context.getJDA().getSelfUser()) {
                            channel.sendMessage(":frowning: I can't ban myself...").queue();
                        } else if (context.getMessage().getMentionedUsers().get(0).equals(context.getMessage().getAuthor())) {
                            channel.sendMessage("Why would you want to ban yourself? That seems kinda useless to me...").queue();
                        } else {
                            try {
                                server.getController().ban(context.getMessage().getMentionedUsers().get(0), 0).queue();
                                channel.sendMessage(KekBot.respond(context, Action.BAN_SUCCESS, context.getMessage().getMentionedUsers().get(0).getName())).queue();
                            } catch (PermissionException e) {
                                channel.sendMessage(context.getMessage().getMentionedUsers().get(0).getName() + "'s role is higher than mine. I am unable to ban them.").queue();
                            }
                        }
                    } else {
                        List<String> users = new ArrayList<>();
                        List<String> failed = new ArrayList<>();
                        for (int i = 0; i < context.getMessage().getMentionedUsers().size(); i++) {
                            if (context.getMessage().getMentionedUsers().get(i) != context.getJDA().getSelfUser()) {
                                    try {
                                        server.getController().ban(context.getMessage().getMentionedUsers().get(i), 0).queue();
                                        users.add(context.getMessage().getMentionedUsers().get(i).getName());
                                    } catch (PermissionException e) {
                                        failed.add(context.getMessage().getMentionedUsers().get(i).getName());
                                    }
                            }
                        }
                        if (users.size() >= 1) {
                            channel.sendMessage(KekBot.respond(context, Action.BAN_SUCCESS, "`" + StringUtils.join(users, ", ") + "`")).queue();
                            if (failed.size() > 0) {
                                channel.sendMessage("However, " + failed.size() + (failed.size() == 1 ? " user" : " users") + " (`" + StringUtils.join(failed, ", ") + "`) couldn't be banned due to having a higher rank than I do. ¯\\_(ツ)_/¯").queue();
                            }
                        } else {
                            if (failed.size() >= 1) {
                                channel.sendMessage("All of the users you have specified couldn't be banned due to having a higher rank than I do. ¯\\_(ツ)_/¯").queue();
                            }
                        }
                    }
                }
            })
            .onFailure((context, reason) -> {
                if (reason.equals(FailureReason.AUTHOR_MISSING_PERMISSIONS))
                    context.getTextChannel().sendMessage(KekBot.respond(context, Action.NOPERM_USER, "`Ban Members`")).queue();
                else context.getTextChannel().sendMessage(KekBot.respond(context, Action.NOPERM_BOT, "`Ban Members`")).queue();
            });
}
