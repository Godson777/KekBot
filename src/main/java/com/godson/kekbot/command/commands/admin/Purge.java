package com.godson.kekbot.command.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.FailureReason;
import com.godson.kekbot.EasyMessage;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.util.EnumSet;


public class Purge {
    public static Command purge = new Command("purge")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Mass deletes X number of messages.")
            .withUsage("{p}purge <number>")
            .userRequiredPermissions(EnumSet.of(Permissions.MANAGE_MESSAGES))
            .botRequiredPermissions(EnumSet.of(Permissions.MANAGE_MESSAGES))
            .deleteCommand(true)
            .onExecuted(context -> {
                String args[] = context.getArgs();
                IChannel channel = context.getMessage().getChannel();
                IMessage message;
                if (args.length == 0) {
                    EasyMessage.send(channel, context.getMessage().getAuthor().mention() + ", next time, try to at least supply a number...");
                } else {
                    try {
                        int purge = Integer.valueOf(args[0]);
                        if (purge >= 2 && purge <= 100) {
                            message = EasyMessage.send(channel, "Purging...");
                            RequestBuffer.request(() -> {
                                try {
                                    channel.getMessages().load(purge);
                                    channel.getMessages().deleteFromRange(2, purge+2);
                                    EasyMessage.editMessage(message, "Sucessfully purged `" + purge + "` messages.");
                                } catch (DiscordException | MissingPermissionsException e) {
                                    e.printStackTrace();
                                } catch (IndexOutOfBoundsException e) {
                                    int remaining = channel.getMessages().size();
                                    try {
                                        channel.getMessages().deleteFromRange(2, remaining);
                                        EasyMessage.editMessage(message, "Sucessfully purged `" + remaining + "` messages.");
                                    } catch (DiscordException | IndexOutOfBoundsException | MissingPermissionsException er) {
                                        er.printStackTrace();
                                    }
                                }
                            });
                        } else if (purge <= 1) {
                            EasyMessage.send(channel, "Sorry m8, that's too low a number. Number must be between 2 and 100.");
                        } else if (purge > 100) {
                            EasyMessage.send(channel, "Sorry m8, that's too high a number. Number must be between 2 and 100.");
                        }
                    } catch (NumberFormatException e) {
                        EasyMessage.send(channel, "`" + args[0] + "` is not a number, " + context.getMessage().getAuthor().mention() + ", next time, try to at least supply a number...");
                    }

                }
            })
            .onFailure((context, failureReason) -> {
                if (failureReason.equals(FailureReason.AUTHOR_MISSING_PERMISSIONS))
                    EasyMessage.send(context.getMessage().getChannel(), context.getMessage().getAuthor().mention() + ", you do not have the `Manage Messages` permission!");
                else EasyMessage.send(context.getMessage().getChannel(),"I seem to be lacking the `Manage Messages` permission!");
            });
}
