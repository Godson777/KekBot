package com.godson.kekbot;

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
            KekBot.jdas[0].getAccountManager().setGame(games.get(index).replace("{users}", String.valueOf(KekBot.jdas[0].getUsers().size())).replace("{servers}", String.valueOf(KekBot.jdas[0].getGuilds().size())));
            System.out.println("Playing: " + KekBot.jdas[0].getSelfInfo().getCurrentGame().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
