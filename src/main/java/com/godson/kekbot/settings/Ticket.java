package com.godson.kekbot.settings;

import com.google.gson.annotations.SerializedName;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class Ticket {
    @SerializedName("Status")
    private TicketStatus status;
    @SerializedName("ID")
    private String id;
    @SerializedName("Title")
    private String title;
    @SerializedName("Author ID")
    private String authorID;
    @SerializedName("Guild ID")
    private String guildID;
    @SerializedName("Contents")
    private String contents;
    @SerializedName("Replies")
    private List<TicketReply> replies = new ArrayList<>();
    @SerializedName("Time Created")
    private long timeCreated;

    public Ticket() {
        id = generateID();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("EST"));
        timeCreated = calendar.getTimeInMillis();
    }

    public String getID() {
        return id;
    }

    public void resetID() {
        id = generateID();
    }

    /**
     * Generates an ID for the ticket. (Inspired by OddityBot's ticket system.)
     * @return The generated ID.
     */
    private String generateID() {
        StringBuilder id = new StringBuilder();
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmopqrstuvwxyz0123456789";

        for (int i = 0; i < 5; i++) {
            id.append(allowedChars.charAt((int) Math.floor(Math.random() * allowedChars.length())));
        }

        return id.toString();
    }

    public Ticket setStatus(TicketStatus status) {
        this.status = status;
        return this;
    }

    public Ticket setTitle(String title) {
        this.title = title;
        return this;
    }

    public Ticket setAuthor(User user) {
        this.authorID = user.getId();
        return this;
    }

    public Ticket setGuild(Guild guild) {
        this.guildID = guild.getId();
        return this;
    }

    public Ticket setContents(String contents) {
        this.contents = contents;
        return this;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorID() {
        return authorID;
    }

    public String getGuildID() {
        return guildID;
    }

    public String getContents() {
        return contents;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void addReply(User replier, String message, Boolean isAdmin) {
        replies.add(new TicketReply(replier.getId(), message, isAdmin));
    }

    public List<TicketReply> getReplies() {
        return replies;
    }

    public enum TicketStatus {
        OPEN("Open"), AWAITING_REPLY("Sent Reply"), RECEIVED_REPLY("Recieved Reply");

        private String name;

        TicketStatus(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public class TicketReply {
        private String userID;
        private String message;
        private long timeCreated;
        private boolean adminReply;

        private TicketReply(String userID, String message, boolean adminReply) {
            this.userID = userID;
            this.message = message;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("EST"));
            timeCreated = calendar.getTimeInMillis();
            this.adminReply = adminReply;
        }

        public String getUserID() {
            return userID;
        }

        public String getMessage() {
            return message;
        }

        public boolean isAdminReply() {
            return adminReply;
        }

        public long getTimeCreated() {
            return timeCreated;
        }
    }
}
