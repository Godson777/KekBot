package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class LMGTFY extends Command {

    public LMGTFY() {
        name = "lmgtfy";
        aliases = new String[]{"lmg"};
        description = "Allows the user to perform a google search for an idiot.";
        usage.add("lmgtfy <query>");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length > 0) {
            event.getChannel().sendMessage("http://lmgtfy.com/?q=" + event.combineArgs().replace(" ", "+")).queue();
        } else event.getChannel().sendMessage("You haven't given me anything to search for!").queue();
    }
}
