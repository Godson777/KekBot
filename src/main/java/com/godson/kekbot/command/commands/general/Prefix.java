package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class Prefix extends Command {

    public Prefix() {
        name = "prefix";
    }

    @Override
    public void onExecuted(CommandEvent event) {
        event.getChannel().sendMessage("The prefix for `" + event.getGuild().getName() + "` is: `" + event.getPrefix() + "`").queue();
    }
}
