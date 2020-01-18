package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;

import java.io.File;
import java.util.Arrays;

public class Poosy extends Command {

    public Poosy() {
        name = "poosy";
        description = "\"Poosy...De...stroyer.\" ~Vinesauce Joel";
        usage.add("poosy");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        boolean reboot = (Arrays.stream(event.getArgs()).anyMatch(s -> s.equalsIgnoreCase("--reboot")));

        event.getChannel().sendTyping().queue();
        event.getChannel().sendFile(new File(reboot ? "resources/memegen/poosy-reboot.png" : "resources/memegen/poosy.png"), "poosy.png").queue();
    }
}
