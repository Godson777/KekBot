package com.godson.kekbot.settings;

import com.godson.kekbot.exceptions.ChannelNotFoundException;
import com.godson.kekbot.exceptions.MessageNotFoundException;
import com.godson.kekbot.KekBot;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.rethinkdb.model.MapObject;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

public class Settings {
    //Remove these annotations when porting from 1.4 settings to 1.5.
    @SerializedName("Guild ID")
    private String guildID;
    @SerializedName("Prefix")
    private String prefix;
    @SerializedName("AutoRole ID")
    private String autoRoleID;
    @SerializedName("Announce Settings")
    public AnnounceSettings announceSettings = new AnnounceSettings();
    @SerializedName("Tags")
    public TagManager tags = new TagManager();
    @SerializedName("Quotes")
    public List<String> quotes;

    public class AnnounceSettings {
        private boolean welcome = false;
        private boolean farewell = false;
        private boolean broadcasts = false;
        private String welcomeChannelID;
        private String welcomeMessage;
        private String farewellChannelID;
        private String farewellMessage;
        private String broadcastChannelID;
    }

    public Settings(String guildID) {
        this.guildID = guildID;
    }

    public Settings setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public Settings setAutoRoleID(String autoRoleID) {
        this.autoRoleID = autoRoleID;
        return this;
    }

    public Settings toggleWelcome(boolean status) {
        announceSettings.welcome = status;
        return this;
    }

    public Settings setWelcomeChannel(TextChannel channel) {
        try {
            announceSettings.welcomeChannelID = channel.getId();
        } catch (NullPointerException e) {
            announceSettings.welcomeChannelID = null;
        }
        return this;
    }

    public Settings setWelcomeMessage(String welcomeMessage) {
        announceSettings.welcomeMessage = welcomeMessage;
        return this;
    }

    public Settings toggleFarewell(boolean status) {
        announceSettings.farewell = status;
        return this;
    }

    public Settings setFarewellChannel(TextChannel channel) {
        try {
            announceSettings.farewellChannelID = channel.getId();
        } catch (NullPointerException e) {
            announceSettings.farewellChannelID = null;
        }
        return this;
    }

    public Settings setFarewellMessage(String farewellMessage) {
        announceSettings.farewellMessage = farewellMessage;
        return this;
    }

    public Settings toggleBroadcasts(boolean status) {
        announceSettings.broadcasts = status;
        return this;
    }

    public Settings setBroadcastChannel(TextChannel channel) {
        try {
            announceSettings.broadcastChannelID = channel.getId();
        } catch (NullPointerException e) {
            announceSettings.broadcastChannelID = null;
        }
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getAutoRoleID() {
        return autoRoleID;
    }

    public boolean welcomeEnabled() {
        return announceSettings.welcome;
    }

    public boolean farewellEnabled() {
        return announceSettings.farewell;
    }

    public boolean broadcastsEnabled() {
        return announceSettings.broadcasts;
    }

    public String getWelcomeMessage() {
        if (welcomeMessageIsSet()) return announceSettings.welcomeMessage;
        else throw new MessageNotFoundException("Welcome message could not be found!");
    }

    public TextChannel getWelcomeChannel(Guild guild) {
        if (welcomeChannelIsSet()) return guild.getTextChannelById(announceSettings.welcomeChannelID);
        else throw new ChannelNotFoundException("Welcome channel could not be found!");
    }

    public boolean welcomeChannelIsSet() {
        return announceSettings.welcomeChannelID != null;
    }

    public boolean welcomeMessageIsSet() {
        return announceSettings.welcomeMessage != null;
    }

    public String getFarewellMessage() {
        if (farewellChannelIsSet()) return announceSettings.farewellMessage;
        else throw new MessageNotFoundException("Farewell message could not be found!");
    }

    public TextChannel getFarewellChannel(Guild guild) {
        if (farewellChannelIsSet()) return guild.getTextChannelById(announceSettings.farewellChannelID);
        else throw new ChannelNotFoundException("Farewell channel could not be found!");
    }

    public boolean farewellChannelIsSet() {
        return announceSettings.farewellChannelID != null;
    }

    public boolean farewellMessageIsSet() {
        return announceSettings.farewellMessage != null;
    }

    public TextChannel getBroadcastChannel(Guild guild) {
        if (broadcastChannelIsSet()) return guild.getTextChannelById(announceSettings.broadcastChannelID);
        else throw new ChannelNotFoundException("Broadcasts channel could not be found!");
    }

    public boolean broadcastChannelIsSet() {
        return announceSettings.broadcastChannelID != null;
    }

    public void save() {
        MapObject settings = KekBot.r.hashMap("Guild ID", guildID)
                .with("Prefix", prefix)
                .with("AutoRole ID", autoRoleID)
                .with("Announce Settings", announceSettings)
                .with("Tags", tags)
                .with("Quotes", quotes);

        if (KekBot.r.table("Settings").get(guildID).run(KekBot.conn) == null) {
            KekBot.r.table("Settings").insert(settings).run(KekBot.conn);
        } else {
            KekBot.r.table("Settings").update(settings).run(KekBot.conn);
        }
    }

    public static Settings getSettings(Guild guild) {
        return getSettings(guild.getId());
    }

    public static Settings getSettings(String guildID) {
        if (KekBot.r.table("Settings").get(guildID).run(KekBot.conn) != null) {
            Gson gson = new Gson();
            return gson.fromJson((String) KekBot.r.table("Settings").get(guildID).toJson().run(KekBot.conn), Settings.class);
        } else return new Settings(guildID);
    }

    public QuoteManager getQuotes() {
        if (quotes != null) return new QuoteManager(quotes);
        else return new QuoteManager();
    }

    public TagManager getTags() {
        return tags;
    }
}
