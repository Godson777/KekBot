package com.godson.kekbot.Games;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.Profile.Profile;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Game {
    private String gameName;
    int numberOfPlayers;
    private boolean hasAI;
    private boolean isReady = false;
    public List<User> players = new ArrayList<>();
    private Map<User, Integer> playerNumber = new HashMap<>();
    public TextChannel channel;

    public Game(int numberOfPlayers, boolean hasAI, TextChannel channel, String gameName) {
        this.numberOfPlayers = numberOfPlayers;
        this.hasAI = hasAI;
        this.channel = channel;
        this.gameName = gameName;
    }

    public abstract void startGame();

    public void addPlayer(User player) {
        if (players.size() < numberOfPlayers) {
            players.add(player);
            playerNumber.put(player, players.size());
        }
    }

    public int getPlayerNumber(User player) {
        if (players.contains(player)) {
            return playerNumber.get(player);
        } else {
            return 0;
        }
    }

    public void endGame(User winner) {
        for (User player : players) {
            Profile profile = Profile.getProfile(player);
            if (player.equals(winner)) {
                profile.wonGame();
                profile.save();
            } else {
                profile.lostGame();
                profile.save();
            }
        }
        KekBot.gamesManager.closeGame(channel);
    }

    public void endGame(User winner, int topkeks, int KXP) {
        for (User player : players) {
            Profile profile = Profile.getProfile(player);
            if (player.equals(winner)) {
                profile.wonGame(channel, topkeks, KXP);
                profile.save();
            } else {
                profile.lostGame();
                profile.save();
            }
        }
        KekBot.gamesManager.closeGame(channel);
    }

    public void endTie(int topkeks, int KXP) {
        for (User player : players) {
            Profile profile = Profile.getProfile(player);
            profile.tieGame(channel, topkeks, KXP);
            profile.save();
        }
    }

    public void endGame() {
        KekBot.gamesManager.closeGame(channel);
    }

    public TextChannel getChannel() {
        return channel;
    }

    public boolean isReady() {
        return isReady;
    }

    public void ready() {
        if (players.size() == numberOfPlayers) {
            startGame();
            isReady = true;
        } else {
            if (hasAI) {
                startGame();
                isReady = true;
            }
        }
    }

    public boolean hasRoomForPlayers() {
        return numberOfPlayers > players.size();
    }

    public boolean hasAI() {
        return hasAI;
    }

    public String getGameName() {
        return gameName;
    }
}
