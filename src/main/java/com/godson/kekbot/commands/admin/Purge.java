package com.godson.kekbot.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.FailureReason;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;

import java.util.List;


public class Purge {
    public static Command purge = new Command("purge")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Mass deletes X number of messages.")
            .withUsage("{p}purge <number>")
            .userRequiredPermissions(Permission.MESSAGE_MANAGE)
            .botRequiredPermissions(Permission.MESSAGE_MANAGE)
            .deleteCommand(true)
            .onExecuted(context -> {
                String args[] = context.getArgs();
                TextChannel channel = context.getTextChannel();
                if (args.length == 0) {
                    channel.sendMessageAsync(context.getAuthor().getAsMention() + ", next time, try to at least supply a number...", null);
                } else {
                    try {
                        int purge = Integer.valueOf(args[0]);
                        if (purge >= 2 && purge <= 100) {
                            channel.sendMessageAsync("Purging...", msg -> {
                                try {
                                    List<Message> messages = channel.getHistory().retrieve(purge+1);
                                    messages.remove(msg);
                                    channel.deleteMessages(messages);
                                    msg.updateMessage("Sucessfully purged `" + purge + "` messages.");
                                } catch (IndexOutOfBoundsException e) {
                                    int remaining = channel.getHistory().retrieve().size();
                                    List<Message> messages = channel.getHistory().retrieve();
                                    messages.remove(msg);
                                    try {
                                        channel.deleteMessages(messages);
                                        msg.updateMessage("Sucessfully purged `" + remaining + "` messages.");
                                    } catch (IndexOutOfBoundsException er) {
                                        er.printStackTrace();
                                    }
                                }
                            });
                        } else if (purge <= 1) {
                            channel.sendMessageAsync("Sorry m8, that's too low a number. Number must be between 2 and 100.", null);
                        } else if (purge > 100) {
                            channel.sendMessageAsync("Sorry m8, that's too high a number. Number must be between 2 and 100.", null);
                        }
                    } catch (NumberFormatException e) {
                        channel.sendMessageAsync("`" + args[0] + "` is not a number, " + context.getMessage().getAuthor().getAsMention() + ", next time, try to at least supply a number...", null);
                    }

                }
            })
            .onFailure((context, failureReason) -> {
                if (failureReason.equals(FailureReason.AUTHOR_MISSING_PERMISSIONS))
                    context.getTextChannel().sendMessageAsync(context.getAuthor().getAsMention() + ", you do not have the `Manage Messages` permission!", null);
                else context.getTextChannel().sendMessageAsync("I seem to be lacking the `Manage Messages` permission!", null);
            });
}
