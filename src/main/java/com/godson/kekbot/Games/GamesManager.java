package com.godson.kekbot.Games;

import com.godson.kekbot.KekBot;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import java.util.*;
import java.util.stream.Collectors;

public class GamesManager extends ListenerAdapter {
    private Map<Long, Game> activeGames = new HashMap<>();
    private Map<Game, BetManager> bets = new HashMap<>();
    private GameRegistry gameRegistry = new GameRegistry();

    public Game getGame(TextChannel channel) {
        return activeGames.get(Long.valueOf(channel.getId()));
    }

    public void joinGame(TextChannel channel, User player) {
        if (activeGames.containsKey(Long.valueOf(channel.getId()))) {
            Game game = activeGames.get(Long.valueOf(channel.getId()));
            if (game.hasRoomForPlayers()) {
                game.addPlayer(player);
                String joinMessage = "**" + player.getName() + " joined the game. (" + game.players.size() + "/" + game.getMaxNumberOfPlayers() + ")**";
                if (game.hasMinimum() && game.hasMinimumPlayers() && !game.reachedMinimumPlayers()) {
                    joinMessage += " *(Minimum Players Reached! You can now start the game with `" + KekBot.insertPrefix(channel.getGuild()) + "game ready`!)*";
                }
                channel.sendMessage(joinMessage).queue();
                if (!game.hasRoomForPlayers()) channel.sendMessage("The lobby is now full! " + game.players.get(0).getAsMention() + "! Start the game with `" + KekBot.insertPrefix(channel.getGuild()) + "game ready`!").queue();
            } else channel.sendMessage("This `" + game.getGameName() + "` lobby is already full.").queue();
        }
    }

    public void addGame(TextChannel channel, String gameName, User host) {
        if (isChannelFree(channel)) {
            if (!gameRegistry.hasGame(gameName)) {
                return;
            }
            Game game = gameRegistry.getGame(gameName, channel);
            game.addPlayer(host);
            activeGames.put(Long.valueOf(channel.getId()), game);
            channel.sendMessage(game.getGameName() + " lobby created!" +
                    (game.hasMinimum() ? " ***(Minimum " + game.getMinNumberOfPlayers() + " players to play. Maximum " + game.getMaxNumberOfPlayers() + " players.)***" : "") +
                    (game.hasRoomForPlayers() ? KekBot.replacePrefix(channel.getGuild(), " Players can join by using `{p}game join`.") : "") +
                    (game.hasRoomForPlayers() && game.hasAI() ? KekBot.replacePrefix(channel.getGuild(), " Or, you can start the game early with `{p}game ready`, and play with an AI.") : "") +
                    (game.hasAI() && !game.hasRoomForPlayers() ? KekBot.replacePrefix(channel.getGuild(), " You can now start the game with `{p}game ready`") : "")).queue();
        } else {
            Game game = getGame(channel);
            User otherHost = game.players.get(0);
            if (otherHost == host) {
                channel.sendMessage("You're already hosting a game of " + game.getGameName() + " in this channel!").queue();
                return;
            }
            if (game.isReady()) channel.sendMessage("There's already a game of `" + game.getGameName() + "` being played here. The game was started by " + otherHost.getName() + ".").queue();
            else channel.sendMessage("There's already a lobby for `" + game.getGameName() + "` in this channel, the lobby was created by " + otherHost.getName() + ".").queue();
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
                    game.acceptInputFromMessage(event.getMessage());
                }
            }
        }
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        if (activeGames.containsKey(Long.valueOf(event.getChannel().getId()))) closeGame(event.getChannel());
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        if (event.getGuild().getTextChannels().stream().anyMatch(channel -> activeGames.containsKey(Long.valueOf(channel.getId())))) {
            List<TextChannel> gameChannels = event.getGuild().getTextChannels().stream().filter(channel -> activeGames.containsKey(Long.valueOf(channel.getId()))).collect(Collectors.toList());
            for (TextChannel channel : gameChannels) {
                if (activeGames.get(Long.valueOf(channel.getId())).players.contains(event.getUser())) {
                    activeGames.get(Long.valueOf(channel.getId())).channel.sendMessage("This game has ended abruptly due to a player (" + event.getUser().getAsMention() + ") having left or having been removed from this server.").queue();
                    closeGame(channel);
                }
            }
        }
    }
}
