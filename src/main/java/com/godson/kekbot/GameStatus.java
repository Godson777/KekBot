package com.godson.kekbot;

import net.dv8tion.jda.api.entities.Activity;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

public class GameStatus extends TimerTask {

    public GameStatus() {
        setDefaultGames();
    }

    private void setDefaultGames() {
        if (games != null) games.clear();
        try {
            games = FileUtils.readLines(new File("games.txt"), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> games;

    @Override
    public void run() {
        Random random = new Random();
        int index = random.nextInt(games.size());
        KekBot.jda.getShards().forEach(jda -> jda.getPresence().setActivity(Activity.playing(games.get(index))));
        System.out.println("Playing: " + games.get(index));
    }

    public void takeoverGames(List<String> games) {
        this.games = games;
    }

    public void resetGames() {
        setDefaultGames();
    }
}
