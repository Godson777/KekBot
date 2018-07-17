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
        if (event.getArgs().length > 0) {
            String question = event.combineArgs();

            int response = random.nextInt(19) + 1;
            event.getChannel().sendMessage(event.getString("command.fun.8ball.userquestion", CustomEmote.think(), question, event.getString("command.fun.8ball." + response))).queue();
        } else {
            event.getChannel().sendMessage(event.getString("command.fun.8ball.noargs", CustomEmote.think(), event.getAuthor().getName())).queue();
        }
    }
}
