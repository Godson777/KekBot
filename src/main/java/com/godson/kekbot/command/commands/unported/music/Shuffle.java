package com.godson.kekbot.command.commands.unported.music;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Optional;

public class Shuffle {
    public static Command shuffle = new Command("shuffle")
            .withDescription("Shuffles the \"Now Playing\" list.")
            .withUsage("{p}shuffle")
            .onExecuted(context -> {
                Optional<VoiceChannel> voiceChannel = context.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(context.getMember())).findFirst();
                if (!voiceChannel.isPresent()) {
                    context.getTextChannel().sendMessage(KekBot.respond(context, Action.GET_IN_VOICE_CHANNEL)).queue();
                } else {
                    if (context.getGuild().getAudioManager().isConnected()) {
                        if (KekBot.player.getHost(context.getGuild()).equals(context.getAuthor()) || context.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                            if (KekBot.player.isMeme(context.getGuild())) {
                                context.getTextChannel().sendMessage("You can't shuffle memes. :neutral_face:").queue();
                            } else {
                                if (context.getGuild().getAudioManager().getConnectedChannel().equals(voiceChannel.get())) {
                                    KekBot.player.shuffle(context);
                                } else {
                                    context.getTextChannel().sendMessage(KekBot.respond(context, Action.MUSIC_NOT_IN_CHANNEL, "`" + context.getGuild().getAudioManager().getConnectedChannel().getName() + "`")).queue();
                                }
                            }
                        } else {
                            context.getTextChannel().sendMessage("Only the host and users with the `Administrator` permission can shuffle the \"Now Playing\" list!").queue();
                        }
                    } else {
                        context.getTextChannel().sendMessage(KekBot.respond(context, Action.MUSIC_NOT_PLAYING)).queue();
                    }
                }
            });
}
