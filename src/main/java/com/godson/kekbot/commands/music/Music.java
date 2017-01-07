package com.godson.kekbot.commands.music;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Optional;

public class Music {
    public static Command music = new Command("music")
            .withDescription("")
            .withUsage("{p}music")
            .onExecuted(context -> {
                Optional<VoiceChannel> voiceChannel = context.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(context.getMember())).findFirst();
                if (!voiceChannel.isPresent()) {
                    context.getTextChannel().sendMessage(KekBot.respond(context, Action.GET_IN_VOICE_CHANNEL)).queue();
                } else {
                    if (context.getGuild().getAudioManager().isConnected()) {
                        if (KekBot.player.isMeme(context.getGuild())) {
                            context.getTextChannel().sendMessage("You cannot call music commands while memes are playing.").queue();
                        } else {
                            if (context.getGuild().getAudioManager().getConnectedChannel().equals(voiceChannel.get())) {
                                context.getTextChannel().sendMessage(KekBot.replacePrefix(context.getGuild(), "Music Commands: " +
                                        "\n{p}queue - **Queues a music track.**" +
                                        "\n{p}skip - **Skips a track. (Host Only)**" +
                                        "\n{p}song - **Gets the current song info.**" +
                                        "\n{p}playlist - **Lists all the tracks that are in the queue.**" +
                                        "\n{p}volume - **Sets the volume. (Host Only)**" +
                                        "\n{p}host - **Makes someone else the \"Host\". (Host Only)**" +
                                        "\n{p}stop - **Stops the current music session. (Host Only)**" +
                                        "\n" +
                                        "\nAll \"Host Only\" commands can also be executed by a user with `Administrator` permissions.")).queue();
                            } else context.getTextChannel().sendMessage(KekBot.respond(context, Action.MUSIC_NOT_IN_CHANNEL, "`" + context.getGuild().getAudioManager().getConnectedChannel().getName() + "`")).queue();
                        }
                    } else context.getTextChannel().sendMessage(KekBot.respond(context, Action.MUSIC_NOT_PLAYING)).queue();
                }
            });
}
