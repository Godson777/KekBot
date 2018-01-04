package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

import java.util.Random;

public class Roll extends Command {

    private final Random random = new Random();

    public Roll() {
        name = "roll";
        aliases = new String[]{"dice"};
        description = "Rolls a die. Specify a number to give the bot that number sided die.";
        category = new Category("Fun");
        usage.add("roll");
        usage.add("roll {number}");
        cooldownScope = CooldownScope.USER_GUILD;
        cooldown = 10;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        int defaultDie = random.nextInt(6)+1;
        int specifiedDie;
        if (event.getArgs().length == 0) {
            event.getChannel().sendMessage(Utils.emojify(String.valueOf(defaultDie))).queue();
        } else {
            try {
                specifiedDie = random.nextInt(Integer.valueOf(event.getArgs()[0]));
                event.getChannel().sendMessage(Utils.emojify(String.valueOf(specifiedDie))).queue();
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage(KekBot.respond(event, Action.NOT_A_NUMBER, "`" + event.getArgs()[0] + "`")).queue();
            }
        }
    }
}
