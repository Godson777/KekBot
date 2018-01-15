package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.menu.EmbedPaginator;
import com.godson.kekbot.settings.Ticket;
import com.godson.kekbot.settings.TicketManager;
import com.jagrosh.jdautilities.menu.pagination.PaginatorBuilder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class TicketCommand extends Command {

    public TicketCommand() {
        name = "ticket";
        description = "Sends a ticket to the bot dev. Use only when KekBot gives you any issues or you require assistance with something.";
        usage.add("ticket");
        usage.add("ticket <title> | <contents>");
        usage.add("ticket view <ID>");
        usage.add("ticket reply <ID> <message>");
        commandState = CommandState.BOTH;
        category = new Category("General");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (!event.getClient().canUseTickets(event.getAuthor().getId())) return;
        if (event.getArgs().length > 0) {
            switch (event.getArgs()[0].toLowerCase()) {
                case "list":
                    if (!event.isBotAdmin()) return;

                    List<Ticket> tickets = TicketManager.getTickets();
                    if (tickets.size() > 0) {
                        PaginatorBuilder builder = new PaginatorBuilder();
                        List<String> list = TicketManager.getTickets().stream().map(ticket -> ticket.getID() + " - " + "\"" + (ticket.getTitle().length() >= 24 ? ticket.getTitle().substring(0, 25) + "..." : ticket.getTitle()) + "\"" + StringUtils.repeat(" ", 30-(ticket.getTitle().length() >= 20 ? 28 : ticket.getTitle().length())) + "**" + ticket.getStatus().getName() + "**").collect(Collectors.toList());
                        builder.addItems(list.toArray(new String[list.size()]));
                        builder.setEventWaiter(KekBot.waiter);
                        builder.addUsers(event.getAuthor());
                        builder.setText("List of Tickets:");
                        builder.waitOnSinglePage(true);
                        builder.setItemsPerPage(10);
                        builder.build().display(event.getChannel());
                    } else {
                        event.getChannel().sendMessage("There are no tickets to list!").queue();
                    }
                    break;
                case "view":
                    if (event.getArgs().length > 1) {
                        if (TicketManager.getTickets().size() == 0) {
                            if (event.isBotAdmin()) event.getChannel().sendMessage("There are no tickets to view!").queue();
                        } else {
                            Ticket ticket = TicketManager.getTicket(event.getArgs()[1]);
                            if (ticket == null) {
                                event.getChannel().sendMessage("Ticket not found.").queue();
                                return;
                            }
                            if (!ticket.getAuthorID().equals(event.getAuthor().getId()) && !event.isBotAdmin()) {
                                event.getChannel().sendMessage("You cannot view a ticket you haven't created!").queue();
                                return;
                            }

                            String ticketTitle = ticket.getTitle();
                            String ticketContents = ticket.getContents();
                            String ticketAuthor;
                            String ticketGuild;
                            User author = KekBot.jda.getUserById(ticket.getAuthorID());
                            Guild authorGuild = KekBot.jda.getGuildById(ticket.getGuildID());
                            if (author != null) ticketAuthor = author.getName() + "#" + author.getDiscriminator() + " (ID: **" + ticket.getAuthorID() + ")" + "**";
                            else ticketAuthor = "Author Not Found. Close this ticket if the ticket isn't concerning a bug.";
                            if (authorGuild != null) ticketGuild = authorGuild.getName() + " (ID: **" + ticket.getGuildID() + ")" + "**";
                            else ticketGuild = "Guild Not Found. Close this ticket if the ticket isn't concerning a bug.";
                            String ticketStatus = ticket.getStatus().getName();

                            EmbedPaginator.Builder builder = new EmbedPaginator.Builder();
                            builder.setEventWaiter(KekBot.waiter);
                            EmbedBuilder eBuilder = new EmbedBuilder();

                            eBuilder.setTitle("Ticket:");
                            eBuilder.addField("Title:", ticketTitle, true);
                            eBuilder.addField("Status:", ticketStatus, true);
                            eBuilder.addField("Author", ticketAuthor, true);
                            eBuilder.addField("Server:", ticketGuild, true);
                            eBuilder.addField("Contents:", ticketContents, false);
                            eBuilder.setTimestamp(Instant.ofEpochMilli(ticket.getTimeCreated()));
                            switch (ticket.getStatus()) {
                                case OPEN: eBuilder.setColor(Color.GREEN);
                                break;
                                case AWAITING_REPLY: eBuilder.setColor(Color.ORANGE);
                                break;
                                case RECEIVED_REPLY: eBuilder.setColor(Color.YELLOW);
                                break;
                            }
                            if (author != null) eBuilder.setThumbnail(Utils.getUserAvatarURL(author));
                            else eBuilder.setThumbnail("https://discordapp.com/assets/dd4dbc0016779df1378e7812eabaa04d.png");

                            builder.addItems(eBuilder.build());

                            List<Ticket.TicketReply> replies = ticket.getReplies();
                            for (Ticket.TicketReply reply : replies) {
                                eBuilder = new EmbedBuilder();
                                author = KekBot.jda.getUserById(reply.getUserID());
                                String replyAuthor;
                                if (author != null) replyAuthor = author.getName() + "#" + author.getDiscriminator() + " (ID: **" + reply.getUserID() + ")" + "**";
                                else replyAuthor = "Author Not Found. Close this ticket if the ticket isn't concerning a bug.";

                                eBuilder.setColor(reply.isAdminReply() ? Color.BLUE : Color.CYAN);
                                eBuilder.setTitle("Reply:");
                                eBuilder.addField("Author:", replyAuthor, true);
                                eBuilder.addField("Contents:", reply.getMessage(), false);
                                if (author != null) eBuilder.setThumbnail(Utils.getUserAvatarURL(author));
                                else eBuilder.setThumbnail("https://discordapp.com/assets/dd4dbc0016779df1378e7812eabaa04d.png");
                                eBuilder.setTimestamp(Instant.ofEpochMilli(reply.getTimeCreated()));

                                builder.addItems(eBuilder.build());
                            }

                            builder.setUsers(event.getAuthor());
                            builder.build().display(event.getChannel());
                        }
                    } else event.getChannel().sendMessage("No ticket specified.").queue();
                    break;
                case "reply":
                    if (event.getArgs().length > 1) {
                        if (TicketManager.getTickets().size() == 0) {
                            if (event.isBotAdmin())
                                event.getChannel().sendMessage("You don't have any tickets to reply to!").queue();
                            return;
                        }
                        Ticket ticket = TicketManager.getTicket(event.getArgs()[1]);
                        if (ticket == null) {
                            event.getChannel().sendMessage("Ticket not found.").queue();
                            return;
                        }
                        if (!ticket.getAuthorID().equals(event.getAuthor().getId()) && !event.isBotAdmin()) {
                            event.getChannel().sendMessage("You cannot reply to a ticket you haven't created!").queue();
                            return;
                        }

                        if (event.getArgs().length > 2) {
                            if (!ticket.getAuthorID().equals(event.getAuthor().getId()))
                                TicketManager.addAdminReply(ticket, event.combineArgs(2), event.getAuthor());
                            else TicketManager.addUserReply(ticket, event.combineArgs(2), event.getAuthor());
                            event.getChannel().sendMessage("Reply Sent!").queue();
                        } else {
                            event.getChannel().sendMessage("No reply message specified.").queue();
                        }
                    } else {
                        event.getChannel().sendMessage("No ticket specified.").queue();
                    }
                    break;
                case "close":
                    if (!event.isBotAdmin()) return;
                    if (event.getArgs().length > 1) {
                        if (TicketManager.getTickets().size() == 0) {
                            event.getChannel().sendMessage("You don't have any tickets to close!").queue();
                            return;
                        }

                        boolean closed = TicketManager.closeTicket(event.getArgs()[1]);
                        if (!closed) event.getChannel().sendMessage("Ticket not found.").queue();
                        else event.getChannel().sendMessage("Ticket closed.").queue();
                    }
                    break;
                default:
                    String t = event.combineArgs();
                    String ticketInfo[] = t.split("\\u007C", 2);
                    ticketInfo[0] = Utils.removeWhitespaceEdges(ticketInfo[0]);
                    if (ticketInfo.length == 1) {
                        event.getChannel().sendMessage("Contents of your ticket must be seperated with the vertical line ( **|** ). `Example: \"Title|Contents\"`").queue();
                    } else {
                        ticketInfo[1] = Utils.removeWhitespaceEdges(ticketInfo[1]);
                        Ticket ticket = new Ticket().setTitle(ticketInfo[0]).setContents(ticketInfo[1]).setAuthor(event.getAuthor()).setGuild(event.getGuild()).setStatus(Ticket.TicketStatus.OPEN);
                        TicketManager.addTicket(ticket);
                        event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Your ticket has been submitted, thanks! (Ticket ID: `" + ticket.getID() + "`)").queue();
                    }
                    break;
            }
        } else {
            if (event.isBotOwner()) event.getChannel().sendMessage("You have **" + TicketManager.getTickets().size() + (TicketManager.getTickets().size() == 1 ? "** ticket." : "** tickets.")).queue();
            else event.getChannel().sendMessage("I can't send a ticket without a title and message!").queue();
        }
    }
}
