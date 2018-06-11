package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class GoogleSearch extends Command {

    public GoogleSearch() {
        name = "google";
        description = "Sends you a google search with your query.";
        usage.add("google <query>");
        category = new Category("General");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length > 0) {
            event.getChannel().sendMessage("http://google.com/#q=" + event.combineArgs().replace(" ", "+")).queue();
        } else event.getChannel().sendMessage(event.getString("command.general.google.noargs")).queue();
    }
}
