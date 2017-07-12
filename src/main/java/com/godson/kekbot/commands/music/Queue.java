package com.godson.kekbot.commands.music;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Music.*;
import com.godson.kekbot.Music.Playlist;
import com.godson.kekbot.Profile.Profile;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.client.entities.CallUser;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Optional;

public class Queue {
    public static Command queue = new Command("queue")
            .withCategory(CommandCategory.FUN)
            .withDescription("Queues a song or custom playlist to play on a Voice Channel.")
            .withUsage("{p}queue <link>\n{p}queue playlist <playlist name> (Case Insensitive)")
            .onExecuted(context -> {
                Optional<VoiceChannel> voiceChannel = context.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(context.getMember())).findFirst();
                if (!voiceChannel.isPresent()) {
                    context.getTextChannel().sendMessage(KekBot.respond(context, Action.GET_IN_VOICE_CHANNEL)).queue();
                } else {
                    if (context.getGuild().getAudioManager().isConnected()) {
                        if (context.getGuild().getAudioManager().getConnectedChannel().equals(voiceChannel.get())) {
                            if (context.getArgs().length >= 1) {
                                if (context.getArgs()[0].equals("playlist")) {
                                    if (context.getArgs().length >= 2) {
                                        String playlistName = "";
                                        for (int i = 1; i < context.getArgs().length; i++) {
                                            if (i == context.getArgs().length-1) playlistName += context.getArgs()[i];
                                            else playlistName += context.getArgs()[i] + " ";
                                        }
                                        Profile profile = Profile.getProfile(context.getAuthor());
                                        String finalPlaylistName = playlistName;
                                        Optional<Playlist> playlist = profile.getPlaylists().stream().filter(playlist1 -> playlist1.getName().equals(finalPlaylistName)).findFirst();
                                        if (playlist.isPresent()) {
                                            KekBot.player.loadAndPlay(context, playlist.get());
                                        } else context.getTextChannel().sendMessage(CustomEmote.think() + " I'm not finding any playlists by that name... Did you type it correctly?").queue();
                                    } else context.getTextChannel().sendMessage("Huh? I get you want to queue a playlist, but you didn't give me the name of your playlist...").queue();
                                } else KekBot.player.loadAndPlay(context, context.getArgs()[0]);
                            } else {
                                context.getTextChannel().sendMessage("You haven't given a valid URL to queue. " + CustomEmote.think()).queue();
                            }
                        } else {
                            context.getTextChannel().sendMessage(KekBot.respond(context, Action.MUSIC_NOT_IN_CHANNEL, "`" + context.getGuild().getAudioManager().getConnectedChannel().getName() + "`")).queue();
                        }
                    } else {
                        if (context.getArgs().length >= 1) {
                            if (context.getArgs()[0].equals("playlist")) {
                                if (context.getArgs().length >= 2) {
                                    String playlistName = "";
                                    for (int i = 1; i < context.getArgs().length; i++) {
                                        if (i == context.getArgs().length-1) playlistName += context.getArgs()[i];
                                        else playlistName += context.getArgs()[i] + " ";
                                    }
                                    Profile profile = Profile.getProfile(context.getAuthor());
                                    String finalPlaylistName = playlistName;
                                    Optional<Playlist> playlist = profile.getPlaylists().stream().filter(playlist1 -> playlist1.getName().equalsIgnoreCase(finalPlaylistName)).findFirst();
                                    if (playlist.isPresent()) {
                                        KekBot.player.loadAndPlay(context, playlist.get());
                                    } else context.getTextChannel().sendMessage(CustomEmote.think() + " I'm not finding any playlists by that name... Did you type it correctly?").queue();
                                } else context.getTextChannel().sendMessage("Huh? I get you want to queue a playlist, but you didn't give me the name of your playlist...").queue();
                            } else KekBot.player.loadAndPlay(context, context.getArgs()[0]);
                        } else {
                            context.getTextChannel().sendMessage("You haven't given a valid URL to queue. " + CustomEmote.think()).queue();
                        }
                    }
                }
            });
}
