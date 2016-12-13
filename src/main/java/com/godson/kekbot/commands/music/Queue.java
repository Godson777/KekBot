package com.godson.kekbot.commands.music;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Optional;

public class Queue {
    public static Command queue = new Command("queue")
            .withCategory(CommandCategory.FUN)
            .withDescription("Queues a song to play on a Voice Channel.")
            .withUsage("{p}queue <link>")
            .onExecuted(context -> {
                Optional<VoiceChannel> voiceChannel = context.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(context.getMember())).findFirst();
                if (!voiceChannel.isPresent()) {
                    context.getTextChannel().sendMessage("This command requies you to be in a voice channel!").queue();
                } else {
                    if (context.getGuild().getAudioManager().isConnected()) {
                        if (context.getGuild().getAudioManager().getConnectedChannel().equals(voiceChannel.get())) {
                            if (context.getArgs().length >= 1) {
                                KekBot.player.loadAndPlay(context, context.getArgs()[0]);
                            } else {
                                context.getTextChannel().sendMessage("You haven't given a valid URL to queue. :thinking:").queue();
                            }
                        } else {
                            context.getTextChannel().sendMessage("You have to be in \"" + context.getGuild().getAudioManager().getConnectedChannel().getName() + "\" in order to queue music.").queue();
                        }
                    } else {
                        if (context.getArgs().length >= 1) {
                            KekBot.player.loadAndPlay(context, context.getArgs()[0]);
                        } else {
                            context.getTextChannel().sendMessage("You haven't given a valid URL to queue. :thinking:").queue();
                        }
                    }
                }
            });
}
