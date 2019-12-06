package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

public class Prefix extends Command {

    public Prefix() {
        name = "prefix";
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length > 0 && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessage("You cannot change the prefix from this command anymore, try the `settings` command.").queue();
            return;
        }

        event.getChannel().sendMessage("The prefix for `" + event.getGuild().getName() + "` is: `" + event.getPrefix() + "`").queue();
    }
}
