package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class Support extends Command {

    public Support() {
        name = "support";
        description = "Shows how you can support KekBot.";
        usage.add("support");
        category = new Category("General");
        cooldown = 10;
        cooldownScope = CooldownScope.USER_GUILD;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        String support = "Want to show your support for KekBot? There are plenty of ways to do so!\n" +
                "You can join KekBot's official server here: https://discord.gg/3nbqavE\n" +
                "There, you can suggest features, memes, and other things that'll help impact KekBot's progress!\n" +
                "And you can also visit KekBot's Patreon: https://www.patreon.com/KekBot";
        event.getTextChannel().sendMessage(support).queue();
    }
}
