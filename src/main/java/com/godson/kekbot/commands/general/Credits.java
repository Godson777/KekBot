package com.godson.kekbot.commands.general;

import com.darichey.discord.api.Command;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.Utils;
import org.apache.commons.lang3.StringUtils;

public class Credits {
    public static Command credits = new Command("credits")
            .onExecuted(context -> {
                context.getTextChannel().sendMessage(
                        "```md" +
                                "\nCredits:\n\n# Coded By: #\n" + Utils.findShardUser("99405418077364224").getName() +
                                "\n\n# Memes Supplied By: #\n" + Utils.findShardUser("159671787683184640").getName() +
                                "\n" + Utils.findShardUser("194197898584391680").getName() +
                                "\n" + Utils.findShardUser("174713102628028416").getName() +
                                "\n" + Utils.findShardUser("181569245253992448").getName() +
                                "\n\n# Special thanks to: #" +
                                "\n" + Utils.findShardUser("240128376105336832").getName() + " for making the TopKek drawing, and a few other assets for profiles." +
                                "\n" + Utils.findShardUser("255472639261409281").getName() + " for making some assets for shops." +
                                "\nJDA Team for making JDA in the first place" +
                                "\nPanda for making Command4J (Even though I kinda modded it a lot...)" +
                                "\nEveryone in the Discord4J and JDA servers for helping me with my stupid problems and putting up with me"+ "```").queue();
                if (GSONUtils.getConfig().getPatrons().size() > 0)
                    context.getTextChannel().sendMessage("```md" +
                        "\n# List of Patreons: #\n" + StringUtils.join(GSONUtils.getConfig().getPatrons(), "\n") + "\nYou all rock!```").queue();
            });
}
