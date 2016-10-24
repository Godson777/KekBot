package com.godson.kekbot.command.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.XMLUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListServers {
    public static Command listServers = new Command("listservers")
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().equals(KekBot.client.getUserByID(XMLUtils.getBotOwner()))) {
                    List<String> guilds = new ArrayList<String>();
                    List<String> pages = new ArrayList<String>();
                    KekBot.client.getGuilds().forEach(guild -> {
                        guilds.add(guild.getName() + "(in shard " + guild.getShard() + "/" + KekBot.client.getShardCount() + ")");
                    });
                    for (int i = 0; i < KekBot.client.getGuilds().size(); i += 25) {
                        try {
                            pages.add(StringUtils.join(guilds.subList(i, i + 25), "\n"));
                        } catch (IndexOutOfBoundsException e) {
                            pages.add(StringUtils.join(guilds.subList(i, guilds.size()), "\n"));
                        }
                    }
                }
            });
}
