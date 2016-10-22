package com.godson.kekbot.command.commands.general;

import com.darichey.discord.api.Command;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.XMLUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.StringUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AddGame {
    public static Command addGame = new Command("addGame")
            .onExecuted(context -> {
                if (XMLUtils.getAllowedUsers().contains(context.getMessage().getAuthor()) || context.getMessage().getAuthor().getID().equals(XMLUtils.getBotOwner())) {
                    String rawSplit[] = context.getMessage().getContent().split(" ", 2);
                    IChannel channel = context.getMessage().getChannel();
                    if (rawSplit.length == 1) {
                        EasyMessage.send(channel, "Failed to add game, due to the lack of a game you were supposed to give.");
                    } else if (rawSplit.length == 2) {
                        String game = rawSplit[1];
                        try {
                            List<String> games = FileUtils.readLines(new File("games.txt"), "utf-8");
                            if (!games.contains(game)) {
                                RequestBuffer.request(() -> {
                                    try {
                                        FileUtils.writeStringToFile(new File("games.txt"), "\n" + game, "utf-8", true);
                                        new MessageBuilder(KekBot.client).withChannel(channel).withContent("Added __**" + game.replace("{users}", StringUtil.valueOf(KekBot.client.getUsers().size())).replace("{servers}" , StringUtil.valueOf(KekBot.client.getGuilds().size())) + "**__ to the list of games.").send();
                                    } catch (DiscordException | IOException | MissingPermissionsException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } else {
                                EasyMessage.send(channel, "__**" + game + "**__ is already in my list of games!");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
}
