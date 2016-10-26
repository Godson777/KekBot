package com.godson.kekbot.command.commands.general;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;

public class Credits {
    public static Command credits = new Command("credits")
            .onExecuted(context -> context.getTextChannel().sendMessage(
                    "```md" +
                            "\nCredits:\n\n# Coded By: #\n" + KekBot.client.getUserById("99405418077364224").getUsername() +
                            "\n\n# Memes Supplied By: #\n" + KekBot.client.getUserById("159671787683184640").getUsername() +
                            "\n" + KekBot.client.getUserById("194197898584391680").getUsername() +
                            "\n" + KekBot.client.getUserById("174713102628028416").getUsername() +
                            "\n" + KekBot.client.getUserById("181569245253992448").getUsername() +
                            "\n\n# Special thanks to: #\n" +
                            "Discord4J Team for making D4J in the first place" +
                            "\nPanda for making Command4J" +
                            "\nEveryone in the Discord4J server for helping me with my stupid problems and putting up with me"+ "```"));
}
