package com.godson.kekbot.commands.music;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Optional;

public class Repeat {
    public static Command repeat = new Command("repeat")
            .withDescription("Sets the current playing song to loop on repeat.")
            .withUsage("{p}repeat")
            .onExecuted(context -> {
                Optional<VoiceChannel> voiceChannel = context.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(context.getMember())).findFirst();
                if (!voiceChannel.isPresent()) {
                    context.getTextChannel().sendMessage(KekBot.respond(context, Action.GET_IN_VOICE_CHANNEL)).queue();
                } else {
                    TextChannel channel = context.getTextChannel();
                    if (!context.getGuild().getAudioManager().isConnected()) {
                        channel.sendMessage(KekBot.respond(context, Action.MUSIC_NOT_PLAYING)).queue();
                    } else {
                        if (KekBot.player.getHost(context.getGuild()).equals(context.getAuthor())) {
                            if (KekBot.player.isMeme(context.getGuild())) {
                                context.getTextChannel().sendMessage("Memes cannot be repeated, only music.").queue();
                            } else {
                                if (context.getGuild().getAudioManager().getConnectedChannel().equals(voiceChannel.get())) {
                                    KekBot.player.repeat(context);
                                } else {
                                    context.getTextChannel().sendMessage(KekBot.respond(context, Action.MUSIC_NOT_IN_CHANNEL, "`" + context.getGuild().getAudioManager().getConnectedChannel().getName() + "`")).queue();
                                }
                            }
                        } else if (context.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                            KekBot.player.repeat(context);
                        } else {
                            context.getTextChannel().sendMessage("Only the host and users with the `Administrator` permission can toggle the repeat status!").queue();
                        }
                    }
                }
            });
}
