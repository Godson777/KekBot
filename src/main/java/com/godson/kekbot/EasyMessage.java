package com.godson.kekbot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import static java.lang.System.out;

public class EasyMessage {
    public static void send(IUser user, String content) {
        RequestBuffer.request(() -> {
            try {
                new MessageBuilder(KekBot.client).withChannel(user.getOrCreatePMChannel()).withContent(content).send();
            } catch (DiscordException | MissingPermissionsException e) {
                e.printStackTrace();
            }
        });

    }

    public static IMessage send(IChannel channel, String content) {
        final IMessage[] message = {null};
        RequestBuffer.request(() -> {
            try {
                message[0] = new MessageBuilder(KekBot.client).withChannel(channel).withContent(content).send();
            } catch (DiscordException e) {
                e.printStackTrace();
            } catch (MissingPermissionsException e) {
                out.println("I do not have the 'Send Messages' permission in server: " + channel.getGuild().getName() + " - #" + channel.getName() + "! Aborting!");
            }
        });
        return message[0];
    }

    public static void editMessage(IMessage message, String content) {
        RequestBuffer.request(() -> {
            try {
                message.edit(content);
            } catch (DiscordException | MissingPermissionsException e) {
                e.printStackTrace();
            }
        });
    }
}
