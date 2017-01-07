package com.godson.kekbot.commands.owner;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.Exceptions.ChannelNotFoundException;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Settings.Settings;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.net.MalformedURLException;

public class Broadcast {
    public static Command broadcast = new Command("broadcast")
            .withCategory(CommandCategory.BOT_OWNER)
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().getId().equals(GSONUtils.getConfig().getBotOwner())) {
                    String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                    if (rawSplit.length == 1) {
                        context.getMessage().getChannel().sendMessage("Cannot broadcast empty message.").queue();
                    } else {
                        for (JDA jda : KekBot.jdas) {
                            System.out.println("Entering Shard " + (jda.getShardInfo().getShardId()+1) + "...");
                            jda.getGuilds().forEach(guild -> {
                                Settings settings = GSONUtils.getSettings(guild);
                                if (settings.broadcastsEnabled()) {
                                    try {
                                        settings.getBroadcastChannel(guild).sendMessage("**BROADCAST: **" + rawSplit[1]).queue();
                                    } catch (ChannelNotFoundException e) {
                                        for (TextChannel channel : guild.getTextChannels()) {
                                            try {
                                                channel.sendMessage("**BROADCAST: **" + rawSplit[1]).queue();
                                                break;
                                            } catch (PermissionException er) {
                                                //¯\_(ツ)_/¯
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });
}
