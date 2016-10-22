package com.godson.kekbot.command.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.KekBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static java.lang.System.out;

public class JustRight {
    public static Command justRight = new Command("justright")
            .withCategory(CommandCategory.MEME)
            .withDescription("When the memes are just right...")
            .withUsage("{p}justright")
            .onExecuted(context -> {
                IGuild server = context.getMessage().getGuild();
                IChannel channel = context.getMessage().getChannel();
                List<IRole> checkForMeme = server.getRolesByName("Living Meme");
                if (checkForMeme.size() == 0) {
                    EasyMessage.send(channel, ":exclamation: __**Living Meme**__ role not found! Please add this role and assign it to me!");
                } else {
                    IRole meme = checkForMeme.get(0);
                    if (KekBot.client.getOurUser().getRolesForGuild(server).contains(meme)) {
                        if (new File("justright").isDirectory()) {
                            File justrights[] = new File("justright").listFiles();
                            Random random = new Random();
                            int index = random.nextInt(justrights.length);
                            RequestBuffer.request(() -> {
                                try {
                                    if (!channel.getTypingStatus())
                                        channel.toggleTypingStatus();
                                    channel.sendFile(justrights[index]);
                                    if (channel.getTypingStatus())
                                        channel.toggleTypingStatus();
                                } catch (DiscordException | IOException e) {
                                    e.printStackTrace();
                                } catch (MissingPermissionsException e) {
                                    out.println("I do not have the 'Send Messages' permission in server: " + server.getName() + " - #" + channel.getName() + "! Aborting!");
                                }
                            });
                        }
                    } else {
                        EasyMessage.send(channel, ":exclamation: This command requires me to have the __**Living Meme**__ role.");
                    }
                }
            });
}
