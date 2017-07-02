package com.godson.kekbot;

import net.dv8tion.jda.core.JDA;
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

    public static User findUser(String userId) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < KekBot.jdas.length; i++) {
            users.addAll(KekBot.jdas[i].getUsers().stream().filter(user -> !users.contains(user)).collect(Collectors.toList()));
        }
        if (users.stream().anyMatch(user -> user.getId().equals(userId))) {
            return users.stream().filter(user -> user.getId().equals(userId)).findAny().get();
        } else throw new NullPointerException();
    }
}
