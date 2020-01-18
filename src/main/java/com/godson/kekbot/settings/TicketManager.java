package com.godson.kekbot.settings;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.util.Utils;
import com.google.gson.Gson;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TicketManager {

    private static Gson gson = new Gson();
    private static final Timer timer = new Timer();

    public static void addTicket(Ticket ticket) {
        User user = KekBot.jda.getUserById(ticket.getAuthorID());
        KekBot.getCommandClient().getTicketChannel().sendMessage("New ticket made by: **" + user.getName() + "** (Ticket ID: **" + ticket.getID() + "**)").queue();
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

    public static boolean closeTicket(String ticketID, String reply, User replier) {
        if (KekBot.r.table("Tickets").get(ticketID).run(KekBot.conn) != null) {
            if (reply != null) addAdminReply(getTicket(ticketID), reply, replier);
            User user = KekBot.jda.getUserById(getTicket(ticketID).getAuthorID());
            KekBot.r.table("Tickets").get(ticketID).delete().run(KekBot.conn);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (user != null) user.openPrivateChannel().queue(ch -> ch.sendMessage("Your ticket (" + ticketID + ") has been closed.").queue());
                }
            }, TimeUnit.SECONDS.toMillis(2));
            return true;
        } return false;
    }

    public static void addAdminReply(Ticket ticket, String response, User replier) {
        EmbedBuilder eBuilder = new EmbedBuilder();
        String replyAuthor;
        if (replier != null) replyAuthor = replier.getName() + "#" + replier.getDiscriminator() + "**";
        else replyAuthor = "Author Not Found. Close this ticket if the ticket isn't concerning a bug.";

        eBuilder.setColor(Color.BLUE);
        eBuilder.setTitle("Reply:");
        eBuilder.addField("Author:", replyAuthor, true);
        eBuilder.addField("Contents:", response, false);
        if (replier != null) eBuilder.setThumbnail(Utils.getUserAvatarURL(replier));
        else eBuilder.setThumbnail("https://discordapp.com/assets/dd4dbc0016779df1378e7812eabaa04d.png");
        eBuilder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
        KekBot.jda.getUserById(ticket.getAuthorID()).openPrivateChannel().queue(ch -> ch.sendMessage("You have received a reply for your ticket. (`" + ticket.getID() + "`) Use `$ticket view " + ticket.getID() + "` to view your ticket and its replies.").embed(eBuilder.build()).queue());
        ticket.setStatus(Ticket.TicketStatus.AWAITING_REPLY);
        ticket.addReply(replier, response, true);
        KekBot.r.table("Tickets").get(ticket.getID()).update(KekBot.r.hashMap("Replies", ticket.getReplies()).with("Status", ticket.getStatus().name())).run(KekBot.conn);
    }

    public static void addUserReply(Ticket ticket, String response, User replier) {
        String replierName = replier.getName() + "#" + replier.getDiscriminator();
        KekBot.getCommandClient().getTicketChannel().sendMessage("You have received a reply for a ticket. (`" + ticket.getID() + "`)\n**" + replierName
                + "**:\n\n" + response).queue();

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
