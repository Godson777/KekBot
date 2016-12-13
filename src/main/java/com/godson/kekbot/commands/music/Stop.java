package com.godson.kekbot.commands.music;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Optional;

public class Stop {
    public static Command stop = new Command("stop")
            .withDescription("")
            .withUsage("{p}stop")
            .onExecuted(context -> {
                Optional<VoiceChannel> voiceChannel = context.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(context.getMember())).findFirst();
                if (!voiceChannel.isPresent()) {
                    context.getTextChannel().sendMessage("This command requies you to be in a voice channel!").queue();
                } else {
                    if (context.getGuild().getAudioManager().isConnected()) {
                        if (KekBot.player.getHost(context.getGuild()).equals(context.getAuthor())) {
                            if (KekBot.player.isMeme(context.getGuild())) {
                                context.getTextChannel().sendMessage("You can't stop teh memes mannnnnn...").queue();
                            } else {
                                if (context.getGuild().getAudioManager().getConnectedChannel().equals(voiceChannel.get())) {
                                    KekBot.player.closeConnection(context.getGuild());
                                } else {
                                    context.getTextChannel().sendMessage("You have to be in \"" + context.getGuild().getAudioManager().getConnectedChannel().getName() + "\" in order to use music commands.").queue();
                                }
                            }
                        } else if (context.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                            KekBot.player.closeConnection(context.getGuild());
                        } else {
                            context.getTextChannel().sendMessage("Only the host and users with the `Administrator` permission can stop a music session and memes!").queue();
                        }
                    } else context.getTextChannel().sendMessage("There isn't even anything playing. :neutral_face:").queue();
                }
            });
}
