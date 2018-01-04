package com.godson.kekbot.command.commands.owner;

import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.settings.Config;
import net.dv8tion.jda.core.entities.User;

import java.util.Optional;

public class BlockUser extends Command {

    public BlockUser() {
        name = "blockuser";
        category = CommandCategories.botOwner;
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
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
                        event.getChannel().sendMessage(KekBot.respond(event, Action.NOT_A_NUMBER, "`" + args[1] + "`")).queue();
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
