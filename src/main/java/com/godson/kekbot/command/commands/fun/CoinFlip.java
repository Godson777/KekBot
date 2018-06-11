package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

import java.util.Random;

public class CoinFlip extends Command {

    private final Random random = new Random();

    public CoinFlip() {
        name = "flip";
        description = "Flips a coin.";
        usage.add("flip");
        category = new Category("Fun");
        cooldownScope = CooldownScope.USER_GUILD;
        cooldown = 5;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        int flip = random.nextInt(2);
        String coin;
        if (flip == 0) {
            coin = "**" + event.getString("command.fun.coinflip.heads") + "!**";
        } else {
            coin = "**" + event.getString("command.fun.coinflip.tails") + "!**";
        }
        event.getChannel().sendMessage(event.getString("command.fun.coinflip.flip", event.getAuthor().getName(), coin)).queue();
    }
}
