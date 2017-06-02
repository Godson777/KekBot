package com.godson.kekbot.commands.music;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Optional;

public class Repeat {
    public static Command repeat = new Command("repeat")
            .withDescription("Sets the current playing song to loop on repeat.")
            .withUsage("{p}repeat")
            .onExecuted(context ->  {
                Optional<VoiceChannel> voiceChannel = context.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(context.getMember())).findFirst();
                if (!voiceChannel.isPresent()) {
                    context.getTextChannel().sendMessage(KekBot.respond(context, Action.GET_IN_VOICE_CHANNEL)).queue();
                } else {
                    TextChannel channel = context.getTextChannel();
                    if (!context.getGuild().getAudioManager().isConnected()) {
                        channel.sendMessage(KekBot.respond(context, Action.MUSIC_NOT_PLAYING)).queue();
                    } else {
                        if (context.getGuild().getAudioManager().getConnectedChannel().equals(voiceChannel.get())) {
                            KekBot.player.repeat(context);
                        } else {
                            context.getTextChannel().sendMessage(KekBot.respond(context, Action.MUSIC_NOT_IN_CHANNEL, "`" + context.getGuild().getAudioManager().getConnectedChannel().getName() + "`")).queue();
                        }
                    }
                }
            });
}
