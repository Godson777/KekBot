package com.godson.kekbot;

import net.dv8tion.jda.core.entities.Game;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

public class GameStatus extends TimerTask {

    public GameStatus() {}

    @Override
    public void run() {
        try {
            Random random = new Random();
            List<String> games = FileUtils.readLines(new File("games.txt"), "utf-8");
            int index = random.nextInt(games.size());
            KekBot.jdas[0].getPresence().setGame(Game.of(games.get(index).replace("{users}", String.valueOf(Utils.collectShardUsers().size()).replace("{servers}", String.valueOf(Utils.collectShardGuilds().size())))));
            System.out.println("Playing: " + KekBot.jdas[0].getPresence().getGame().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
