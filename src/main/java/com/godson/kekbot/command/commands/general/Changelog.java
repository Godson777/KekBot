package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class Changelog extends Command {

    public Changelog() {
        name = "changelog";
        description = "Shows you the changelogs for this version of KekBot.";
        usage.add("changelog");
        cooldownScope = CooldownScope.USER_GUILD;
        cooldown = 5;
        category = new Category("General");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        event.getChannel().sendMessage("https://github.com/Godson777/KekBot/releases/tag/v" + KekBot.version).queue();
    }
}
