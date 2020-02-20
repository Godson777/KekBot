package com.godson.kekbot.settings;

import com.godson.kekbot.KekBot;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.rethinkdb.model.MapObject;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.*;

public class Settings {
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
    @SerializedName("Twitter Feeds")
    private Map<String, String> twitterFeeds = new HashMap<>();
    @SerializedName("Anti-Ad")
    private boolean antiAd = false;
    @SerializedName("Twitter Feed Enabled")
    private boolean twitterFeed = false;
    @SerializedName("Update Channel ID")
    private String updateChannelID;
    @SerializedName("Locale")
    private String locale = "en_US";

    public class AnnounceSettings {
        private String welcomeChannelID;
        private String welcomeMessage;
        private String farewellMessage;

        public AnnounceSettings setWelcomeChannel(TextChannel channel) {
            try {
                welcomeChannelID = channel.getId();
            } catch (NullPointerException e) {
                welcomeChannelID = null;
            }
            return this;
        }

        public AnnounceSettings setWelcomeMessage(String welcomeMessage) {
            this.welcomeMessage = welcomeMessage;
            return this;
        }

        public AnnounceSettings setFarewellMessage(String farewellMessage) {
            this.farewellMessage = farewellMessage;
            return this;
        }

        public String getFarewellMessage() {
            return farewellMessage;
        }

        public String getWelcomeChannelID() {
            return welcomeChannelID;
        }

        public String getWelcomeMessage() {
            return welcomeMessage;
        }
    }

    public Settings(String guildID) {
        this.guildID = guildID;
    }

    public Settings(Guild guild) {
        this.guildID = guild.getId();
    }

    public void setUpdateChannel(TextChannel channel) {
        this.updateChannelID = channel.getId();
    }

    public String getUpdateChannelID() {
        return updateChannelID;
    }

    public Settings setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public Settings setAutoRoleID(String autoRoleID) {
        this.autoRoleID = autoRoleID;
        return this;
    }

    public AnnounceSettings getAnnounceSettings() {
        return announceSettings;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getAutoRoleID() {
        return autoRoleID;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }

    /**
     * Saves all the settings into rethinkdb, allowing for later reading/writing.
     */
    public void save() {
        MapObject settings = KekBot.r.hashMap("Guild ID", guildID)
                .with("Prefix", prefix)
                .with("AutoRole ID", autoRoleID)
                .with("Announce Settings", announceSettings)
                .with("Tags", tags == null ? new TagManager() : tags)
                .with("Quotes", quotes == null ? new QuoteManager() : quotes)
                .with("Free Roles", freeRoles == null ? new ArrayList<Role>() : freeRoles)
                .with("Anti-Ad", antiAd)
                .with("Update Channel ID", updateChannelID)
                .with("Locale", locale == null ? "en_US" : locale)
                .with("Twitter Feeds", twitterFeeds == null ? new HashMap<String, String>() : twitterFeeds)
                .with("Twitter Feed Enabled", twitterFeed);

        if (KekBot.r.table("Settings").get(guildID).run(KekBot.conn) == null) {
            KekBot.r.table("Settings").insert(settings).run(KekBot.conn);
        } else {
            if (twitterFeeds.size() < (long) KekBot.r.table("Settings").get(guildID).getField("Twitter Feeds").count().run(KekBot.conn)) {
                KekBot.r.table("Settings").get(guildID).replace(settings).run(KekBot.conn);
            } else {
                KekBot.r.table("Settings").get(guildID).update(settings).run(KekBot.conn);
            }
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

    public Map<String, String> getTwitterFeeds() {
        return twitterFeeds;
    }

    public boolean isTwitterFeedEnabled() {
        return twitterFeed;
    }

    public Settings toggleTwitterFeed(boolean twitterFeed) {
        this.twitterFeed = twitterFeed;
        return this;
    }

    public Settings followTwitterAccount(String accID, String channelID)  {
        if (twitterFeeds == null) twitterFeeds = new HashMap<>();
        if (!twitterFeeds.containsKey(accID)) twitterFeeds.put(accID, channelID);
        return this;
    }

    public Settings unfollowTwitterAccount(String accID)  {
        if (twitterFeeds == null) twitterFeeds = new HashMap<>();
        twitterFeeds.remove(accID);
        return this;
    }

    public boolean isFollowingTwitterAccount(String accID) {
        if (twitterFeeds == null) twitterFeeds = new HashMap<>();
         return twitterFeeds.keySet().stream().anyMatch(id -> id.equals(accID));
    }

    public String getTwitterFeedChannel(String accID) {
        return twitterFeeds.get(accID);
    }
}