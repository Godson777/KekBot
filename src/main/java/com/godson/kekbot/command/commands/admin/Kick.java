package com.godson.kekbot.command.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.FailureReason;
import com.godson.kekbot.KekBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.exceptions.PermissionException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class Kick {
    public static Command kick = new Command("kick")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Kicks a specified user or users.")
            .withUsage("{p}kick <@user> {can @mention more than one person}")
            .userRequiredPermissions()
            .botRequiredPermissions()
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                Guild server = context.getGuild();
                if (rawSplit.length == 1) {
                    channel.sendMessage(context.getMessage().getAuthor().getAsMention() + " Who am I supposed to kick? :neutral_face:");
                } else {
                    if (context.getMessage().getMentionedUsers().size() == 0) {
                        channel.sendMessage(context.getMessage().getAuthor().getAsMention() + " The user you want to kick __**must**__ be in the form of a mention!");
                    } else if (context.getMessage().getMentionedUsers().size() == 1) {
                        if (context.getMessage().getMentionedUsers().get(0) == KekBot.client.getSelfInfo()) {
                            channel.sendMessage("I can't kick *myself*! :neutral_face:");
                        } else if (context.getMessage().getMentionedUsers().get(0).equals(context.getMessage().getAuthor())) {
                            channel.sendMessage("You can't kick *yourself*! :neutral_face:");
                        } else {
                            try {
                                server.getManager().kick(context.getMessage().getMentionedUsers().get(0));
                                channel.sendMessage(context.getMessage().getMentionedUsers().get(0).getUsername() + " has been kicked. :boot:");
                            } catch (PermissionException e) {
                                channel.sendMessage(context.getMessage().getMentionedUsers().get(0).getUsername() + "'s role is higher than mine. I am unable to kick them.");
                            }
                        }
                    } else {
                        List<String> users = new ArrayList<>();
                        List<String> failed = new ArrayList<>();
                        for (int i = 0; i < context.getMessage().getMentionedUsers().size(); i++) {
                            if (context.getMessage().getMentionedUsers().get(i) != KekBot.client.getSelfInfo()) {
                                try {
                                    server.getManager().kick(context.getMessage().getMentionedUsers().get(i));
                                    users.add(context.getMessage().getMentionedUsers().get(i).getUsername());
                                } catch (PermissionException e) {
                                    failed.add(context.getMessage().getMentionedUsers().get(i).getUsername());
                                }
                            }
                        }
                        if (users.size() >= 1) {
                            channel.sendMessage(users.size() + " users (`" + StringUtils.join(users, ", ") + "`) have been kicked. :boot:");
                            if (failed.size() == 1) {
                                channel.sendMessage("However, 1 user (`" + StringUtils.join(failed, ", ") + "`) couldn't be kicked due to having a higher rank than I do. ¯\\_(ツ)_/¯");
                            }
                            if (failed.size() > 1) {
                                channel.sendMessage("However, " + failed.size() + " users (`" + StringUtils.join(failed, ", ") + "`) couldn't be kicked due to having a higher rank than I do. ¯\\_(ツ)_/¯");
                            }
                        } else {
                            if (failed.size() >= 1) {
                                channel.sendMessage("All of the users you have specified could not be kicked due to having a higher rank than I do. ¯\\_(ツ)_/¯");
                            }
                        }
                    }
                }
            })
            .onFailure((context, reason) -> {
                if (reason.equals(FailureReason.AUTHOR_MISSING_PERMISSIONS))
                    context.getTextChannel().sendMessage(context.getMessage().getAuthor().getAsMention() + ", you do not have the `Kick Members` permission!");
                else context.getTextChannel().sendMessage("I seem to be lacking the `Kick Members` permission!");
            });
}
