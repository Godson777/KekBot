package com.godson.kekbot.command.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.XMLUtils;
import org.jdom2.JDOMException;
import sx.blah.discord.handle.obj.IChannel;

import java.io.IOException;

public class Ticket {
    public static Command ticket = new Command("ticket")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Sends a ticket to the bot dev. Use only when KekBot gives you any issues or you require assistance with something.")
            .withUsage("{p}ticket <title>|<message>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getContent().split(" ", 2);
                IChannel channel = context.getMessage().getChannel();
                if (rawSplit.length == 1) {
                    EasyMessage.send(channel, "I can't send a ticket without a title and message!");
                } else {
                    String ticketInfo[] = rawSplit[1].split("\\u007C", 2);
                    if (ticketInfo.length == 1) {
                        EasyMessage.send(channel, "Contents of your ticket must be seperated with the vertical line ( **|** ). `Example: \"Title|Contents\"`");
                    } else {
                        try {
                            XMLUtils.addTicket(context.getMessage().getAuthor().getID(), ticketInfo[0], ticketInfo[1], context.getMessage().getGuild());
                            EasyMessage.send(channel, context.getMessage().getAuthor().mention() + " Thanks for submitting your ticket!");
                            EasyMessage.send(channel.getClient().getUserByID("99405418077364224"), "New ticket made by: **" + context.getMessage().getAuthor().getName() + "** (ID: **" + context.getMessage().getAuthor().getID() + "**)");
                        } catch (JDOMException | IOException e) {
                            EasyMessage.send(channel, "A **fatal** error has occurred! This has been reported to the bot dev!");
                            EasyMessage.send(channel.getClient().getUserByID("99405418077364224"), "New ticket made by: **" + context.getMessage().getAuthor().getName() + "** (ID: **" + context.getMessage().getAuthor().getID() + "**)");
                        }
                    }
                }
            });
}
