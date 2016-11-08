package com.godson.kekbot;

import com.godson.kekbot.Settings.*;
import com.google.gson.Gson;
import net.dv8tion.jda.entities.Guild;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class GSONUtils {

    public static Settings getSettings(Guild guild) {
        Settings settings = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader("settings\\" + guild.getId() + "\\Settings.json"));
            Gson gson = new Gson();
            settings = gson.fromJson(br, Settings.class);
            br.close();
        } catch (FileNotFoundException e) {
            //do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
        return settings;
    }

    public static TagManager getTagManager(Guild guild) {
        TagManager manager = new TagManager();
        try {
            BufferedReader br = new BufferedReader(new FileReader("settings\\" + guild.getId() + "\\Tags.json"));
            Gson gson = new Gson();
            manager = gson.fromJson(br, TagManager.class);
            br.close();
        } catch (FileNotFoundException e) {
            //do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
        return manager;
    }

    public static Quotes getQuotes(Guild guild) {
        Quotes quotes = new Quotes();
        try {
            BufferedReader br = new BufferedReader(new FileReader("settings\\" + guild.getId() + "\\Quotes.json"));
            Gson gson = new Gson();
            quotes = gson.fromJson(br, Quotes.class);
            br.close();
        } catch (FileNotFoundException e) {
            //do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
        return quotes;
    }

    public static Config getConfig() {
        Config config = new Config();
        try {
            BufferedReader br = new BufferedReader(new FileReader("config.json"));
            Gson gson = new Gson();
            config = gson.fromJson(br, Config.class);
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("config.json not found! What have you done with it?!");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static TicketManager getTicketManager() {
        TicketManager manager = new TicketManager();
        try {
            BufferedReader br = new BufferedReader(new FileReader("tickets.json"));
            Gson gson = new Gson();
            manager = gson.fromJson(br, TicketManager.class);
            br.close();
        } catch (FileNotFoundException e) {
            manager.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return manager;
    }
}
