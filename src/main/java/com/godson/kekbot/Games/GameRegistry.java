package com.godson.kekbot.Games;

import com.sun.javaws.exceptions.InvalidArgumentException;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.*;
import java.util.stream.Collectors;

public class GameRegistry {
    private Map<String, Integer> registeredGames = new HashMap<>();
    private Map<String, Class<Game>> blah = new HashMap<>();

    /**
     * Ghetto way of getting game objects.
     */
    public GameRegistry() {
        registerGame(0,"tictactoe", "ttt", "tic-tac-toe", "tic tac toe");
        registerGame(1, "snail race", "sr", "snailrace");
        registerGame(2, "hangman");
    }

    /**
     * Registers a game.
     * @param id The game's ID.
     * @param aliases The name/aliases for the game.
     */
    private void registerGame(int id, String... aliases) {
        for (int i = 0; i < aliases.length; i++) {
            if (registeredGames.containsKey(aliases[i])) {
                throw new IllegalArgumentException("There is already a game registered with this name/alias.");
            }
            registeredGames.put(aliases[i], id);
        }
    }

    /**
     * Gets a game based on its ID.
     * @param name Name/Alias of the game.
     * @param channel The channel where the game will be played.
     * @return The game object that will be played.
     */
    public Game getGame(String name, TextChannel channel) {
        if (hasGame(name)) {
            int gameID = registeredGames.get(name);
            switch (gameID) {
                case 0: return new TicTacToe(channel);
                case 1: return new SnailRace(channel);
                case 2: return new Hangman(channel);
                default: throw new NullPointerException("No game found with this ID. How'd you manage to get this error anyway?");
            }
        } else throw new NullPointerException("No game found with this alias.");
    }

    /**
     * Checks if the registry contains a game.
     * @param name Name/Alias of the game.
     * @return Whether or not if the game exists.
     */
    public boolean hasGame(String name) {
        return registeredGames.containsKey(name);
    }
}
