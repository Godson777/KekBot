package com.godson.kekbot.command.commands.botowner;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.settings.Config;
import net.dv8tion.jda.api.entities.User;

public class BlockUser extends Command {

    public BlockUser() {
        name = "blockuser";
        category = new Category("Bot Owner");
        commandPermission = CommandPermission.OWNER;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        String args[] = event.getArgs();
        Config config = Config.getConfig();
        if (args.length > 0) {
            User user = KekBot.jda.getUserById(args[0]);
            if (user != null) {
                if (args.length > 1) {
                    try {
                        int type = Integer.valueOf(args[1]);
                        if (type == -1) {
                            config.removeBlockedUser(args[0]);
                            event.getClient().undisableUser(args[0]);
                            event.getChannel().sendMessage(user.getName() + " was removed from the blacklist.").queue();
                        } else {
                            config.addBlockedUser(args[0], type);
                            event.getClient().disableUser(args[0], type);
                            event.getChannel().sendMessage(user.getName() + " was added to the blacklist under type " + type + ".").queue();
                        }
                    } catch (NumberFormatException e) {
                        event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), "`" + args[1] + "`")).queue();
                    }
                } else event.getChannel().sendMessage("No block type specified.").queue();
            } else {
                event.getChannel().sendMessage("Not a valid user ID!").queue();
            }
        } else {
            event.getChannel().sendMessage("No user ID specified.").queue();
        }
    }
}
