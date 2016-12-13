package com.godson.kekbot.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Settings.Config;
import com.godson.kekbot.Settings.Ticket;
import com.godson.kekbot.Settings.TicketManager;
import com.godson.kekbot.Settings.TicketStatus;
import net.dv8tion.jda.core.entities.TextChannel;

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
                    channel.sendMessage("I can't send a ticket without a title and message!").queue();
                } else {
                    String ticketInfo[] = rawSplit[1].split("\\u007C", 2);
                    if (ticketInfo[0].startsWith(" ")) ticketInfo[0] = ticketInfo[0].replaceFirst("([ ]+)", "");
                    if (ticketInfo[0].endsWith(" ")) ticketInfo[0] = ticketInfo[0].replaceAll("([ ]+$)", "");
                    if (ticketInfo.length == 1) {
                        channel.sendMessage("Contents of your ticket must be seperated with the vertical line ( **|** ). `Example: \"Title|Contents\"`").queue();
                    } else {
                        Ticket ticket = new Ticket().setTitle(ticketInfo[0]).setContents(ticketInfo[1]).setAuthor(context.getAuthor()).setGuild(context.getGuild()).setStatus(TicketStatus.OPEN);
                        TicketManager manager = GSONUtils.getTicketManager();
                        manager.addTicket(ticket);
                        manager.save();
                        channel.sendMessage(context.getMessage().getAuthor().getAsMention() + " Thanks for submitting your ticket!").queue();
                        KekBot.jdas[0].getUserById(config.getBotOwner()).getPrivateChannel().sendMessage("New ticket made by: **" + context.getMessage().getAuthor().getName() + "** (ID: **" + context.getMessage().getAuthor().getId() + "**)").queue();
                    }
                }
            });
}
