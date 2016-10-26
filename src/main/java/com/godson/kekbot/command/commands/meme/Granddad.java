package com.godson.kekbot.command.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.KekBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;
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
                    channel.sendMessage(":exclamation: __**Living Meme**__ role not found! Please add this role and assign it to me!");
                } else {
                    Role meme = checkForMeme.get(0);
                    if (server.getRolesForUser(KekBot.client.getSelfInfo()).contains(meme)) {
                        /*if (new File("Granddad").isDirectory()) {
                            File granddads[] = new File("Granddad").listFiles();
                            Random random = new Random();
                            int index = random.nextInt(granddads.length);
                            if (context.getMessage().getAuthor().getConnectedVoiceChannels().size() == 0) {
                                EasyMessage.send(channel, "This command requies you to be in a voice channel!");
                            } else {
                                if (context.getMessage().getAuthor().getConnectedVoiceChannels().get(0).getGuild() == server) {
                                    if (!context.getMessage().getAuthor().getConnectedVoiceChannels().get(0).isConnected()) {
                                        if (channel.getClient().getConnectedVoiceChannels().size() > 0) {
                                            int voiceChannels = KekBot.client.getConnectedVoiceChannels().size();
                                            for (int i = 0; i < voiceChannels; i++) {
                                                if (KekBot.client.getConnectedVoiceChannels().get(i).getGuild() == server) {
                                                    EasyMessage.send(channel, "This command is already being ran in: **" + KekBot.client.getConnectedVoiceChannels().get(i).getName() + "**!");
                                                } else {
                                                    try {
                                                        context.getMessage().getAuthor().getConnectedVoiceChannels().get(0).join();
                                                        try {
                                                            AudioPlayer.getAudioPlayerForGuild(server).queue(granddads[index]);
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
                                                    AudioPlayer.getAudioPlayerForGuild(server).queue(granddads[index]);
                                                } catch (UnsupportedAudioFileException | IOException e) {
                                                    e.printStackTrace();
                                                }
                                            } catch (MissingPermissionsException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        try {
                                            AudioPlayer.getAudioPlayerForGuild(server).queue(granddads[index]);
                                        } catch (UnsupportedAudioFileException | IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    EasyMessage.send(channel, "This command requies you to be in a voice channel!");
                                }
                            }
                        }*/
                        channel.sendMessage("Currently not available, testing JDA things. :cry:");
                    } else {
                        channel.sendMessage(":exclamation: This command requires me to have the __**Living Meme**__ role.");
                    }
                }
            });
}
