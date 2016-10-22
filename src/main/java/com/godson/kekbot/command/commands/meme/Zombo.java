package com.godson.kekbot.command.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.KekBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.audio.AudioPlayer;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Zombo {
    static File zombomp3 = new File("zombo.mp3");
    public static Command zombo = new Command("zombo")
            .withCategory(CommandCategory.MEME)
            .withDescription("Welcome, to zombocom...")
            .withUsage("{p}zombo")
            .onExecuted(context -> {
                IGuild server = context.getMessage().getGuild();
                IChannel channel = context.getMessage().getChannel();
                List<IRole> checkForMeme = server.getRolesByName("Living Meme");
                if (checkForMeme.size() == 0) {
                    EasyMessage.send(channel, ":exclamation: __**Living Meme**__ role not found! Please add this role and assign it to me!");
                } else if (checkForMeme.size() == 1) {
                    IRole meme = checkForMeme.get(0);
                    if (KekBot.client.getOurUser().getRolesForGuild(server).contains(meme)) {
                        if (context.getMessage().getAuthor().getConnectedVoiceChannels().size() == 0) {
                            EasyMessage.send(channel, "This command requires you to be in a voice channel!");
                        } else {
                            if (context.getMessage().getAuthor().getConnectedVoiceChannels().get(0).getGuild() == server) {
                                if (!context.getMessage().getAuthor().getConnectedVoiceChannels().get(0).isConnected()) {
                                    if (KekBot.client.getConnectedVoiceChannels().size() >= 1) {
                                        for (int i = 0; i < KekBot.client.getConnectedVoiceChannels().size(); i++) {
                                            if (KekBot.client.getConnectedVoiceChannels().get(i).getGuild() == server) {
                                                EasyMessage.send(channel, "This command is already being ran in: **" + KekBot.client.getConnectedVoiceChannels().get(i).getName() + "**!");
                                            } else {
                                                try {
                                                    context.getMessage().getAuthor().getConnectedVoiceChannels().get(0).join();
                                                    try {
                                                        AudioPlayer.getAudioPlayerForGuild(server).queue(zombomp3);
                                                    } catch (UnsupportedAudioFileException | IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                } catch (MissingPermissionsException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    } else {
                                        try {
                                            context.getMessage().getAuthor().getConnectedVoiceChannels().get(0).join();
                                            try {
                                                AudioPlayer.getAudioPlayerForGuild(server).queue(zombomp3);
                                            } catch (UnsupportedAudioFileException | IOException e) {
                                                e.printStackTrace();
                                            }
                                        } catch (MissingPermissionsException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    try {
                                        AudioPlayer.getAudioPlayerForGuild(server).queue(zombomp3);
                                    } catch (UnsupportedAudioFileException | IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                EasyMessage.send(channel, "This command requires you to be in a voice channel!");
                            }
                        }
                    } else {
                        EasyMessage.send(channel, ":exclamation: This command requires me to have the __**Living Meme**__ role.");
                    }
                }
            });
}
