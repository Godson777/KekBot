package com.godson.kekbot.settings;


import net.dv8tion.jda.api.entities.User;

public class Tag {
    private String name;
    private String contents;
    private String creatorID;
    private String timeCreated;
    private String timeLastEdited;

    public Tag(String name) {
        this.name = name;
    }

    public Tag setContents(String contents) {
        this.contents = contents;
        return this;
    }

    public Tag setCreator(User creator) {
        creatorID = creator.getId();
        return this;
    }

    public Tag setTime(String timeCreated) {
        this.timeCreated = timeCreated;
        return this;
    }

    public Tag setEditedTime(String timeLastEdited) {
        this.timeLastEdited = timeLastEdited;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getContents() {
        return contents;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public String getTimeLastEdited() {
        return timeLastEdited;
    }
}
