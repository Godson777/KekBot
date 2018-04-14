package com.godson.kekbot.settings;

import com.godson.kekbot.KekBot;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.rethinkdb.model.MapObject;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
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
    private AnnounceSettings announceSettings = new AnnounceSettings();
    @SerializedName("Tags")
    private TagManager tags = new TagManager();
    @SerializedName("Quotes")
    private QuoteManager quotes = new QuoteManager();
    @SerializedName("Free Roles")
    private List<String> freeRoles = new ArrayList<>();
    @SerializedName("Anti-Ad")
    private boolean antiAd = false;

    public class AnnounceSettings {
        private String welcomeChannelID;
        private String welcomeMessage;
        private String farewellMessage;
    }

    public Settings(String guildID) {
        this.guildID = guildID;
    }

    public Settings(Guild guild) {
        this.guildID = guild.getId();
    }

    public Settings setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public Settings setAutoRoleID(String autoRoleID) {
        this.autoRoleID = autoRoleID;
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

    public Settings setFarewellMessage(String farewellMessage) {
        announceSettings.farewellMessage = farewellMessage;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getAutoRoleID() {
        return autoRoleID;
    }

    public String getWelcomeMessage() {
        return announceSettings.welcomeMessage;
    }

    public String getWelcomeChannel() {
        return announceSettings.welcomeChannelID;
    }

    public String getFarewellMessage() {
        return announceSettings.farewellMessage;
    }

    /**
     * Saves all the settings into rethinkdb, allowing for later reading/writing.
     */
    public void save() {
        MapObject settings = KekBot.r.hashMap("Guild ID", guildID)
                .with("Prefix", prefix)
                .with("AutoRole ID", autoRoleID)
                .with("Announce Settings", announceSettings)
                .with("Tags", tags)
                .with("Quotes", quotes)
                .with("Free Roles", (freeRoles == null ? new ArrayList<Role>() : freeRoles))
                .with("Anti-Ad", antiAd);

        if (KekBot.r.table("Settings").get(guildID).run(KekBot.conn) == null) {
            KekBot.r.table("Settings").insert(settings).run(KekBot.conn);
        } else {
            KekBot.r.table("Settings").get(guildID).update(settings).run(KekBot.conn);
        }
    }

    /**
     * Gets a settings object based on a guild.
     * @param guild The guild we're getting settings for.
     * @return a {@link Settings} object containing data related to the guild.
     * @see Settings#getSettings(String)
     */
    public static Settings getSettings(Guild guild) {
        return getSettings(guild.getId());
    }

    /**
     * Gets a settings object based on a guild's ID.
     * @param guildID The guild's ID we're using to get get settings for.
     * @return a {@link Settings} object containing data related to the guild.
     */
    public static Settings getSettings(String guildID) {
        if (KekBot.r.table("Settings").get(guildID).run(KekBot.conn) != null) {
            Gson gson = new Gson();
            return gson.fromJson((String) KekBot.r.table("Settings").get(guildID).toJson().run(KekBot.conn), Settings.class);
        } else return new Settings(guildID);
    }

    /**
     * Gets a settings object based on a guild, may return null if no settings exist.
     * @param guildID The guild's ID we're using to get get settings for.
     * @return a {@link Settings} object containing data related to the guild, or null if no settings exist.
     */
    public static Settings getSettingsOrNull(String guildID) {
        if (KekBot.r.table("Settings").get(guildID).run(KekBot.conn) != null) {
            Gson gson = new Gson();
            return gson.fromJson((String) KekBot.r.table("Settings").get(guildID).toJson().run(KekBot.conn), Settings.class);
        } else return null;
    }


    public QuoteManager getQuotes() {
        return quotes;
    }

    public TagManager getTags() {
        return tags;
    }

    public List<String> getFreeRoles() {
        return freeRoles;
    }

    public Settings addFreeRole(String roleID) {
        if (!freeRoles.contains(roleID)) freeRoles.add(roleID);
        return this;
    }

    public Settings removeFreeRole(String roleID) {
        if (freeRoles.contains(roleID)) freeRoles.remove(roleID);
        return this;
    }

    public Settings setAntiAd(boolean antiAd) {
        this.antiAd = antiAd;
        return this;
    }

    public boolean isAntiAdEnabled() {
        return antiAd;
    }
}
