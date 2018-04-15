package com.godson.kekbot.command.commands.botowner;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.settings.Config;
import net.dv8tion.jda.core.entities.User;

public class BotAdmin extends Command {

    public BotAdmin() {
        name = "botadmin";
        category = new Category("Bot Owner");
        commandPermission = CommandPermission.OWNER;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        String args[] = event.getArgs();
        Config config = Config.getConfig();
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "add":
                    if (args.length > 1) {
                        User user = KekBot.jda.getUserById(args[1]);
                        if (user != null) {
                            if (config.getBotAdmins().contains(args[1])) {
                                event.getChannel().sendMessage(event.getJDA().getUserById(args[1]).getName() + " is already a bot admin.").queue();
                                return;
                            }

                            config.addBotAdmin(args[1]).save();
                            event.getChannel().sendMessage("Registered " + event.getJDA().getUserById(args[1]).getName() + " as a bot admin.").queue();
                        } else event.getChannel().sendMessage("Not a valid user ID! (Or user not found.)").queue();
                    } else event.getChannel().sendMessage("No user ID specified.").queue();
                    break;
                case "remove":
                    if (args.length > 1) {
                        User user = KekBot.jda.getUserById(args[1]);
                        if (user != null) {
                            if (!config.getBotAdmins().contains(args[1])) {
                                event.getChannel().sendMessage(event.getJDA().getUserById(args[1]).getName() + " is not a bot admin.").queue();
                                return;
                            }

                            config.removeBotAdmin(args[1]).save();
                            event.getChannel().sendMessage("Unregistered " + event.getJDA().getUserById(args[1]).getName() + " as a bot admin.").queue();
                        } else event.getChannel().sendMessage("Not a valid user ID! (Or user not found.)").queue();
                    } else event.getChannel().sendMessage("No user ID specified.").queue();
                    break;
            }
        }
    }
}
