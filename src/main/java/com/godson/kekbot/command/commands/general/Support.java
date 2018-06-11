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

    private String invite = "https://discord.gg/3nbqavE";

    @Override
    public void onExecuted(CommandEvent event) {
        event.getTextChannel().sendMessage(event.getString("command.general.support", invite)).queue();
    }
}
