package com.godson.kekbot.command.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;

import java.util.Random;

public class CoinFlip {
    public static Command coinFlip = new Command("coinflip")
            .withAliases("flip")
            .withCategory(CommandCategory.FUN)
            .withDescription("Flips a coin.")
            .withUsage("{p}flip")
            .onExecuted(context -> {
                Random random = new Random();
                int flip = random.nextInt(2);
                String coin;
                if (flip == 0) {
                    coin = "**HEADS!**";
                } else {
                    coin = "**TAILS!**";
                }
                context.getTextChannel().sendMessageAsync(context.getMessage().getAuthor().getAsMention() + " Flipped the coin and it landed on... " + coin, null);
            });
}
