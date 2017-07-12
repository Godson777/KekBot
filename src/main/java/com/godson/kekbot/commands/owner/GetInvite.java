package com.godson.kekbot.commands.owner;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GetInvite {
    public static Command getInvite = new Command("getinvite")
            .withCategory(CommandCategory.BOT_OWNER)
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().equals(context.getJDA().getUserById(GSONUtils.getConfig().getBotOwner()))) {
                    String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                    if (rawSplit.length == 1) {
                        context.getTextChannel().sendMessage("No guild specified.").queue();
                    } else {
                        List<Guild> guilds = new ArrayList<>();
                        if (KekBot.jdas.length > 1) {
                            for (JDA jda : KekBot.jdas) {
                                guilds.addAll(jda.getGuilds());
                            }
                        } else {
                            guilds = context.getJDA().getGuilds();
                        }
                        Optional<Guild> guild = guilds.stream().filter(g -> g.getName().equals(rawSplit[1])).findFirst();
                        if (guild.isPresent()) {
                            for (TextChannel channel : guild.get().getTextChannels()) {
                                    try {
                                        channel.createInvite().setMaxUses(1).setMaxAge(10L, TimeUnit.MINUTES).queue(invite -> context.getTextChannel().sendMessage("http://discord.gg/" + invite.getCode()).queue());
                                        break;
                                    } catch (PermissionException e) {
                                        if (channel == guild.get().getTextChannels().get(guild.get().getTextChannels().size()-1)) {
                                            context.getTextChannel().sendMessage("Couldn't get an invite for \"" + rawSplit[1] + "\". :frowning:").queue();
                                        }
                                    }
                            }
                        } else {
                            context.getTextChannel().sendMessage("Server not found.").queue();
                        }
                    }
                } else context.getTextChannel().sendMessage("This command can only be used by the bot owner!").queue();
            });
}
