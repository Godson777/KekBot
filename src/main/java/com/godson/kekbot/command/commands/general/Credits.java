package com.godson.kekbot.command.commands.general;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;

public class Credits {
    public static Command credits = new Command("credits")
            .onExecuted(context -> context.getTextChannel().sendMessageAsync(
                    "```md" +
                            "\nCredits:\n\n# Coded By: #\n" + context.getJDA().getUserById("99405418077364224").getUsername() +
                            "\n\n# Memes Supplied By: #\n" + context.getJDA().getUserById("159671787683184640").getUsername() +
                            "\n" + context.getJDA().getUserById("194197898584391680").getUsername() +
                            "\n" + context.getJDA().getUserById("174713102628028416").getUsername() +
                            "\n" + context.getJDA().getUserById("181569245253992448").getUsername() +
                            "\n\n# Special thanks to: #\n" +
                            "JDA Team for making JDA in the first place" +
                            "\nPanda for making Command4J (Even though I kinda modded it a lot...)" +
                            "\nEveryone in the Discord4J and JDA servers for helping me with my stupid problems and putting up with me"+ "```", null));
}
