package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class Shrug extends Command {

    public Shrug() {
        name = "shrug";
        description = "\"¯\\_(ツ)_/¯\"";
        category = new Category("Fun");
        usage.add("shrug");
        cooldownScope = CooldownScope.USER_GUILD;
        cooldown = 5;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        event.getChannel().sendMessage("¯\\_(ツ)_/¯").queue();
    }
}
