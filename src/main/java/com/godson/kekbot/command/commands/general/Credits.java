package com.godson.kekbot.command.commands.general;

import com.darichey.discord.api.Command;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.KekBot;

public class Credits {
    public static Command credits = new Command("credits")
            .onExecuted(context -> EasyMessage.send(context.getMessage().getChannel(),
                    "```md" +
                            "\nCredits:\n\n# Coded By: #\n" + KekBot.client.getUserByID("99405418077364224").getName() +
                            "\n\n# Memes Supplied By: #\n" + KekBot.client.getUserByID("159671787683184640").getName() +
                            "\n" + KekBot.client.getUserByID("194197898584391680").getName() +
                            "\n" + KekBot.client.getUserByID("174713102628028416").getName() +
                            "\n" + KekBot.client.getUserByID("181569245253992448").getName() +
                            "\n\n# Special thanks to: #\n" +
                            "Discord4J Team for making D4J in the first place" +
                            "\nPanda for making Command4J" +
                            "\nEveryone in the Discord4J server for helping me with my stupid problems and putting up with me"+ "```"));
}
