package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

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
                    channel.sendMessage(context.getMessage().getAuthor().getAvatarUrl()).queue();
                } else {
                    try {
                        User mention = context.getMessage().getMentionedUsers().get(0);
                        channel.sendMessage(mention.getAvatarUrl()).queue();
                    } catch (IndexOutOfBoundsException e) {
                        try {
                            channel.sendMessage(context.getGuild().getMembersByName(rawSplit[1], true).get(0).getUser().getAvatarUrl()).queue();
                        } catch (IndexOutOfBoundsException er) {
                            channel.sendMessage("I couldn't find a user with that name/nickname!").queue();
                        }
                    }
                }
            });
}
