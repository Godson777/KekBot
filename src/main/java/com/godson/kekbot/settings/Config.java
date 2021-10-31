package com.godson.kekbot.settings;

import com.godson.kekbot.ExitCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    //Bot Token
    private String token;
    //Bot Token (when mode 1 is applied)
    private String betaToken;
    //Rethink stuff
    private String database;
    private String betaDatabase;
    private String dbUser;
    private String dbPassword;
    //Tokens for bot lists
    private String dApiToken;
    private String topGGToken;
    private String carbonToken;
    private String dcoinToken;
    //Bot Owner's User ID
    private String botOwner;
    //Number of Shards to run
    private int shards;
    //Channels for join logs, tickets, and reporting twitter errors
    private String joinLogChannel;
    private String ticketChannel;
    private String twitterChannel;
    //Weeb.sh token
    private String weebToken;
    //IP where the API will be hosted.
    private String APIip;
    //Twitter Stuff
    private String twConsumerKey;
    private String twConsumerSecret;
    private String twAccessToken;
    private String twAccessTokenSecret;
    private boolean twitter = false;

    private final List<String> botAdmins = new ArrayList<>();
    private final List<String> botMods = new ArrayList<>();
    private final Map<String, Integer> blockedUsers = new HashMap<>();
    private final List<String> patrons = new ArrayList<>();

    public String getDatabase() {
        if (database != null) return database;
        else throw new NullPointerException("Database name not listed in config.json.");
    }

    public String getBetaDatabase() {
        if (betaDatabase != null) return betaDatabase;
        else throw new NullPointerException("Database name not listed in config.json.");
    }

    public String getDbUser() {
        if (dbUser != null) return dbUser;
        else throw new NullPointerException("Database user not listed in config.json.");
    }

    public String getAPIip() {
        return APIip;
    }

    public String getDbPassword() {
        if (dbPassword != null) return dbPassword;
        else throw new NullPointerException("Database password not listed in config.json.");
    }

    public Config addBotAdmin(String ID) {
        botAdmins.add(ID);
        return this;
    }

    public Config removeBotAdmin(String ID) {
        botAdmins.remove(ID);
        return this;
    }

    public Config addBotMod(String ID) {
        botMods.add(ID);
        return this;
    }

    public Config removeBotMod(String ID) {
        botMods.remove(ID);
        return this;
    }

    public Config addBlockedUser(String ID, int type) {
        blockedUsers.put(ID, type);
        return this;
    }

    public Config removeBlockedUser(String ID) {
        if (blockedUsers.containsKey(ID)) blockedUsers.remove(ID);
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

    public List<String> getBotAdmins() {
        return botAdmins;
    }

    public List<String> getBotMods() {
        return botMods;
    }

    public Map<String, Integer> getBlockedUsers() {
        return blockedUsers;
    }

    public String getBotOwner() {
        return botOwner;
    }

    public String getToken() {
        return token;
    }

    public String getBetaToken() {
        return betaToken;
    }

    public String getJoinLogChannel() {
        return joinLogChannel;
    }

    public String getTicketChannel() {
        return ticketChannel;
    }

    public String getTwitterChannel() {
        return twitterChannel;
    }

    /**
     * Gets token for the Discord Bots website.
     * @return The token.
     */
    public String getdApiToken() {
        return dApiToken;
    }

    /**
     * Gets token for the Discord Bots List website.
     * @return The token.
     */
    public String getTopGGToken() {
        return topGGToken;
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

    /**
     * Gets token for Weeb.sh
     * @return The token.
     */
    public String getWeebToken() {
        return weebToken;
    }

    public String getTwConsumerKey() {
        return twConsumerKey;
    }

    public String getTwConsumerSecret() {
        return twConsumerSecret;
    }

    public String getTwAccessToken() {
        return twAccessToken;
    }

    public String getTwAccessTokenSecret() {
        return twAccessTokenSecret;
    }

    public boolean usingTwitter() {
        return twitter;
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
        File config = new File("config/config.json");
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
            File configDir = new File("config");
            if (!configDir.exists()) configDir.mkdir();
            BufferedReader br = new BufferedReader(new FileReader("config/config.json"));
            Gson gson = new Gson();
            config = gson.fromJson(br, Config.class);
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("config.json not found! What have you done with it?!");
            // make an empty config object
            Config temp = new Config();
            // fill it with placeholder values
            temp.token = "bot token";
            temp.betaToken = "(optional) beta bot token";
            temp.APIip = "localhost";
            temp.database = "database name";
            temp.betaDatabase = "(optional) database name for beta bot";
            temp.dbUser = "db user name";
            temp.dbPassword = "db password";
            temp.dApiToken = "(optional) discord bot list token";
            temp.topGGToken = "(optional) top.gg bot list token";
            temp.carbonToken = "(optional) carbon bot list token";
            temp.dcoinToken = "(optional) discoin token";
            temp.botOwner = "your discord id";
            temp.shards = 1;
            temp.joinLogChannel = "(optional) id of join log channel";
            temp.ticketChannel = "(optional) channel for tickets";
            temp.twitterChannel = "(optional) channel for tweets";
            temp.weebToken = "(optional) weeb sh token";
            temp.twitter = false;
            temp.twAccessToken = "(optional) twitter access token";
            temp.twConsumerKey = "(optional) twitter consumer key";
            temp.twConsumerSecret = "(optional) twitter consumer secret";
            temp.twAccessTokenSecret = "(optional) twitter access secret token";
            temp.botAdmins.add("(optional) id of bot admin");
            temp.botMods.add("(optional) id of bot mod");
            temp.save();
            System.out.println("Oh well, I just made a blank config file for you!");
            System.exit(ExitCode.SHITTY_CONFIG.getCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }
}
