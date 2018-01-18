package com.godson.kekbot.settings;

import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.net.Cursor;
import com.sedmelluq.discord.lavaplayer.remote.RemoteNode;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TicketManager {

    private static Type type = new TypeToken<List<Ticket>>(){}.getType();
    private static Gson gson = new Gson();

    public static void addTicket(Ticket ticket) {
        User user = KekBot.jda.getUserById(ticket.getAuthorID());
        KekBot.getCommandClient().ticketChannel.sendMessage("New ticket made by: **" + user.getName() + "** (ID: **" + user.getId() + "**)").queue();
        while (KekBot.r.table("Tickets").get(ticket.getID()).run(KekBot.conn) != null) {
            ticket.resetID();
        }
        MapObject object = KekBot.r.hashMap("ID", ticket.getID())
                .with("Status", ticket.getStatus().name())
                .with("Title", ticket.getTitle())
                .with("Contents", ticket.getContents())
                .with("Author ID", ticket.getAuthorID())
                .with("Guild ID", ticket.getGuildID())
                .with("Replies", ticket.getReplies())
                .with("Time Created", ticket.getTimeCreated());
        KekBot.r.table("Tickets").insert(object).run(KekBot.conn);
    }

    public static boolean closeTicket(String ticketID) {
        User user = KekBot.jda.getUserById(getTicket(ticketID).getAuthorID());
        if (KekBot.r.table("Tickets").get(ticketID).run(KekBot.conn) != null) {
            KekBot.r.table("Tickets").get(ticketID).delete().run(KekBot.conn);
            if (user != null) user.openPrivateChannel().queue(ch -> ch.sendMessage("Your ticket (" + ticketID + ") has been closed.").queue());
            return true;
        } return false;
    }

    public static void addAdminReply(Ticket ticket, String response, User replier) {
        String replierName = replier.getName() + "#" + replier.getDiscriminator();
        KekBot.jda.getUserById(ticket.getAuthorID()).openPrivateChannel().queue(ch -> ch.sendMessage("You have received a reply for your ticket. (`" + ticket.getID() + "`) Use `$ticket view " + ticket.getID() + "` to view your ticket and its replies.").queue());
        ticket.setStatus(Ticket.TicketStatus.AWAITING_REPLY);
        ticket.addReply(replier, response, true);
        KekBot.r.table("Tickets").get(ticket.getID()).update(KekBot.r.hashMap("Replies", ticket.getReplies()).with("Status", ticket.getStatus().name())).run(KekBot.conn);
    }

    public static void addUserReply(Ticket ticket, String response, User replier) {
        String replierName = replier.getName() + "#" + replier.getDiscriminator();
        KekBot.jda.getUserById(Config.getConfig().getBotOwner()).openPrivateChannel().queue(ch -> ch.sendMessage("You have received a reply for a ticket. (`" + ticket.getID() + "`)\n**" + replierName
                + "**:\n\n" + response).queue());

        ticket.setStatus(Ticket.TicketStatus.RECEIVED_REPLY);
        ticket.addReply(replier, response, false);
        KekBot.r.table("Tickets").get(ticket.getID()).update(KekBot.r.hashMap("Replies", ticket.getReplies()).with("Status", ticket.getStatus().name())).run(KekBot.conn);
    }

    public static List<Ticket> getTickets() {
        if (!(boolean) KekBot.r.table("Tickets").isEmpty().run(KekBot.conn)) {
            Cursor cursor = KekBot.r.table("Tickets").run(KekBot.conn);
            List<org.json.simple.JSONObject> list = cursor.bufferedItems();
            List<Ticket> tickets = new ArrayList<>();
            list.forEach(json -> tickets.add(gson.fromJson(json.toJSONString(), Ticket.class)));
            return tickets;
        } else return new ArrayList<>();
    }

    public static Ticket getTicket(String ticketID) {
        if (KekBot.r.table("Tickets").get(ticketID).run(KekBot.conn) != null) return gson.fromJson((String) KekBot.r.table("Tickets").get(ticketID).toJson().run(KekBot.conn), Ticket.class);
        return null;
    }
}
