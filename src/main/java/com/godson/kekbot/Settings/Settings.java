package com.godson.kekbot.Settings;

import com.godson.kekbot.Exceptions.ChannelNotFoundException;
import com.godson.kekbot.Exceptions.MessageNotFoundException;
import com.godson.kekbot.KekBot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Settings {
    private String guildName;
    private String prefix;
    private String autoRoleID;
    private AnnounceSettings announceSettings = new AnnounceSettings();

    private class AnnounceSettings {
        private boolean welcome = false;
        private boolean farewell = false;
        private boolean broadcasts = true;
        private String welcomeChannelID;
        private String welcomeMessage;
        private String farewellChannelID;
        private String farewellMessage;
        private String broadcastChannelID;
    }

    public Settings() {}

    public Settings setName(String guildName) {
        this.guildName = guildName;
        return this;
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

    public TextChannel getWelcomeChannel(JDA jda) {
        if (welcomeChannelIsSet()) return jda.getTextChannelById(announceSettings.welcomeChannelID);
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

    public TextChannel getFarewellChannel(JDA jda) {
        if (farewellChannelIsSet()) return jda.getTextChannelById(announceSettings.farewellChannelID);
        else throw new ChannelNotFoundException("Farewell channel could not be found!");
    }

    public boolean farewellChannelIsSet() {
        return announceSettings.farewellChannelID != null;
    }

    public boolean farewellMessageIsSet() {
        return announceSettings.farewellMessage != null;
    }

    public TextChannel getBroadcastChannel(JDA jda) {
        if (broadcastChannelIsSet()) return jda.getTextChannelById(announceSettings.broadcastChannelID);
        else throw new ChannelNotFoundException("Broadcasts channel could not be found!");
    }

    public boolean broadcastChannelIsSet() {
        return announceSettings.broadcastChannelID != null;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this, this.getClass());
    }

    public void save(Guild guild) {
        File folder = new File("settings\\" + guild.getId());
        File settings = new File("settings\\" + guild.getId() + "\\Settings.json");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try {
            FileWriter writer = new FileWriter(settings);
            writer.write(this.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
