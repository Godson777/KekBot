package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class RIP extends Command {

    public RIP() {
        name = "rip";
        description = "Is someone kill? Mourn them with this command.";
        usage.add("rip <whoever>");
        category = new Category("Fun");
        cooldownScope = CooldownScope.USER_GUILD;
        cooldown = 10;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length > 0) {
            event.getChannel().sendMessage("http://ripme.xyz/#" + event.combineArgs().replace(" ", "%20")).queue();
        } else event.getChannel().sendMessage("Wait, so who is kill?").queue();
    }
}
