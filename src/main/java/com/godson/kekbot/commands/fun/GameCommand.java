package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.Games.Game;
import com.godson.kekbot.Games.SnailRace;
import com.godson.kekbot.Games.TicTacToe;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Profile.Profile;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Settings.Config;
import com.godson.kekbot.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GameCommand {
    public static Command game = new Command("game")
            .withCategory(CommandCategory.FUN)
            .withDescription("Central command for all the game related commands.")
            .withUsage("{p}game create <gamename> - Creates a lobby for the specified game." +
                    "\n{p}game join - Joins the lobby created in the channel." +
                    "\n{p}game ready (or start) - Starts the game." +
                    "\n{p}game rules - Gives the rules (or instructions) of the game. (This requires you to create a lobby for the game first.)" +
                    "\n{p}game lobby - Gives you info on the current game lobby. (This requires you to create a lobby for the game first.)" +
                    "\n{p}game quit - Quits the game, ending it early. (This will have consequences.)" +
                    "\n{p}game cancel - Cancels/Closes the game lobby before it has started." +
                    "\n{p}game bet <amount> - Places a bet in favor of you winning. (When in a game lobby.)" +
                    "\n{p}game bet <@player> <amount> - Places a bet in favor or someone else winning. (When watching a game.)" +
                    "\n\nKeep in mind, you cannot have more than one lobby in a single channel." +
                    "\n\nAvailable Games:" +
                    "\nTic-Tac-Toe (or \"ttt\" for short.)" +
                    "\nSnail Race (or \"sr\" for short.)" +
                    "\nHangman")
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                if (context.getArgs().length >= 1) {
                    switch (context.getArgs()[0].toLowerCase()) {
                        case "create":
                            if (context.getArgs().length >= 2) {
                                String game = Utils.combineArguments(Arrays.copyOfRange(context.getArgs(), 1, context.getArgs().length));
                                KekBot.gamesManager.addGame(channel, game.toLowerCase(), context.getAuthor());
                            }
                            break;
                        case "start":
                        case "ready":
                            if (KekBot.gamesManager.doesUserHaveGame(channel, context.getAuthor())) {
                                Game game = KekBot.gamesManager.getGame(channel);
                                if (!game.players.get(0).equals(context.getAuthor())) {
                                    channel.sendMessage("Only Player 1 (" + game.players.get(0).getName() + ") can run this command.").queue();
                                    return;
                                }
                                if (!game.isReady()) game.ready();
                                else channel.sendMessage("The game has already started!").queue();
                            }
                            break;
                        case "rules":
                            if (!KekBot.gamesManager.isChannelFree(channel)) {
                                Game game = KekBot.gamesManager.getGame(channel);
                                channel.sendMessage(game.getRules()).queue();
                            } else channel.sendMessage("There doesn't seem to be a game lobby in here...").queue();
                            break;
                        case "lobby":
                            if (!KekBot.gamesManager.isChannelFree(channel)) {
                                Game game = KekBot.gamesManager.getGame(channel);
                                EmbedBuilder embed = new EmbedBuilder();
                                embed.setTitle("Current Game:")
                                        .addBlankField(false)
                                        .addField("Game:", game.getGameName(), false);
                                if (game.hasMinimum()) embed.addField("Minimum Players Needed:", String.valueOf(game.getMinNumberOfPlayers()), true);
                                embed.addField("Maximum Players Allowed:", String.valueOf(game.getMaxNumberOfPlayers()), true)
                                        .addField("Number of Players:", String.valueOf(game.players.size()), true)
                                        .addField("Status:", getGameStatus(game), false)
                                        .addField("Players:", StringUtils.join(game.players.stream().map(user -> game.getPlayerNumber(user) + ". " + user.getName()).collect(Collectors.toList()), "\n"), false);

                                channel.sendMessage(embed.build()).queue();
                            }
                            break;
                        case "join":
                            if (!KekBot.gamesManager.isChannelFree(channel)) {
                                Game game = KekBot.gamesManager.getGame(channel);
                                if (!game.isReady()) {
                                    if (game.players.contains(context.getAuthor())){
                                        channel.sendMessage("You're already in this game!").queue();
                                    } else {
                                        KekBot.gamesManager.joinGame(channel, context.getAuthor());
                                    }
                                } else channel.sendMessage("This game's already started, you can't join it now!").queue();
                            } else channel.sendMessage("There doesn't seem to be a game lobby in here...").queue();
                            break;
                        case "quit":
                            if (!KekBot.gamesManager.isChannelFree(channel)) {
                                Game game = KekBot.gamesManager.getGame(channel);
                                if (game.isReady()) {
                                    if (game.players.contains(context.getAuthor())) {
                                        Profile profile = Profile.getProfile(context.getAuthor());
                                        profile.spendTopKeks(ThreadLocalRandom.current().nextInt(1, 15));
                                        profile.takeKXP(ThreadLocalRandom.current().nextInt(5, 20));
                                        profile.save();
                                        KekBot.gamesManager.closeGame(channel);
                                        channel.sendMessage("This game has ended abruptly due to a player (" + context.getAuthor().getAsMention() + ") having quit the game.").queue();
                                    } else channel.sendMessage("You're not even in this game. Are you sure you're not trying to quit something else?").queue();
                                } else {
                                    if (game.players.contains(context.getAuthor())) {
                                        game.removePlayer(context.getAuthor());
                                        if (game.players.size() == 0) {
                                            KekBot.gamesManager.closeGame(channel);
                                            channel.sendMessage("**This game of " + game.getGameName() + " has been cancelled due to all players having left the lobby.**").queue();
                                        } else channel.sendMessage("**" + context.getAuthor().getName() + " left the game. (" + game.players.size() + "/" + game.getMaxNumberOfPlayers() + ")**").queue();
                                    }
                                }
                            } else channel.sendMessage("There doesn't seem to be a game lobby in here...").queue();
                            break;
                        case "cancel":
                            if (!KekBot.gamesManager.isChannelFree(channel)) {
                                Game game = KekBot.gamesManager.getGame(channel);
                                boolean admin = context.getMember().hasPermission(Permission.ADMINISTRATOR);
                                if (game.players.contains(context.getAuthor()) || admin) {
                                    if (!game.isReady()) {
                                        if (game.players.get(0).equals(context.getAuthor()) || admin) {
                                            if (game.areBetsAllowed()) game.getBets().declareTie();
                                            KekBot.gamesManager.closeGame(channel);
                                            channel.sendMessage("**This game of " + game.getGameName() + " has been cancelled.**").queue();
                                        } else channel.sendMessage("Only player 1 or someone with the `Administrator` permission can cancel a game.").queue();
                                    } else channel.sendMessage("This game has already started. If you want to quit, you can use `" + KekBot.insertPrefix(channel.getGuild()) + "game quit`, however, there will be consequences.").queue();
                                }
                            } else channel.sendMessage("There doesn't seem to be a game lobby in here...").queue();
                            break;
                        case "bet":
                            if (!KekBot.gamesManager.isChannelFree(channel)) {
                                Game game = KekBot.gamesManager.getGame(channel);
                                if (!game.isReady()) {
                                    if (game.players.contains(context.getAuthor())) {
                                        if (context.getArgs().length >= 2) {
                                            double bet;
                                            try {
                                                bet = Double.valueOf(context.getArgs()[1]);
                                            } catch (NumberFormatException e) {
                                                channel.sendMessage(KekBot.respond(Action.NOT_A_NUMBER, context.getArgs()[1])).queue();
                                                return;
                                            }
                                            channel.sendMessage(game.getBets().addPlayerBet(context.getAuthor(), bet)).queue();
                                        } else channel.sendMessage("You haven't specified how much you want to bet!").queue();
                                    } else {
                                        if (context.getArgs().length >= 2) {
                                            if (context.getMessage().getMentionedUsers().size() > 0) {
                                                if (context.getArgs().length >= 3) {
                                                    double bet;
                                                    try {
                                                        bet = Double.valueOf(context.getArgs()[2]);
                                                    } catch (NumberFormatException e) {
                                                        channel.sendMessage(KekBot.respond(Action.NOT_A_NUMBER, context.getArgs()[2])).queue();
                                                        return;
                                                    }
                                                    channel.sendMessage(game.getBets().addSpectatorBet(context.getAuthor(), game.getPlayerNumber(context.getMessage().getMentionedUsers().get(0)), bet)).queue();
                                                } else channel.sendMessage("You haven't specified how much you want to bet!").queue();
                                            } else channel.sendMessage("The user you want to bet on must be in the form of a mention!").queue();
                                        }
                                    }
                                } else channel.sendMessage("This game's already started, you can't bet in it now!").queue();
                            } else channel.sendMessage("There doesn't seem to be a game lobby in here...").queue();
                            break;
                    }
                } else {
                    channel.sendMessage(KekBot.replacePrefix(context.getGuild(), "No args specified. Use `{p}help game` for help.")).queue();
                }
            });

    private static String getGameStatus(Game game) {
        final String ready = "Ready to Play!";
        final String morePlayers = "Awaiting more players...";
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
}
