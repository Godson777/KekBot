package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class Spoiler extends Command {

    public Spoiler() {
        name = "spoiler";
        description = "Wraps your text in the most cancerous spoiler.";
        usage.add("spoiler <text>");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage(event.getString("command.meme.spoiler.noargs", event.getLocale())).queue();
            return;
        }

        String message = event.combineArgs();
        StringBuilder builder = new StringBuilder();
        for (char c : message.toCharArray()) {
            builder.append("||").append(c).append("||");
        }
        String spoiler = builder.toString();
        if (spoiler.length() > 2000) {
            event.getChannel().sendMessage(event.getString("command.meme.spoiler.toolong", event.getLocale())).queue();
            return;
        }
        event.getChannel().sendMessage(spoiler).queue();
    }
}
