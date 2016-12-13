package com.godson.kekbot.Settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private String token;
    private String dApiToken;
    private String botOwner;
    private List<String> allowedUsers = new ArrayList<String>();

    public Config addAllowedUser(String ID) {
        allowedUsers.add(ID);
        return this;
    }

    public Config removeAllowedUser(String ID) {
        allowedUsers.remove(ID);
        return this;
    }

    public List<String> getAllowedUsers() {
        return allowedUsers;
    }

    public String getBotOwner() {
        return botOwner;
    }

    public String getToken() {
        return token;
    }

    public String getdApiToken() {
        return dApiToken;
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
}
