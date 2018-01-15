package com.godson.kekbot.command.commands.botowner;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.core.JDA;

public class Shutdown extends Command {

    public Shutdown() {
        name = "shutdown";
        category = new Category("Bot Owner");
        commandPermission = CommandPermission.OWNER;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        event.getEvent().getMessage().getChannel().sendMessage("Well, looks like this is it for me! Hope you all had a gay old time!").queue();

        String reason = "No Description Provided.";
        if (event.getArgs().length > 0) reason = event.combineArgs();


        KekBot.shutdown(reason);

        for (JDA jda : KekBot.jda.getShards()) jda.shutdown();
    }
}
