package com.godson.kekbot.Games;

import com.godson.kekbot.KekBot;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import java.util.*;

public class GamesManager extends ListenerAdapter {
    private Map<Long, Game> activeGames = new HashMap<>();

    public Game getGame(TextChannel channel) {
        return activeGames.get(Long.valueOf(channel.getId()));
    }

    public void joinGame(TextChannel channel, User player) {
        if (activeGames.containsKey(Long.valueOf(channel.getId()))) {
            Game game = activeGames.get(Long.valueOf(channel.getId()));
            if (game.hasRoomForPlayers()) {
                game.addPlayer(player);
                channel.sendMessage("**" + player.getName() + " joined the game. (" + game.players.size() + "/" + game.numberOfPlayers + ")**").queue();
                if (!game.hasRoomForPlayers()) channel.sendMessage("The lobby is now full! " + game.players.get(0).getAsMention() + KekBot.replacePrefix(channel.getGuild(), "! Start the game with `{p}game ready`!")).queue();
            } else channel.sendMessage("This `" + game.gameName + "` lobby is already full.").queue();
        }
    }

    public void addGame(TextChannel channel, Game game, User host) {
        if (!activeGames.containsKey(Long.valueOf(channel.getId()))) {
            game.addPlayer(host);
            activeGames.put(Long.valueOf(channel.getId()), game);
            channel.sendMessage(game.gameName + " lobby created!" +
                    (game.hasRoomForPlayers() ? KekBot.replacePrefix(channel.getGuild(), " Players can join by using `{p}game join`.") : "") +
                    (game.hasRoomForPlayers() && game.hasAI() ? KekBot.replacePrefix(channel.getGuild(), " Or, you can start the game early with `{p}game ready`, and play with an AI.") : "") +
                    (game.hasAI() && !game.hasRoomForPlayers() ? KekBot.replacePrefix(channel.getGuild(), " You can now start the game with `{p}game ready`") : "")).queue();
        }
    }

    public void closeGame(TextChannel channel) {
        if (activeGames.containsKey(Long.valueOf(channel.getId()))) {
                activeGames.remove(Long.valueOf(channel.getId()));
        }
    }

    public boolean doesUserHaveGame(TextChannel channel, User user) {
        return activeGames.containsKey(Long.valueOf(channel.getId())) && activeGames.get(Long.valueOf(channel.getId())).players.contains(user);
    }

    public boolean isChannelFree(TextChannel channel) {
        return !activeGames.containsKey(Long.valueOf(channel.getId()));
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (activeGames.containsKey(Long.valueOf(event.getChannel().getId()))) {
            Game game = activeGames.get(Long.valueOf(event.getChannel().getId()));
            if (game.players.contains(event.getAuthor())) {
                if (game.isReady()) {
                    String contents = event.getMessage().getRawContent();
                    switch (game.gameName) {
                        case "TicTacToe":
                            TicTacToe ticTacToe = (TicTacToe) game;
                            try {
                                int slot = Integer.valueOf(contents);
                                ticTacToe.fillSlot(slot-1, event.getAuthor());
                            } catch (NumberFormatException e) {
                                //do nothing.
                            }
                    }
                }
            }
        }
    }
}
