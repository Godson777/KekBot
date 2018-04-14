package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;

import java.io.File;

public class Poosy extends Command {

    public Poosy() {
        name = "poosy";
        description = "\"Poosy...De...stroyer.\" ~Vinesauce Joel";
        usage.add("poosy");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        event.getChannel().sendTyping().queue();
        event.getChannel().sendFile(new File("resources/memegen/poosy.png"), "poosy.png", null).queue();
    }
}
