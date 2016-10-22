package com.godson.kekbot.command.commands;


import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.EasyMessage;
import sx.blah.discord.handle.obj.IMessage;

public class ping {
    public static Command test = new Command("test")
            .withCategory(CommandCategory.TEST)
            .withDescription("Just a test command.")
            .withUsage("{p}test")
            .caseSensitive(true)
            .onExecuted(context -> {
                if (!context.getMessage().getAttachments().isEmpty()) {

                }
            });
}
