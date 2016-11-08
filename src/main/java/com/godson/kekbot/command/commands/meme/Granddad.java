package com.godson.kekbot.command.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;
import net.dv8tion.jda.audio.player.FilePlayer;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.player.AbstractMusicPlayer;
import net.dv8tion.jda.player.JDAPlayerInfo;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.hooks.PlayerListenerAdapter;
import net.dv8tion.jda.player.hooks.events.FinishEvent;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import net.dv8tion.jda.player.source.AudioStream;
import net.dv8tion.jda.player.source.LocalSource;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Granddad {
    public static Command granddad = new Command("granddad")
            .withCategory(CommandCategory.MEME)
            .withDescription("FLEEENSTONES!?")
            .withUsage("{p}granddad")
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                Guild server = context.getGuild();
                List<Role> checkForMeme = server.getRolesByName("Living Meme");
                if (checkForMeme.size() == 0) {
                    channel.sendMessageAsync(":exclamation: __**Living Meme**__ role not found! Please add this role and assign it to me!", null);
                } else {
                    Role meme = checkForMeme.get(0);
                    if (server.getRolesForUser(context.getJDA().getSelfInfo()).contains(meme)) {
                        if (new File("Granddad").isDirectory()) {
                            File granddads[] = new File("Granddad").listFiles();
                            Random random = new Random();
                            int index = random.nextInt(granddads.length);
                            Optional<VoiceChannel> voiceChannel = context.getGuild().getVoiceChannels().stream().filter(c -> c.getUsers().contains(context.getAuthor())).findFirst();
                            if (!voiceChannel.isPresent()) {
                                channel.sendMessageAsync("This command requies you to be in a voice channel!", null);
                            } else {
                                AudioManager manager = context.getJDA().getAudioManager(context.getGuild());
                                MusicPlayer player;
                                if (manager.getSendingHandler() == null) {
                                    player = new MusicPlayer();
                                    manager.setSendingHandler(player);
                                } else {
                                    player = (MusicPlayer) manager.getSendingHandler();
                                }
                                player.addEventListener(new PlayerListenerAdapter() {
                                    @Override
                                    public void onFinish(FinishEvent event) {
                                        if (event.getPlayer().getAudioQueue().isEmpty())
                                            manager.closeAudioConnection();
                                    }
                                });
                                player.getAudioQueue().add(new LocalSource(granddads[index]));
                                if (!manager.isConnected()) {
                                    manager.openAudioConnection(voiceChannel.get());
                                } else {
                                    if (manager.getConnectedChannel() != voiceChannel.get()) {
                                        manager.moveAudioConnection(voiceChannel.get());
                                    }
                                }
                                if (player.isStopped()) {
                                    player.play();
                                }
                            }
                        }
                    } else {
                        channel.sendMessageAsync(":exclamation: This command requires me to have the __**Living Meme**__ role.", null);
                    }
                }
            });
}
