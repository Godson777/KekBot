package com.godson.kekbot.command.commands.owner;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.GSONUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ListServers {
    public static Command listServers = new Command("listservers")
            .withCategory(CommandCategory.BOT_OWNER)
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().equals(context.getJDA().getUserById(GSONUtils.getConfig().getBotOwner()))) {
                    List<String> guilds = new ArrayList<String>();
                    List<String> pages = new ArrayList<String>();
                    context.getJDA().getGuilds().forEach(guild -> {
                        guilds.add(guild.getName() /*+ " <in shard " + (guild.getInfo()[0]+1) + "/" + KekBot.jda.getShardCount() + ">"*/);
                    });
                    for (int i = 0; i < context.getJDA().getGuilds().size(); i += 25) {
                        try {
                            pages.add(StringUtils.join(guilds.subList(i, i + 25), "\n"));
                        } catch (IndexOutOfBoundsException e) {
                            pages.add(StringUtils.join(guilds.subList(i, guilds.size()), "\n"));
                        }
                    }
                    context.getMessage().getChannel().sendMessageAsync("```md\n" + pages.get(0) + "```", null);
                }
            });
}
