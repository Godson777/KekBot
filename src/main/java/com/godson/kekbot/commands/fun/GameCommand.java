package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.Games.Game;
import com.godson.kekbot.Games.TicTacToe;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Settings.Config;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

public class GameCommand {
    public static Command game = new Command("game")
            .withCategory(CommandCategory.FUN)
            .withDescription("Central command for all the game related commands.")
            .withUsage("{p}game createlobby <gamename> - Creates a lobby for the specified game." +
                    "\n{p}game join - Joins the lobby created in the channel." +
                    "\n{p}game ready (or start) - Starts the game." +
                    "\n\nAvailable Games:" +
                    "\nTic-Tac-Toe (or \"ttt\" for short.)")
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                if (context.getArgs().length >= 1) {
                    switch (context.getArgs()[0].toLowerCase()) {
                        case "createlobby":
                            if (!KekBot.gamesManager.doesUserHaveGame(channel, context.getAuthor())) {
                                if (KekBot.gamesManager.isChannelFree(channel)) {
                                    if (context.getArgs().length >= 2) {
                                        switch (context.getArgs()[1].toLowerCase()) {
                                            case "tictactoe":
                                            case "ttt":
                                            case "tic-tac-toe":
                                                KekBot.gamesManager.addGame(channel, new TicTacToe(channel), context.getAuthor());
                                        }
                                    }
                                } else {
                                    channel.sendMessage("There is already a game being played on this channel.").queue();
                                }
                            } /*else {
                            if (KekBot.gamesManager.getGame(guild, context.getAuthor()).getChannel().equals(channel)) {
                                if (KekBot.gamesManager.getGame(channel, context.getAuthor()) instanceof TicTacToe) {
                                    game = (TicTacToe) KekBot.gamesManager.getGame(channel, context.getAuthor());
                                } else {
                                    channel.sendMessage("You're playing a different game already.").queue();
                                    return;
                                }
                            }*/
                            break;
                        case "start":
                        case "ready":
                            if (KekBot.gamesManager.doesUserHaveGame(channel, context.getAuthor())) {
                                Game game = KekBot.gamesManager.getGame(channel);
                                if (!game.isReady()) game.ready();
                                else channel.sendMessage("The game has already started!").queue();
                            }
                            break;
                        case "join":
                            if (!KekBot.gamesManager.isChannelFree(channel)) {
                                if (KekBot.gamesManager.getGame(channel).players.contains(context.getAuthor())) {
                                    channel.sendMessage("You're already in this game!").queue();
                                } else {
                                    KekBot.gamesManager.joinGame(channel, context.getAuthor());
                                }
                            }
                    }
                } else {
                    channel.sendMessage(KekBot.replacePrefix(context.getGuild(), "No args specified. Use `{p}help game` for help.")).queue();
                }
            });
}
