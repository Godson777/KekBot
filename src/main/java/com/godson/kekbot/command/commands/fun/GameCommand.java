package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.LocaleUtils;
import com.godson.kekbot.games.Game;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GameCommand extends Command {

    public GameCommand() {
        name = "game";
        description = "Central command for all the game related commands";
        usage.add("game create <gamename> - Creates a lobby for the specified game.");
        usage.add("game join - Joins the lobby created in the channel.");
        usage.add("game ready (or start) - Starts the game.");
        usage.add("game rules - Gives the rules (or instructions) of the game. (This requires you to create a lobby for the game first.)");
        usage.add("game lobby - Gives you info on the current game lobby. (This requires you to create a lobby for the game first.)");
        usage.add("game quit - Quits the game, ending it early. (This will have consequences.)");
        usage.add("game cancel - Cancels/Closes the game lobby before it has started.");
        usage.add("game bet <amount> - Places a bet in favor of you winning. (When in a game lobby.)");
        usage.add("game bet <@player> <amount> - Places a bet in favor or someone else winning. (When watching a game.)");
        extendedDescription = "Keep in mind, you cannot have more than one lobby in a single channel." +
                "\n\nAvailable Games:" +
                "\nTic-Tac-Toe (or \"ttt\" for short.)" +
                "\nSnail Race (or \"sr\" for short.)" +
                "\nHangman";
        exDescPos = ExtendedPosition.AFTER;
        category = new Category("Fun");
    }

    private static String getGameStatus(Game game, String locale) {
        final String ready = LocaleUtils.getString("command.fun.game.readystatus", locale);
        final String morePlayers = LocaleUtils.getString("command.fun.game.awaitingstatus", locale);
        if (game.hasMinimum()) {
            if (game.hasMinimumPlayers()) return ready;
            else return morePlayers;
        } else {
            if (game.hasRoomForPlayers()) {
                if (game.hasAI()) return ready;
                else return morePlayers;
            } else return ready;
        }
    }

    @Override
    public void onExecuted(CommandEvent event) {
        TextChannel channel = event.getTextChannel();
        if (event.getArgs().length >= 1) {
            switch (event.getArgs()[0].toLowerCase()) {
                case "create":
                    if (event.getArgs().length >= 2) {
                        String game = event.combineArgs(1, event.getArgs().length);
                        KekBot.gamesManager.addGame(channel, game.toLowerCase(), event.getAuthor());
                    }
                    break;
                case "start":
                case "ready":
                    if (KekBot.gamesManager.doesUserHaveGame(channel, event.getAuthor())) {
                        Game game = KekBot.gamesManager.getGame(channel);
                        if (!game.players.get(0).equals(event.getAuthor())) {
                            channel.sendMessage(event.getString("command.fun.game.start.error", game.players.get(0).getName())).queue();
                            return;
                        }
                        if (!game.isReady()) game.ready();
                        else channel.sendMessage(event.getString("command.fun.game.start.alreadystarted")).queue();
                    }
                    break;
                case "rules":
                    if (!KekBot.gamesManager.isChannelFree(channel)) {
                        Game game = KekBot.gamesManager.getGame(channel);
                        channel.sendMessage(game.getRules()).queue();
                    } else channel.sendMessage(event.getString("command.fun.game.nolobby")).queue();
                    break;
                case "lobby":
                    if (!KekBot.gamesManager.isChannelFree(channel)) {
                        Game game = KekBot.gamesManager.getGame(channel);
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setTitle("Current Game:")
                                .addBlankField(false)
                                .addField(event.getString("command.fun.game.lobby.currentgame"), game.getGameName(), false);
                        if (game.hasMinimum()) embed.addField(event.getString("command.fun.game.lobby.minplayers"), String.valueOf(game.getMinNumberOfPlayers()), true);
                        embed.addField(event.getString("command.fun.game.lobby.maxplayers"), String.valueOf(game.getMaxNumberOfPlayers()), true)
                                .addField(event.getString("command.fun.game.lobby.numplayers"), String.valueOf(game.players.size()), true)
                                .addField(event.getString("command.fun.game.lobby.status"), getGameStatus(game, event.getLocale()), false)
                                .addField(event.getString("command.fun.game.lobby.players"), StringUtils.join(game.players.stream().map(user -> game.getPlayerNumber(user) + ". " + user.getName()).collect(Collectors.toList()), "\n"), false);

                        channel.sendMessage(embed.build()).queue();
                    }
                    break;
                case "join":
                    if (!KekBot.gamesManager.isChannelFree(channel)) {
                        Game game = KekBot.gamesManager.getGame(channel);
                        if (!game.isReady()) {
                            if (game.players.contains(event.getAuthor())){
                                channel.sendMessage(event.getString("command.fun.game.join.existing")).queue();
                            } else {
                                KekBot.gamesManager.joinGame(channel, event.getAuthor());
                            }
                        } else channel.sendMessage(event.getString("command.fun.game.join.existing")).queue();
                    } else channel.sendMessage(event.getString("command.fun.game.nolobby")).queue();
                    break;
                case "quit":
                    if (!KekBot.gamesManager.isChannelFree(channel)) {
                        Game game = KekBot.gamesManager.getGame(channel);
                        if (game.isReady()) {
                            if (game.players.contains(event.getAuthor())) {
                                Profile profile = Profile.getProfile(event.getAuthor());
                                profile.spendTopKeks(ThreadLocalRandom.current().nextInt(1, 15));
                                profile.takeKXP(ThreadLocalRandom.current().nextInt(5, 20));
                                profile.save();
                                KekBot.gamesManager.killGame(channel);
                                channel.sendMessage(event.getString("command.fun.game.quit.existing", event.getAuthor().getAsMention())).queue();
                            } else channel.sendMessage(event.getString("command.fun.game.quit.existingerror")).queue();
                        } else {
                            if (game.players.contains(event.getAuthor())) {
                                game.removePlayer(event.getAuthor());
                                if (game.players.size() == 0) {
                                    KekBot.gamesManager.closeGame(channel);
                                    channel.sendMessage("**" + event.getString("command.fun.game.quit.cancelled", game.getGameName()) + "**").queue();
                                } else channel.sendMessage("**" + event.getString("command.fun.game.quit.lobby", event.getAuthor().getName(), "(" + game.players.size() + "/" + game.getMaxNumberOfPlayers() + ")") + "**").queue();
                            }
                        }
                    } else channel.sendMessage(event.getString("command.fun.game.nolobby")).queue();
                    break;
                case "cancel":
                    if (!KekBot.gamesManager.isChannelFree(channel)) {
                        Game game = KekBot.gamesManager.getGame(channel);
                        boolean admin = event.getMember().hasPermission(Permission.ADMINISTRATOR);
                        if (game.players.contains(event.getAuthor()) || admin) {
                            if (!game.isReady()) {
                                if (game.players.get(0).equals(event.getAuthor()) || admin) {
                                    if (game.areBetsAllowed()) game.getBets().declareTie();
                                    KekBot.gamesManager.closeGame(channel);
                                    channel.sendMessage("**" + event.getString("command.fun.game.cancel", game.getGameName()) + "**").queue();
                                } else channel.sendMessage(event.getString("command.fun.game.cancel.error", "`Administrator`")).queue();
                            } else channel.sendMessage(event.getString("command.fun.game.cancel.started", "`" + event.getPrefix() + "game quit`")).queue();
                        }
                    } else channel.sendMessage(event.getString("command.fun.game.nolobby")).queue();
                    break;
                case "bet":
                    if (!KekBot.gamesManager.isChannelFree(channel)) {
                        Game game = KekBot.gamesManager.getGame(channel);
                        if (!game.isReady()) {
                            if (game.players.contains(event.getAuthor())) {
                                if (event.getArgs().length >= 2) {
                                    double bet;
                                    try {
                                        bet = Double.valueOf(event.getArgs()[1]);
                                    } catch (NumberFormatException e) {
                                        channel.sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), event.getArgs()[1])).queue();
                                        return;
                                    }
                                    channel.sendMessage(game.getBets().addPlayerBet(event.getAuthor(), bet, event.getLocale())).queue();
                                } else channel.sendMessage(event.getString("command.fun.game.bet.noargs")).queue();
                            } else {
                                if (event.getArgs().length >= 2) {
                                    if (event.getMentionedUsers().size() > 0) {
                                        if (event.getArgs().length >= 3) {
                                            double bet;
                                            try {
                                                bet = Double.valueOf(event.getArgs()[2]);
                                            } catch (NumberFormatException e) {
                                                channel.sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), event.getArgs()[2])).queue();
                                                return;
                                            }
                                            channel.sendMessage(game.getBets().addSpectatorBet(event.getAuthor(), game.getPlayerNumber(event.getMentionedUsers().get(0)), bet, event.getLocale())).queue();
                                        } else channel.sendMessage(event.getString("command.fun.game.bet.noargs")).queue();
                                    } else channel.sendMessage(event.getString("command.fun.game.bet.nomention")).queue();
                                }
                            }
                        } else channel.sendMessage(event.getString("command.fun.game.bet.started")).queue();
                    } else channel.sendMessage(event.getString("command.fun.game.nolobby")).queue();
                    break;
            }
        } else {
            channel.sendMessage(event.getString("command.noargs", "`" + event.getPrefix() + "help game`")).queue();
        }
    }
}
