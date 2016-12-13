package com.godson.kekbot.commands.owner;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import net.dv8tion.jda.core.JDA;


public class Shutdown {
    public static Command shutdown = new Command("shutdown")
            .withCategory(CommandCategory.BOT_OWNER)
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().getId().equals(GSONUtils.getConfig().getBotOwner())) {
                    context.getMessage().getChannel().sendMessage("That's all from me! Hope you had a gay old time!").queue();
                    for (JDA jda : KekBot.jdas) {
                        jda.shutdown();
                    }
                    System.exit(0);
                }
            });
}
