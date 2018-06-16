package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;

import java.io.File;

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
        event.getChannel().sendTyping().queue();
        event.getChannel().sendFile(new File("resources/memegen/topless_grill.png"), "topless_gril.png", null).queue();
    }
}
