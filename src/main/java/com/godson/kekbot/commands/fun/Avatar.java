package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class Avatar {
    public static Command avatar = new Command("avatar")
            .withAliases("ava")
            .withCategory(CommandCategory.FUN)
            .withDescription("Sends a larger version of the specified user's avatar. If there is no user specified, it'll send your avatar.")
            .withUsage("{p}avatar <user>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                if (rawSplit.length == 1) {
                    channel.sendMessageAsync(context.getMessage().getAuthor().getAvatarUrl(), null);
                } else {
                    try {
                        User mention = context.getMessage().getMentionedUsers().get(0);
                        channel.sendMessageAsync(mention.getAvatarUrl(), null);
                    } catch (IndexOutOfBoundsException e) {
                        try {
                            channel.sendMessageAsync(context.getGuild().getUsersByName(rawSplit[1]).get(0).getAvatarUrl(), null);
                        } catch (IndexOutOfBoundsException er) {
                            channel.sendMessageAsync("I couldn't find a user with that name/nickname!", null);
                        }
                    }
                }
            });
}
