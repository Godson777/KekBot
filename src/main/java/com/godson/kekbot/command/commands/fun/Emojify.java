package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class Emojify extends Command {

    private final String[] REPLACEMENT = new String[Character.MAX_VALUE+1];

    public Emojify() {
        name = "emojify";
        aliases = new String[]{"cancerify", "emoji"};
        description = "Converts your plain and boring text message to an exciting message full of emoji.";
        usage.add("emojify <message>");
        category = new Category("Fun");
        cooldownScope = CooldownScope.USER_GUILD;
        cooldown = 10;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length > 0) {
            event.getChannel().sendMessage(Utils.emojify(event.combineArgs().replaceAll("[^\\w\\d\\s!\\?]", ""))).queue();
        } else event.getChannel().sendMessage("No message specified! :cry:").queue();
    }
}
