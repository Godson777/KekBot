package com.godson.kekbot;


import sx.blah.discord.Discord4J;

import java.io.IOException;
import java.io.InputStream;

public class Properties {

    public static String getDiscordVersion() {
        java.util.Properties properties = new java.util.Properties();
        InputStream stream = Discord4J.class.getClassLoader().getResourceAsStream("app.properties");
        try {
            properties.load(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty("application.version");
    }

    public static String getBotVersion() {
        return null;
    }
}
