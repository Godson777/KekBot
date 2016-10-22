package com.godson.kekbot.command.commands.general;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.XMLUtils;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class Shutdown {
    public static Command shutdown = new Command("shutdown")
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().getID().equals(XMLUtils.getBotOwner())) {
                    try {
                        new MessageBuilder(KekBot.client).withChannel(context.getMessage().getChannel()).withContent("That's all from me! Hope you had a gay old time!").send();
                        KekBot.client.logout();
                        System.exit(0);
                    } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
                        e.printStackTrace();
                    }
                }
            });
}
