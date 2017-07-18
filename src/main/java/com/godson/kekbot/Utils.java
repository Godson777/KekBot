package com.godson.kekbot;

import com.godson.kekbot.Profile.BackgroundManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    /**
     * Deletes an entire directory. Enough said.
     * @param directory The directory we're going to delete.
     * @return Whether or not the directory has been deleted successfully.
     */
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

    /**
     * Searches for aa shard that a specific user can be found in. This is meant to be used in cross-shard compatible situations.
     * @param user The user we're searching for.
     * @return The instance of {@link JDA JDA} (or shard) where the user has been found.
     */
    public static JDA getShardUsersShard(User user) {
        JDA jda = null;
        for (JDA shard : KekBot.jdas) {
            if (shard.getUsers().stream().anyMatch(user1 -> user1.equals(user))) {
                jda = shard;
                break;
            }
        }
        if (jda != null) return jda;
        else throw new NullPointerException("Couldn't find this user's shard!");
    }

    /**
     * Attempts to find a {@link net.dv8tion.jda.core.entities.User user} within all shards.
     * @param userId The user's ID we're searching for.
     * @return The now found user object.
     */
    public static User findShardUser(String userId) {
        List<User> users = collectShardUsers();
        if (users.stream().anyMatch(user -> user.getId().equals(userId))) {
            return users.stream().filter(user -> user.getId().equals(userId)).findAny().get();
        } else throw new NullPointerException();
    }

    /**
     * Combines the list of {@link User users} in every shard into a single, merged list.<br>
     * (Note, this WILL cause duplicates to appear if a user appears in more than one shard.
     * The reason for this is due to the fact that attempting to filter through a large list, especially one with over 100k users, causes the bot to hang, the duration of this hang is unknown
     * but during this hang time it <i>does</i> freeze the entire bot (if running on one shard, otherwise it freezes the shard this was ran in.)
     * @return The merged {@link List list} object.
     */
    public static List<User> collectShardUsers() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < KekBot.jdas.length; i++) {
            users.addAll(KekBot.jdas[i].getUsers());
        }
        return users;
    }

    /**
     * Combines the list of {@link Guild guilds} in every shard into a single, merged list.
     * @return The merged {@link List list} object.
     */
    public static List<Guild> collectShardGuilds() {
        List<Guild> guilds = new ArrayList<>();
        for (int i = 0; i < KekBot.jdas.length; i++ ) guilds.addAll(KekBot.jdas[i].getGuilds());
        return guilds;
    }

    /**
     * Purely a method to use with eval.
     */
    public static void reloadBackgrounds() {
        KekBot.backgroundManager = new BackgroundManager();
    }

    /**
     * Attempts to get the user's avatar.
     * @param user The User KekBot will try to steal the avatar from.
     * @return The user's profile picture. Will return a generic avatar if the user doesn't happen (which will only happen if a MalformedURLException occurs).
     */
    public static BufferedImage getAvatar(User user) {
        BufferedImage ava = null;
        try {
            URL userAva = new URL(user.getAvatarUrl());
            URLConnection connection = userAva.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();
            ava = ImageIO.read(connection.getInputStream());
        } catch (MalformedURLException e) {
            ava = KekBot.genericAvatar;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ava == null) ava = KekBot.genericAvatar;
        return ava;
    }
}
