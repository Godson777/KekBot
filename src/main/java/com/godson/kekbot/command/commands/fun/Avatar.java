package com.godson.kekbot.command.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.EasyMessage;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class Avatar {
    public static Command avatar = new Command("avatar")
            .withAliases("ava")
            .withCategory(CommandCategory.FUN)
            .withDescription("Sends a larger version of the specified user's avatar. If there is no user specified, it'll send your avatar.")
            .withUsage("{p}avatar <user>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getContent().split(" ", 2);
                IChannel channel = context.getMessage().getChannel();
                if (rawSplit.length == 1) {
                    EasyMessage.send(channel, context.getMessage().getAuthor().getAvatarURL());
                } else {
                    try {
                        IUser mention = context.getMessage().getMentions().get(0);
                        EasyMessage.send(channel, mention.getAvatarURL());
                    } catch (IndexOutOfBoundsException e) {
                        try {
                            EasyMessage.send(channel, context.getMessage().getGuild().getUsersByName(rawSplit[1]).get(0).getAvatarURL());
                        } catch (IndexOutOfBoundsException er) {
                            EasyMessage.send(channel, "I couldn't find a user with that name/nickname!");
                        }
                    }
                }
            });
}
