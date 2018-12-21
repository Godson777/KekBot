package com.godson.kekbot.command.commands.botowner.botadmin;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.Version;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.util.Utils;

public class Update extends Command {

    public Update() {
        name = "update";
        description = "Checks if there's an update for KekBot and updates if there is one";
        usage.add("update");
        commandPermission = CommandPermission.MOD;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        Version latest = Utils.getLatestVersion(KekBot.version.getBetaVersion() > 0);

        if (!latest.isHigherThan(KekBot.version) && !event.getMessage().getContentRaw().contains("--forced")) {
            event.getChannel().sendMessage("KekBot is currently running the latest version. (" + KekBot.version.toString() + ")").queue();
            return;
        }

        event.getChannel().sendMessage("Update found! (Current: " + KekBot.version.toString() + " | Latest: " + latest.toString() + ") Updating...").queue();
        KekBot.update();
    }
}
