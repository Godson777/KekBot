package com.godson.kekbot.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private String token;
    private String dApiToken;
    private String dListBotsToken;
    private String dBotsListToken;
    private String carbonToken;
    private String dcoinToken;
    private String botOwner;
    private int shards;
    private List<String> allowedUsers = new ArrayList<String>();
    private List<String> blockedUsers = new ArrayList<>();
    private List<String> patrons = new ArrayList<>();

    public Config addAllowedUser(String ID) {
        allowedUsers.add(ID);
        return this;
    }

    public Config removeAllowedUser(String ID) {
        allowedUsers.remove(ID);
        return this;
    }

    public Config addBlockedUser(String ID) {
        blockedUsers.add(ID);
        return this;
    }

    public Config removeBlockedUser(String ID) {
        blockedUsers.remove(ID);
        return this;
    }

    public Config addPatron(String patron) {
        patrons.add(patron);
        return this;
    }

    public Config removePatron(String patron) {
        if (patrons.contains(patron)) {
            patrons.remove(patron);
        } else throw new IllegalArgumentException("Patron not found.");
        return this;
    }

    public List<String> getAllowedUsers() {
        return allowedUsers;
    }

    public List<String> getBlockedUsers() {
        return blockedUsers;
    }

    public String getBotOwner() {
        return botOwner;
    }

    public String getToken() {
        return token;
    }

    /**
     * Gets token for the Discord Bots website.
     * @return The token.
     */
    public String getdApiToken() {
        return dApiToken;
    }

    /**
     * Gets token for the DiscordList Bots website.
     * @return The token.
     */
    public String getdListBotsToken() {
        return dListBotsToken;
    }

    /**
     * Gets token for the Discord Bots List website.
     * @return The token.
     */
    public String getdBotsListToken() {
        return dBotsListToken;
    }

    /**
     * Gets token for Carbonitex.
     * @return The token.
     */
    public String getCarbonToken() {
        return carbonToken;
    }

    /**
     * Gets token for Discoin.
     * @return The token.
     */
    public String getDcoinToken() {
        return dcoinToken;
    }

    public List<String> getPatrons() {
        return patrons;
    }

    public int getShards() {
        return shards;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this, this.getClass());
    }

    public void save() {
        File config = new File("config.json");
        try {
            FileWriter writer = new FileWriter(config);
            writer.write(this.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config getConfig() {
        Config config = new Config();
        try {
            BufferedReader br = new BufferedReader(new FileReader("config/config.json"));
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
}
