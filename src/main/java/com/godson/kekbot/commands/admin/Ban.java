package com.godson.kekbot.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.FailureReason;
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
                    channel.sendMessage(context.getMessage().getAuthor().getAsMention() + " Who am I supposed to ban? :neutral_face:").queue();
                } else {
                    if (context.getMessage().getMentionedUsers().size() == 0) {
                        channel.sendMessage(context.getMessage().getAuthor().getAsMention() + " The user you want to ban __**must**__ be in the form of a mention!").queue();
                    } else if (context.getMessage().getMentionedUsers().size() == 1) {
                        if (context.getMessage().getMentionedUsers().get(0) == context.getJDA().getSelfUser()) {
                            channel.sendMessage("I can't ban *myself*! :neutral_face:").queue();
                        } else if (context.getMessage().getMentionedUsers().get(0).equals(context.getMessage().getAuthor())) {
                            channel.sendMessage("You can't ban *yourself*! :neutral_face:").queue();
                        } else {
                            try {
                                server.getController().ban(context.getMessage().getMentionedUsers().get(0), 0);
                                channel.sendMessage(context.getMessage().getMentionedUsers().get(0).getName() + " has met the banhammer. :hammer:").queue();
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
                                        server.getController().ban(context.getMessage().getMentionedUsers().get(i), 0);
                                        users.add(context.getMessage().getMentionedUsers().get(i).getName());
                                    } catch (PermissionException e) {
                                        failed.add(context.getMessage().getMentionedUsers().get(i).getName());
                                    }
                            }
                        }
                        if (users.size() >= 1) {
                            channel.sendMessage(users.size() + " users (`" + StringUtils.join(users, ", ") + "`) have met the banhammer. :hammer:").queue();
                            if (failed.size() == 1) {
                                channel.sendMessage("However, 1 user (`" + StringUtils.join(failed, ", ") + "`) couldn't be banned due to having a higher rank than I do. ¯\\_(ツ)_/¯").queue();
                            }
                            if (failed.size() > 1) {
                                channel.sendMessage("However, " + failed.size() + " users (`" + StringUtils.join(failed, ", ") + "`) couldn't be banned due to having a higher rank than I do. ¯\\_(ツ)_/¯").queue();
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
                    context.getTextChannel().sendMessage(context.getMessage().getAuthor().getAsMention() + ", you do not have the `Ban Members` permission!").queue();
                else context.getTextChannel().sendMessage("I seem to be lacking the `Ban Members` permission!").queue();
            });
}
