package com.godson.kekbot.command.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.FailureReason;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.KekBot;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Ban {
    public static Command ban = new Command("ban")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Bans a specified user or users.")
            .withUsage("{p}ban <@user> {can @mention more than one person}")
            .userRequiredPermissions(EnumSet.of(Permissions.BAN))
            .botRequiredPermissions(EnumSet.of(Permissions.BAN))
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getContent().split(" ", 2);
                IGuild server = context.getMessage().getGuild();
                IChannel channel = context.getMessage().getChannel();
                if (rawSplit.length == 1) {
                    EasyMessage.send(channel, context.getMessage().getAuthor().mention() + " Who am I supposed to ban? :neutral_face:");
                } else {
                    if (context.getMessage().getMentions().size() == 0) {
                        EasyMessage.send(channel, context.getMessage().getAuthor().mention() + " The user you want to ban __**must**__ be in the form of a mention!");
                    } else if (context.getMessage().getMentions().size() == 1) {
                        if (context.getMessage().getMentions().get(0) == KekBot.client.getOurUser()) {
                            EasyMessage.send(channel, "I can't ban *myself*! :neutral_face:");
                        } else if (context.getMessage().getMentions().get(0).equals(context.getMessage().getAuthor())) {
                            EasyMessage.send(channel, "You can't ban *yourself*! :neutral_face:");
                        } else {
                            RequestBuffer.request(() -> {
                                try {
                                    server.banUser(context.getMessage().getMentions().get(0));
                                    EasyMessage.send(channel, context.getMessage().getMentions().get(0).getName() + " has met the banhammer. :hammer:");
                                } catch (DiscordException e) {
                                    e.printStackTrace();
                                } catch (MissingPermissionsException e) {
                                    EasyMessage.send(channel, context.getMessage().getMentions().get(0).getName() + "'s role is higher than mine. I am unable to ban them.");
                                }
                            });
                        }
                    } else {
                        List<String> users = new ArrayList<>();
                        List<String> failed = new ArrayList<>();
                        for (int i = 0; i < context.getMessage().getMentions().size(); i++) {
                            if (context.getMessage().getMentions().get(i) != KekBot.client.getOurUser()) {
                                int finalI = i;
                                RequestBuffer.request(() -> {
                                    try {
                                        server.banUser(context.getMessage().getMentions().get(finalI));
                                        users.add(context.getMessage().getMentions().get(finalI).getName());
                                    } catch (DiscordException e) {
                                        e.printStackTrace();
                                    } catch (MissingPermissionsException e) {
                                        failed.add(context.getMessage().getMentions().get(finalI).getName());
                                    }
                                });
                            }
                        }
                        if (users.size() >= 1) {
                            EasyMessage.send(channel, users.size() + " users (`" + StringUtils.join(users, ", ") + "`) have met the banhammer. :hammer:");
                            if (failed.size() == 1) {
                                EasyMessage.send(channel, "However, 1 user (`" + StringUtils.join(failed, ", ") + "`) couldn't be banned due to having a higher rank than I do. ¯\\_(ツ)_/¯");
                            }
                            if (failed.size() > 1) {
                                EasyMessage.send(channel, "However, " + failed.size() + " users (`" + StringUtils.join(failed, ", ") + "`) couldn't be banned due to having a higher rank than I do. ¯\\_(ツ)_/¯");
                            }
                        } else {
                            if (failed.size() >= 1) {
                                EasyMessage.send(channel, "All of the users you have specified couldn't be banned due to having a higher rank than I do. ¯\\_(ツ)_/¯");
                            }
                        }
                    }
                }
            })
            .onFailure((context, reason) -> {
                if (reason.equals(FailureReason.AUTHOR_MISSING_PERMISSIONS))
                    EasyMessage.send(context.getMessage().getChannel(), context.getMessage().getAuthor().mention() + ", you do not have the `Ban Members` permission!");
                else EasyMessage.send(context.getMessage().getChannel(), "I seem to be lacking the `Ban Members` permission!");
            });
}
