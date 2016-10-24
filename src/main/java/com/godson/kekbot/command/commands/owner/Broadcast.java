package com.godson.kekbot.command.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.XMLUtils;
import org.jdom2.JDOMException;

import java.io.IOException;

public class Broadcast {
    public static Command broadcast = new Command("broadcast")
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().getID().equals(XMLUtils.getBotOwner())) {
                    String rawSplit[] = context.getMessage().getContent().split(" ", 2);
                    if (rawSplit.length == 1) {
                        EasyMessage.send(context.getMessage().getChannel(), "Cannot broadcast empty message.");
                    } else {
                        for (int i = 0; i < KekBot.client.getGuilds().size(); i++) {
                            try {
                                XMLUtils.broadcast(KekBot.client.getGuilds().get(i), rawSplit[1]);
                            } catch (JDOMException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
}
