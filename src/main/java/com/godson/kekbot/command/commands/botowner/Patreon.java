package com.godson.kekbot.command.commands.botowner;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.settings.Config;

public class Patreon extends Command {

    public Patreon() {
        name = "patreon";
        category = new Category("Bot Owner");
        commandPermission = CommandPermission.OWNER;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length > 0) {
            switch (event.getArgs()[0]) {
                case "add":
                    if (event.getArgs().length > 1) {
                        Config.getConfig().addPatron(event.combineArgs(1)).save();
                        event.getTextChannel().sendMessage("Successfully added patron.").queue();
                    } else event.getMessage().getChannel().sendMessage("No name specified.").queue();
                    break;
                case "remove":
                    if (event.getArgs().length > 1) {
                        try {
                            Config.getConfig().removePatron(event.combineArgs(1)).save();
                            event.getTextChannel().sendMessage("Successfully removed patron.").queue();
                        } catch (IllegalArgumentException e) {
                            event.getTextChannel().sendMessage("Patron not found.").queue();
                        }
                    } else event.getMessage().getChannel().sendMessage("No name specified.").queue();
            }
        } else event.getChannel().sendMessage("No arguments specified.").queue();
    }
}
