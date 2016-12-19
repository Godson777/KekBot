package com.godson.kekbot.commands.owner;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListServers {
    public static Command listServers = new Command("listservers")
            .withCategory(CommandCategory.BOT_OWNER)
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().equals(context.getJDA().getUserById(GSONUtils.getConfig().getBotOwner()))) {
                    List<String> guilds = new ArrayList<String>();
                    context.getJDA().getGuilds().forEach(guild -> {
                        int bots = guild.getMembers().stream().map(Member::getUser).filter(User::isBot).collect(Collectors.toList()).size();
                        int users = guild.getMembers().size() - bots;
                        guilds.add("#" + guild.getName() /*+ " <in shard " + (guild.getInfo()[0]+1) + "/" + KekBot.jda.getShardCount() + ">"*/ +
                        " - Users: " + users + " - Bots: " + bots);
                    });
                    String message;
                    int page = 0;
                    if (context.getArgs().length > 0) {
                        try {
                            page = Integer.valueOf(context.getArgs()[0])-1;
                        } catch (NumberFormatException e) {
                            context.getTextChannel().sendMessage(KekBot.respond(context, Action.NOT_A_NUMBER, "`" + context.getArgs()[0] + "`")).queue();
                            return;
                        }
                    }
                    try {
                        if ((page * 30) > guilds.size() || (page * 30) < 0) {
                            context.getTextChannel().sendMessage("That page doesn't exist!").queue();
                            return;
                        }
                        else message = StringUtils.join(guilds.subList((page * 30), ((page + 1) * 30)), "\n") +
                                (guilds.size() > 30 ? "\n\nPage " + (page + 1) + "/" + (guilds.size() / 30 + 1) : "");
                    } catch (IndexOutOfBoundsException e) {
                        message = StringUtils.join(guilds.subList((page * 30), guilds.size()), "\n") +
                                (guilds.size() > 30 ? "\n\nPage " + (page + 1) + "/" + (guilds.size() / 30 + 1) : "");
                    }
                    context.getMessage().getChannel().sendMessage("```md\n" + message + "```").queue();
                }
            });
}
