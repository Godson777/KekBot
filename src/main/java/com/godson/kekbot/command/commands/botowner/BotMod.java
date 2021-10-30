package com.godson.kekbot.command.commands.botowner;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.settings.Config;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.entities.User;

public class BotMod extends Command {

    public BotMod() {
        name = "botmod";
        category = new Category("Bot Owner");
        commandPermission = CommandPermission.OWNER;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        String[] args = event.getArgs();
        Config config = Config.getConfig();
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "add":
                    if (args.length > 1) {
                        User user = KekBot.jda.getUserById(args[1]);
                        if (user != null) {
                            if (config.getBotMods().contains(args[1])) {
                                event.getChannel().sendMessage(user.getName() + " is already a bot mod.").queue();
                                return;
                            }

                            config.addBotMod(args[1]).save();
                            event.getClient().addBotMod(args[1]);
                            event.getChannel().sendMessage("Registered " + user.getName() + " as a bot mod.").queue();
                        } else event.getChannel().sendMessage("Not a valid user ID! (Or user not found.)").queue();
                    } else event.getChannel().sendMessage("No user ID specified.").queue();
                    break;
                case "remove":
                    if (args.length > 1) {
                        User user = KekBot.jda.getUserById(args[1]);
                        if (user != null) {
                            if (!config.getBotMods().contains(args[1])) {
                                event.getChannel().sendMessage(user.getName() + " is not a bot mod.").queue();
                                return;
                            }

                            config.removeBotMod(args[1]).save();
                            event.getClient().removeBotMod(args[1]);
                            event.getChannel().sendMessage("Unregistered " + user.getName() + " as a bot mod.").queue();
                        } else event.getChannel().sendMessage("Not a valid user ID! (Or user not found.)").queue();
                    } else event.getChannel().sendMessage("No user ID specified.").queue();
                    break;
                case "list":
                    if (config.getBotMods().size() < 1) {
                        event.getChannel().sendMessage("There are no bot mods.").queue();
                        return;
                    }
                    
                    Paginator.Builder builder = new Paginator.Builder();
                    builder.setItemsPerPage(10);
                    builder.waitOnSinglePage(true);
                    builder.showPageNumbers(true);
                    builder.setFinalAction(m -> m.clearReactions().queue());
                    builder.addItems(config.getBotMods().stream().map(id -> KekBot.jda.getUserById(id).getName()).toArray(String[]::new));
                    builder.setEventWaiter(KekBot.waiter);
                    builder.setUsers(event.getAuthor());
                    builder.build().display(event.getChannel());
            }
        }
    }
}
