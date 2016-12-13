package com.godson.kekbot.commands.music;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Optional;

public class Skip {
    public static Command skip = new Command("skip")
            .withDescription("")
            .withUsage("{p}skip")
            .onExecuted(context -> {
                Optional<VoiceChannel> voiceChannel = context.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(context.getMember())).findFirst();
                if (!voiceChannel.isPresent()) {
                    context.getTextChannel().sendMessage("This command requies you to be in a voice channel!").queue();
                } else {
                    TextChannel channel = context.getTextChannel();
                    if (!context.getGuild().getAudioManager().isConnected()) {
                        channel.sendMessage("I'm not even playing music!").queue();
                    } else {
                        if (context.getGuild().getAudioManager().getConnectedChannel().equals(voiceChannel.get())) {
                            KekBot.player.skipTrack(context);
                        } else {
                            context.getTextChannel().sendMessage("You have to be in \"" + context.getGuild().getAudioManager().getConnectedChannel().getName() + " in order to use music commands.").queue();
                        }
                    }
                }
            });
}
