package com.godson.kekbot.commands.music;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Optional;

public class Volume {
    public static Command volume = new Command("volume")
            .withDescription("")
            .withUsage("{p}volume <number>")
            .onExecuted(context -> {
                Optional<VoiceChannel> voiceChannel = context.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(context.getMember())).findFirst();
                if (!voiceChannel.isPresent()) {
                    context.getTextChannel().sendMessage("This command requies you to be in a voice channel!").queue();
                } else {
                    if (context.getGuild().getAudioManager().isConnected()) {
                        if (KekBot.player.getHost(context.getGuild()).equals(context.getAuthor()) || context.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                            if (KekBot.player.isMeme(context.getGuild())) {
                                context.getTextChannel().sendMessage("You can't change the volume of my dank memes. :neutral_face:").queue();
                            } else {
                                if (context.getGuild().getAudioManager().getConnectedChannel().equals(voiceChannel.get())) {
                                    if (context.getArgs().length > 0) {
                                        try {
                                            int volume = Integer.valueOf(context.getArgs()[0]);
                                            KekBot.player.setVolume(context, volume);
                                        } catch (NumberFormatException e) {
                                            context.getTextChannel().sendMessage(KekBot.respond(context, Action.NOT_A_NUMBER, context.getArgs()[0])).queue();
                                        }
                                    } else {
                                        context.getTextChannel().sendMessage("You haven't even specified the volume you want to set it to!").queue();
                                    }
                                } else {
                                    context.getTextChannel().sendMessage("You have to be in \"" + context.getGuild().getAudioManager().getConnectedChannel().getName() + "\" in order to use music commands.").queue();
                                }
                            }
                        } else {
                            context.getTextChannel().sendMessage("Only the host and users with the `Administrator` permission can set the volume!").queue();
                        }
                    } else {
                        context.getTextChannel().sendMessage("There isn't even anything playing... :neutral_face:").queue();
                    }
                }
            });
}
