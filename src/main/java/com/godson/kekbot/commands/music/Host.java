package com.godson.kekbot.commands.music;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Optional;

public class Host {
    public static Command host = new Command("host")
            .withDescription("")
            .withAliases("{p}host <@mention>")
            .onExecuted(context -> {
                    Optional<VoiceChannel> voiceChannel = context.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(context.getMember())).findFirst();
                    if (!voiceChannel.isPresent()) {
                        context.getTextChannel().sendMessage(KekBot.respond(context, Action.GET_IN_VOICE_CHANNEL)).queue();
                    } else {
                        if (context.getGuild().getAudioManager().isConnected()) {
                        if (KekBot.player.getHost(context.getGuild()).equals(context.getAuthor()) || context.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                            if (KekBot.player.isMeme(context.getGuild())) {
                                context.getTextChannel().sendMessage("There is no host. Only memes.").queue();
                            } else {
                                if (context.getGuild().getAudioManager().getConnectedChannel().equals(voiceChannel.get())) {
                                    if (context.getArgs().length > 0) {
                                        if (context.getMessage().getMentionedUsers().size() > 0) {
                                            User newHost = context.getMessage().getMentionedUsers().get(0);
                                            KekBot.player.changeHost(context.getGuild(), newHost);
                                            context.getTextChannel().sendMessage("Done, " + newHost.getName() + " is now the host.").queue();
                                        } else {
                                            context.getTextChannel().sendMessage("You have to mention the user you wanna make the host!").queue();
                                        }
                                    } else {
                                        context.getTextChannel().sendMessage("You haven't specified who to make the host...").queue();
                                    }
                                } else {
                                    context.getTextChannel().sendMessage(KekBot.respond(context, Action.MUSIC_NOT_IN_CHANNEL, "`" + context.getGuild().getAudioManager().getConnectedChannel().getName() + "`")).queue();
                                }
                            }
                        } else {
                            context.getTextChannel().sendMessage("Only the host and users with the `Administrator` permission can set the volume!").queue();
                        }
                    } else context.getTextChannel().sendMessage(KekBot.respond(context, Action.MUSIC_NOT_PLAYING)).queue();
                }
            });
}
