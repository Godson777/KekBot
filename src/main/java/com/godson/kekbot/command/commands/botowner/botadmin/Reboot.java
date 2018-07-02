package com.godson.kekbot.command.commands.botowner.botadmin;

import com.godson.kekbot.ExitCode;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;

public class Reboot extends Command {

    public Reboot() {
        name = "reboot";
        description = "Reboots the entire bot, or a shard.";
        category = new Category("Bot Admin");
        commandPermission = CommandPermission.ADMIN;
        usage.add("reboot");
        usage.add("reboot soft");
        usage.add("reboot <shard>");
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage("Performing a hard reboot in one minute...").queue();
            KekBot.shutdownListener.setExitCode(ExitCode.REBOOT);
            KekBot.shutdown("Reboot incoming, KekBot will be back shortly...");
            return;
        }

        if (event.getArgs()[0].equalsIgnoreCase("soft")) {
            event.getChannel().sendMessage("Soft rebooting all shards...").queue();
            KekBot.shutdownListener.softReboot(event.getChannel());
            return;
        }

        try {
            int shard = Integer.parseInt(event.getArgs()[0]) - 1;
            if (shard > event.getJDA().getShardInfo().getShardTotal() || shard < 0) {
                event.getChannel().sendMessage("Shard does not exist.").queue();
                return;
            }
            event.getChannel().sendMessage("Soft rebooting Shard " + (shard + 1) + "...").queue();
            KekBot.shutdownListener.softReboot(event.getChannel(), shard);
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), "`" + event.getArgs()[0] + "`")).queue();
        }
    }
}
