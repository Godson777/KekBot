package com.godson.kekbot.Settings;

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;

public class Ticket {
    private TicketStatus status;
    private String title;
    private String authorID;
    private String guildID;
    private String contents;

    public Ticket() {}

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
}
