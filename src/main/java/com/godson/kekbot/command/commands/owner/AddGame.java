package com.godson.kekbot.command.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Settings.Config;
import com.godson.kekbot.XMLUtils;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.exceptions.PermissionException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AddGame {
    public static Command addGame = new Command("addGame")
            .onExecuted(context -> {
                Config config = GSONUtils.getConfig();
                if (config.getAllowedUsers().contains(context.getMessage().getAuthor().getId()) || context.getMessage().getAuthor().getId().equals(config.getBotOwner())) {
                    String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                    TextChannel channel = context.getTextChannel();
                    if (rawSplit.length == 1) {
                        channel.sendMessageAsync("Failed to add game, due to the lack of a game you were supposed to give.", null);
                    } else if (rawSplit.length == 2) {
                        String game = rawSplit[1];
                        try {
                            List<String> games = FileUtils.readLines(new File("games.txt"), "utf-8");
                            if (!games.contains(game)) {
                                    try {
                                        FileUtils.writeStringToFile(new File("games.txt"), "\n" + game, "utf-8", true);
                                        channel.sendMessageAsync("Added __**" + game.replace("{users}", String.valueOf(context.getJDA().getUsers().size())).replace("{servers}" , String.valueOf(context.getJDA().getGuilds().size())) + "**__ to the list of games.", null);
                                    } catch (IOException | PermissionException e) {
                                        e.printStackTrace();
                                    }
                            } else {
                                channel.sendMessageAsync("__**" + game + "**__ is already in my list of games!", null);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
}
