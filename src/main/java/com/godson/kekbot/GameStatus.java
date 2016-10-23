package com.godson.kekbot;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.StringUtil;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Status;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;

public class GameStatus extends TimerTask {

    public GameStatus() {}

    @Override
    public void run() {
        try {
            if (KekBot.client.isReady()) {
                Random random = new Random();
                List<String> games = FileUtils.readLines(new File("games.txt"), "utf-8");
                int index = random.nextInt(games.size());
                KekBot.client.changeStatus(Status.game(games.get(index).replace("{users}", StringUtil.valueOf(KekBot.client.getUsers().size())).replace("{servers}", StringUtil.valueOf(KekBot.client.getGuilds().size()))));
                System.out.println("Playing: " + KekBot.client.getOurUser().getStatus().getStatusMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
