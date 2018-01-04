package com.godson.kekbot.command.commands.unported.music;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Optional;

public class Pause {
    public static Command pause = new Command("pause")
            .withDescription("")
            .withUsage("{p}pause")
            .onExecuted(context -> {
                Optional<VoiceChannel> voiceChannel = context.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(context.getMember())).findFirst();
                if (!voiceChannel.isPresent()) {
                    context.getTextChannel().sendMessage(KekBot.respond(context, Action.GET_IN_VOICE_CHANNEL)).queue();
                } else {
                    TextChannel channel = context.getTextChannel();
                    if (!context.getGuild().getAudioManager().isConnected()) {
                        channel.sendMessage(KekBot.respond(context, Action.MUSIC_NOT_PLAYING)).queue();
                    } else {
                        if (context.getGuild().getAudioManager().getConnectedChannel().equals(voiceChannel.get())) {
                            //KekBot.player.pauseTrack(context);
                        } else {
                            context.getTextChannel().sendMessage(KekBot.respond(context, Action.MUSIC_NOT_IN_CHANNEL, "`" + context.getGuild().getAudioManager().getConnectedChannel().getName() + "`")).queue();
                        }
                    }
                }
            });
}
