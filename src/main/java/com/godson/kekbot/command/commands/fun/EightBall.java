package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

import java.util.Random;

public class EightBall extends Command {

    private final Random random = new Random();

    public EightBall() {
        name = "8ball";
        description = "Ask the magic 8-ball a question!";
        usage.add("8ball <question>");
        category = new Category("Fun");
        cooldownScope = CooldownScope.USER_GUILD;
        cooldown = 10;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        String[] responses = {"It is certain.", "It is decidedly so.", "Without a doubt.", "Yes, definitely.", "You may rely on it.", "As I see it, yes.", "Most likely.",
                "Outlook good.", "Yes.", "Signs point to yes.", "Reply hazy, try again.", "Ask again later.", "Better not tell you now.", "Cannot predict now.",
                "Concentrate and ask again.", "Don't count on it.", "My reply is no.", "My sources say no.", "Outlook not so good.", "Very doubtful."};
        Random random = new Random();
        if (event.getArgs().length > 0) {
            String question = event.combineArgs();

            event.getChannel().sendMessage(CustomEmote.think() + " You asked: __**" + question + "**__\n\n" +
                    "\uD83C\uDFB1 8-Ball's response: __**" + responses[random.nextInt(responses.length)] + "**__").queue();
        } else {
            event.getChannel().sendMessage(CustomEmote.think() + " I ask: __**Did " + event.getAuthor().getName() + " give a question?**__\n\n" +
                    "\uD83C\uDFB1 8-Ball's response: __**No, they didn't.**__").queue();
        }
    }
}
