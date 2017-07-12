package com.godson.kekbot;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static boolean deleteDirectory(File directory) {
        if (directory.exists()){
            File[] files = directory.listFiles();
            if (null != files){
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return(directory.delete());
    }

    public static JDA getShardUsersShard(User user) {
        JDA jda = null;
        for (JDA shard : KekBot.jdas) {
            if (shard.getUsers().stream().anyMatch(user1 -> user1.equals(user))) jda = shard;
        }
        if (jda != null) return jda;
        else throw new NullPointerException("Couldn't find this user's shard!");
    }

    public static User findShardUser(String userId) {
        List<User> users = collectShardUsers();
        if (users.stream().anyMatch(user -> user.getId().equals(userId))) {
            return users.stream().filter(user -> user.getId().equals(userId)).findAny().get();
        } else throw new NullPointerException();
    }

    public static List<User> collectShardUsers() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < KekBot.jdas.length; i++) {
            users.addAll(KekBot.jdas[i].getUsers());
        }
        return users;
    }

    public static List<Guild> collectShardGuilds() {
        List<Guild> guilds = new ArrayList<>();
        for (int i = 0; i < KekBot.jdas.length; i++ ) guilds.addAll(KekBot.jdas[i].getGuilds());
        return guilds;
    }
}
