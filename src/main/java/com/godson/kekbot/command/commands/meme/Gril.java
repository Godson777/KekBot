package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;

import java.io.File;
import java.util.Arrays;

public class Gril extends Command {

    public Gril() {
        name = "gril";
        aliases = new String[]{"topless", "girl"};
        description = "Shows a topless gril.";
        usage.add("gril");
        category = CommandCategories.meme;
        cooldown = 5;
        cooldownScope = CooldownScope.USER_GUILD;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        boolean reboot = (Arrays.stream(event.getArgs()).anyMatch(s -> s.equalsIgnoreCase("--reboot")));

        event.getChannel().sendTyping().queue();
        event.getChannel().sendFile(new File(reboot ? "resources/memegen/topless_grill-reboot.png" : "resources/memegen/topless_grill.png"), "topless_gril.png").queue();
    }
}
