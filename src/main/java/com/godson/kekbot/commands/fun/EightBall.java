package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;

import java.util.Random;

public class EightBall {
    public static Command eightBall = new Command("8ball")
            .withCategory(CommandCategory.FUN)
            .withDescription("Ask the magic 8-ball a question!")
            .withUsage("{p}8ball <question>")
            .onExecuted(context -> {
                String[] responses = {"It is certain.", "It is decidedly so.", "Without a doubt.", "Yes, definitely.", "You may rely on it.", "As I see it, yes.", "Most likely.",
                        "Outlook good.", "Yes.", "Signs point to yes.", "Reply hazy, try again.", "Ask again later.", "Better not tell you now.", "Cannot predict now.",
                        "Concentrate and ask again.", "Don't count on it.", "My reply is no.", "My sources say no.", "Outlook not so good.", "Very doubtful."};
                Random random = new Random();
                if (context.getArgs().length > 0) {
                    String question = "";
                    for (int i = 0; i < context.getArgs().length; i++) {
                        question += context.getArgs()[i];
                        if (i != context.getArgs().length -1) question += " ";
                    }
                    context.getTextChannel().sendMessage("\uD83E\uDD14 You asked: __**" + question + "**__\n\n" +
                            "\uD83C\uDFB1 8-Ball's response: __**" + responses[random.nextInt(responses.length)] + "**__").queue();
                } else {
                    context.getTextChannel().sendMessage("\uD83E\uDD14 I ask: __**Did " + context.getAuthor().getName() + " give a question?**__\n\n" +
                            "\uD83C\uDFB1 8-Ball's response: __**No, they didn't.**__").queue();
                }
            });
}
