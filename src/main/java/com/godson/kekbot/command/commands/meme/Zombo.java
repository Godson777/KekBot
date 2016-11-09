package com.godson.kekbot.command.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;

import java.io.File;
import java.util.List;

public class Zombo {
    static File zombomp3 = new File("zombo.mp3");
    public static Command zombo = new Command("zombo")
            .withCategory(CommandCategory.MEME)
            .withDescription("Welcome, to zombocom...")
            .withUsage("{p}zombo")
            .onExecuted(context -> {
                Guild server = context.getGuild();
                TextChannel channel = context.getTextChannel();
                List<Role> checkForMeme = server.getRolesByName("Living Meme");
                if (checkForMeme.size() == 0) {
                    channel.sendMessageAsync(":exclamation: __**Living Meme**__ role not found! Please add this role and assign it to me!", null);
                } else {
                    Role meme = checkForMeme.get(0);
                    if (server.getRolesForUser(context.getJDA().getSelfInfo()).contains(meme)) {
                        /*if (context.getMessage().getAuthor().getConnectedVoiceChannels().size() == 0) {
                            EasyMessage.send(channel, "This command requires you to be in a voice channel!");
                        } else {
                            if (context.getMessage().getAuthor().getConnectedVoiceChannels().get(0).getGuild() == server) {
                                if (!context.getMessage().getAuthor().getConnectedVoiceChannels().get(0).isConnected()) {
                                    if (KekBot.jda.getConnectedVoiceChannels().size() >= 1) {
                                        for (int i = 0; i < KekBot.jda.getConnectedVoiceChannels().size(); i++) {
                                            if (KekBot.jda.getConnectedVoiceChannels().get(i).getGuild() == server) {
                                                EasyMessage.send(channel, "This command is already being ran in: **" + KekBot.jda.getConnectedVoiceChannels().get(i).getName() + "**!");
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
                        }*/
                    } else {
                        channel.sendMessageAsync(":exclamation: This command requires me to have the __**Living Meme**__ role.", null);
                    }
                }
            });
}
