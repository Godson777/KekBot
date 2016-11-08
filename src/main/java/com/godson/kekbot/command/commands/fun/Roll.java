package com.godson.kekbot.command.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;

import java.util.Random;

public class Roll {
    public static Command roll = new Command("roll")
            .withCategory(CommandCategory.FUN)
            .withDescription("Rolls a dice. Specify a number to give the bot that number sided die.")
            .withUsage("{p}roll {number}")
            .onExecuted(context -> {
                Random random = new Random();
                int defaultDie = random.nextInt(6)+1;
                int specifiedDie = 0;
                String args[] = context.getArgs();
                if (args.length == 0) {
                    context.getTextChannel().sendMessageAsync(Emojify.emojify(String.valueOf(defaultDie)), null);
                } else {
                    try {
                        specifiedDie = random.nextInt(Integer.valueOf(args[0]));
                        context.getTextChannel().sendMessageAsync(Emojify.emojify(String.valueOf(specifiedDie)), null);
                    } catch (NumberFormatException e) {
                        context.getTextChannel().sendMessageAsync("\"" + args[0] + "\" is not a valid number!", null);
                    }
                }
            });

}
