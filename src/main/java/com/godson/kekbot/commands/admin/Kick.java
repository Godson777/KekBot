package com.godson.kekbot.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.FailureReason;
import net.dv8tion.jda.Permission;
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
            .userRequiredPermissions(Permission.KICK_MEMBERS)
            .botRequiredPermissions(Permission.KICK_MEMBERS)
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                Guild server = context.getGuild();
                if (rawSplit.length == 1) {
                    channel.sendMessageAsync(context.getMessage().getAuthor().getAsMention() + " Who am I supposed to kick? :neutral_face:", null);
                } else {
                    if (context.getMessage().getMentionedUsers().size() == 0) {
                        channel.sendMessageAsync(context.getMessage().getAuthor().getAsMention() + " The user you want to kick __**must**__ be in the form of a mention!", null);
                    } else if (context.getMessage().getMentionedUsers().size() == 1) {
                        if (context.getMessage().getMentionedUsers().get(0) == context.getJDA().getSelfInfo()) {
                            channel.sendMessageAsync("I can't kick *myself*! :neutral_face:", null);
                        } else if (context.getMessage().getMentionedUsers().get(0).equals(context.getMessage().getAuthor())) {
                            channel.sendMessageAsync("You can't kick *yourself*! :neutral_face:", null);
                        } else {
                            try {
                                server.getManager().kick(context.getMessage().getMentionedUsers().get(0));
                                channel.sendMessageAsync(context.getMessage().getMentionedUsers().get(0).getUsername() + " has been kicked. :boot:", null);
                            } catch (PermissionException e) {
                                channel.sendMessageAsync(context.getMessage().getMentionedUsers().get(0).getUsername() + "'s role is higher than mine. I am unable to kick them.", null);
                            }
                        }
                    } else {
                        List<String> users = new ArrayList<>();
                        List<String> failed = new ArrayList<>();
                        for (int i = 0; i < context.getMessage().getMentionedUsers().size(); i++) {
                            if (context.getMessage().getMentionedUsers().get(i) != context.getJDA().getSelfInfo()) {
                                try {
                                    server.getManager().kick(context.getMessage().getMentionedUsers().get(i));
                                    users.add(context.getMessage().getMentionedUsers().get(i).getUsername());
                                } catch (PermissionException e) {
                                    failed.add(context.getMessage().getMentionedUsers().get(i).getUsername());
                                }
                            }
                        }
                        if (users.size() >= 1) {
                            channel.sendMessageAsync(users.size() + " users (`" + StringUtils.join(users, ", ") + "`) have been kicked. :boot:", null);
                            if (failed.size() == 1) {
                                channel.sendMessageAsync("However, 1 user (`" + StringUtils.join(failed, ", ") + "`) couldn't be kicked due to having a higher rank than I do. ¯\\_(ツ)_/¯", null);
                            }
                            if (failed.size() > 1) {
                                channel.sendMessageAsync("However, " + failed.size() + " users (`" + StringUtils.join(failed, ", ") + "`) couldn't be kicked due to having a higher rank than I do. ¯\\_(ツ)_/¯", null);
                            }
                        } else {
                            if (failed.size() >= 1) {
                                channel.sendMessageAsync("All of the users you have specified could not be kicked due to having a higher rank than I do. ¯\\_(ツ)_/¯", null);
                            }
                        }
                    }
                }
            })
            .onFailure((context, reason) -> {
                if (reason.equals(FailureReason.AUTHOR_MISSING_PERMISSIONS))
                    context.getTextChannel().sendMessageAsync(context.getMessage().getAuthor().getAsMention() + ", you do not have the `Kick Members` permission!", null);
                else context.getTextChannel().sendMessageAsync("I seem to be lacking the `Kick Members` permission!", null);
            });
}
