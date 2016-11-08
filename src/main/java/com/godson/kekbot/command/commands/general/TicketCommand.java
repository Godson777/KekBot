package com.godson.kekbot.command.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Settings.Config;
import com.godson.kekbot.Settings.Ticket;
import com.godson.kekbot.Settings.TicketManager;
import com.godson.kekbot.Settings.TicketStatus;
import com.godson.kekbot.XMLUtils;
import net.dv8tion.jda.entities.TextChannel;
import org.jdom2.JDOMException;

import java.io.IOException;

public class TicketCommand {
    public static Command ticket = new Command("ticket")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Sends a ticket to the bot dev. Use only when KekBot gives you any issues or you require assistance with something.")
            .withUsage("{p}ticket <title>|<message>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                Config config = GSONUtils.getConfig();
                if (rawSplit.length == 1) {
                    channel.sendMessageAsync("I can't send a ticket without a title and message!", null);
                } else {
                    String ticketInfo[] = rawSplit[1].split("\\u007C", 2);
                    if (ticketInfo.length == 1) {
                        channel.sendMessageAsync("Contents of your ticket must be seperated with the vertical line ( **|** ). `Example: \"Title|Contents\"`", null);
                    } else {
                        Ticket ticket = new Ticket().setTitle(ticketInfo[0]).setContents(ticketInfo[1]).setAuthor(context.getAuthor()).setGuild(context.getGuild()).setStatus(TicketStatus.OPEN);
                        TicketManager manager = GSONUtils.getTicketManager();
                        manager.addTicket(ticket);
                        manager.save();
                        channel.sendMessageAsync(context.getMessage().getAuthor().getAsMention() + " Thanks for submitting your ticket!", null);
                        KekBot.jdas[0].getUserById(config.getBotOwner()).getPrivateChannel().sendMessageAsync("New ticket made by: **" + context.getMessage().getAuthor().getUsername() + "** (ID: **" + context.getMessage().getAuthor().getId() + "**)", null);
                    }
                }
            });
}
