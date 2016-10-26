package com.godson.kekbot.command.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.XMLUtils;
import net.dv8tion.jda.entities.TextChannel;
import org.jdom2.JDOMException;

import java.io.IOException;

public class Ticket {
    public static Command ticket = new Command("ticket")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Sends a ticket to the bot dev. Use only when KekBot gives you any issues or you require assistance with something.")
            .withUsage("{p}ticket <title>|<message>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                if (rawSplit.length == 1) {
                    channel.sendMessage("I can't send a ticket without a title and message!");
                } else {
                    String ticketInfo[] = rawSplit[1].split("\\u007C", 2);
                    if (ticketInfo.length == 1) {
                        channel.sendMessage("Contents of your ticket must be seperated with the vertical line ( **|** ). `Example: \"Title|Contents\"`");
                    } else {
                        try {
                            XMLUtils.addTicket(context.getMessage().getAuthor().getId(), ticketInfo[0], ticketInfo[1], context.getGuild());
                            channel.sendMessage(context.getMessage().getAuthor().getAsMention() + " Thanks for submitting your ticket!");
                            KekBot.client.getUserById(XMLUtils.getBotOwner()).getPrivateChannel().sendMessage("New ticket made by: **" + context.getMessage().getAuthor().getUsername() + "** (ID: **" + context.getMessage().getAuthor().getId() + "**)");
                        } catch (JDOMException | IOException e) {
                            channel.sendMessage("A **fatal** error has occurred! This has been reported to the bot dev!");
                            KekBot.client.getUserById(XMLUtils.getBotOwner()).getPrivateChannel().sendMessage("New ticket made by: **" + context.getMessage().getAuthor().getUsername() + "** (ID: **" + context.getMessage().getAuthor().getId() + "**)");
                        }
                    }
                }
            });
}
