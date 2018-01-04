package com.godson.kekbot.command.commands.botowner;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.settings.Config;

public class Patreon extends Command {

    public Patreon() {
        name = "patreon";
        category = CommandCategories.botOwner;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length > 1) {
            switch (event.getArgs()[0]) {
                case "add":
                    if (event.getArgs().length > 2) {
                        Config.getConfig().addPatron(event.combineArgs(1)).save();
                        event.getTextChannel().sendMessage("Successfully added patron.").queue();
                    } else event.getEvent().getMessage().getChannel().sendMessage("No name specified.").queue();
                    break;
                case "remove":
                    if (event.getArgs().length > 2) {
                        try {
                            Config.getConfig().removePatron(event.combineArgs(1)).save();
                            event.getTextChannel().sendMessage("Successfully removed patron.").queue();
                        } catch (IllegalArgumentException e) {
                            event.getTextChannel().sendMessage("Patron not found.").queue();
                        }
                    } else event.getEvent().getMessage().getChannel().sendMessage("No name specified.").queue();
            }
        } else event.getChannel().sendMessage("No arguments specified.").queue();
    }
}
