package com.godson.kekbot.games;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Utils;
import com.godson.kekbot.profile.Profile;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.*;
import java.util.concurrent.*;

public class SnailRace extends Game {
    private Message race;
    private int[] snails;
    private int[] movements = {-1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3};
    private int[] move;
    private Timer timer = new Timer();
    private Random random = new Random();
    private long startTime;
    private boolean finished = false;
    private List<User> winners = new ArrayList<>();
    private final String snail = "\uD83D\uDC0C";
    private final String flag = "\uD83C\uDFF4";

    public SnailRace(TextChannel channel) {
        super(2,6, false, channel, "Snail Race", true);
    }

    @Override
    public void startGame() {
        startTime = System.currentTimeMillis();
        snails = new int[players.size()];
        move = new int[snails.length];
        prepareMessage();
    }

    @Override
    public String getRules() {
        return "It's a race to the finish! As soon as the game starts, everyone will have a snail trying to race for the finish line." +
                "Players don't have to do anything to win, snails move automatically, making this a game of chance!" +
                "\n\nFirst snail to reach the finish wins!" +
                "\nDepending on how many players are playing, there can also be second and third place winners!";
    }

    private void prepareMessage() {
        channel.sendMessage(drawRace()).queue(message -> race = message);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                race();
            }
        }, TimeUnit.SECONDS.toMillis(2), TimeUnit.SECONDS.toMillis(2));
    }

    private String positionSnail(int pos, int placement) {
        String line = "------------------------------------------------------------";
        return line.substring(0, pos) + snail + line.substring(pos, line.length()) + (placement == 0 ? flag : CustomEmote.getTrophy(placement - 1));
    }

    private int getPlacement(User player) {
        if (winners.size() > 0) {
            for (int i = 0; i < winners.size(); i++) {
                if (winners.get(i).equals(player)) return i + 1;
            }
            return 0;
        } else return 0;
    }

    private String drawRace() {
        String space = "               ";
        String currentTime = Utils.convertMillisToHMmSs(System.currentTimeMillis() - startTime);
        StringBuilder builder = new StringBuilder();
        if (!finished) builder.append("***The race has begun!***");
        else builder.append("***The race is now over!***");
        builder.append(" (Time Spent: ").append(currentTime).append(")").append("\n\n");
        for (int i = 0; i < players.size(); i++) {
            User player = players.get(i);
            builder.append("`").append(
                    (player.getName().length() > 15 ?
                    player.getName().substring(0, 11) + "..." :
                    (player.getName().length() < 15 ? player.getName() + space.substring(0, 14-player.getName().length()) :
                            player.getName())))
                    .append(":` ").append(positionSnail(snails[i], getPlacement(player)));
            if (!finished) {
                if (move[i] == -1) builder.append("***-1***");
                else if (move[i] == 0) builder.append("***0***");
                else builder.append("***+").append(move[i]).append("***");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private void race() {
        for (int i = 0; i < snails.length; i++) {
            if (snails[i] != 60) {
                int movement = movements[random.nextInt(movements.length)];
                if (movement == -1 && snails[i] != 0) {
                    snails[i] += movement;
                    move[i] = movement;
                } else if (movement >= 0) {
                    if (snails[i] + movement > 60) {
                        move[i] = 0;
                    } else {
                        snails[i] += movement;
                        move[i] = movement;
                    }
                } else move[i] = 0;
            } else move[i] = 0;
        }
        check();
    }

    private void check() {
        int finished = 0;
        for (int i = 0; i < snails.length; i++) {
            if (snails[i] == 60) {
                if (!winners.contains(players.get(i))) {
                    winners.add(players.get(i));
                    addWinner(players.get(i));
                }
                finished++;
            }
        }
        race.editMessage(drawRace()).queue(message -> race = message);
        if (players.size() == 2 && finished == 1) endGame(false);
        if (players.size() == 3 && finished == 2) endGame(false);
        if (players.size() > 3 && finished == 3) endGame(false);
        if (finished == players.size()) endGame(true);
    }

    @Override
    public void endTie() {
        timer.cancel();
        finished = true;
        race.editMessage(drawRace()).queue();
        super.endTie();
    }

    private void endGame(boolean tie) {
        if (!tie) endGame(winners, ThreadLocalRandom.current().nextInt(1, 3), ThreadLocalRandom.current().nextInt(2, 4));
        else endTie(ThreadLocalRandom.current().nextInt(4, 7), ThreadLocalRandom.current().nextInt(5, 8));
        timer.cancel();
        finished = true;
        race.editMessage(drawRace()).queue();
    }
}
