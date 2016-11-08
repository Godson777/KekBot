package com.godson.kekbot.command.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.Exceptions.ChannelNotFoundException;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Settings.Settings;
import com.godson.kekbot.XMLUtils;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.exceptions.PermissionException;

public class Broadcast {
    public static Command broadcast = new Command("broadcast")
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().getId().equals(GSONUtils.getConfig().getBotOwner())) {
                    String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                    if (rawSplit.length == 1) {
                        context.getMessage().getChannel().sendMessageAsync("Cannot broadcast empty message.", null);
                    } else {
                        for (JDA jda : KekBot.jdas) {
                            for (Guild guild : jda.getGuilds()) {
                                Settings settings = GSONUtils.getSettings(guild);
                                if (settings.broadcastsEnabled()) {
                                    try {
                                        settings.getBroadcastChannel(context.getJDA()).sendMessageAsync("**BROADCAST: **" + rawSplit[1], null);
                                    } catch (ChannelNotFoundException e) {
                                        for (TextChannel channel : guild.getTextChannels()) {
                                            try {
                                                channel.sendMessageAsync("**BROADCAST: **" + rawSplit[1], null);
                                                break;
                                            } catch (PermissionException er) {
                                                //¯\_(ツ)_/¯
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
}
