package com.godson.kekbot.commands.owner;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.GSONUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class Whereis {
    public static Command test = new Command("test")
            .withCategory(CommandCategory.BOT_OWNER)
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().getId().equals(GSONUtils.getConfig().getBotOwner())) {
                    List<Guild> list = context.getJDA().getGuilds().stream().filter(guild -> guild.getMembers().stream().map(Member::getUser).anyMatch(user -> user.equals(context.getJDA().getUserById(context.getArgs()[0])))).collect(Collectors.toList());
                    context.getTextChannel().sendMessage(StringUtils.join(list, ", ")).queue();
                }
            });
}
