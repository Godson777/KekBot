package com.godson.kekbot;

import com.godson.kekbot.settings.Settings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class gsontest {
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void test() {
        System.out.println(gson.toJson(new Settings("testID").setPrefix("$").setAutoRoleID("testID2"), Settings.class));
    }
}
