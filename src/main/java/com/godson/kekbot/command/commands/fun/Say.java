package com.godson.kekbot.command.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.EasyMessage;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

public class Say {
    public static Command say = new Command("say")
            .withCategory(CommandCategory.FUN)
            .withDescription("Makes KekBot say whatever you want it to say.")
            .withUsage("{p}say <message>")
            .onExecuted(context -> {
                IChannel channel = context.getMessage().getChannel();
                String message = context.getMessage().getContent();
                String contents = message.substring(message.contains(" ") ? message.indexOf(" ") : message.length());
                if (!message.contains(" ")) {
                    EasyMessage.send(channel, ":anger: " + context.getMessage().getAuthor().mention() + ", could you at *least* give me something to *say*?");
                } else {
                    RequestBuffer.request(() -> {
                        try {
                            context.getMessage().delete();
                        } catch (DiscordException | MissingPermissionsException e) {
                            //do nothing.
                        }
                    });
                    EasyMessage.send(channel, contents);
                }
            });
}
