package com.godson.kekbot;

import com.godson.kekbot.Objects.UDictionary;
import com.godson.kekbot.Settings.*;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.entities.Guild;

import java.io.*;

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

    public static UDictionary getUDResults(String word) {
        UDictionary uDictionary = new UDictionary();
        try {
            HttpResponse<String> response = Unirest.get("https://mashape-community-urban-dictionary.p.mashape.com/define?term=" + word)
                    .header("X-Mashape-Key", "ceU4edWIr7mshi68Xs4IQYUQ7XgTp1ILJUgjsnsO4Qf4MOc543")
                    .header("Accept", "text/plain")
                    .asString();
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getRawBody()));
            Gson gson = new Gson();
            uDictionary = gson.fromJson(br, UDictionary.class);
            br.close();
        } catch (IOException | UnirestException e) {
            e.printStackTrace();
        }
        return uDictionary;
    }
}
