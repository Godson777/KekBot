package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class Lenny extends Command {

    public Lenny() {
        name = "lenny";
        description = "\"( ͡° ͜ʖ ͡°)\"";
        usage.add("lenny");
        category = new Category("Fun");
        cooldownScope = CooldownScope.USER_GUILD;
        cooldown = 15;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        event.getChannel().sendMessage("( ͡° ͜ʖ ͡°)").queue();
    }
}
