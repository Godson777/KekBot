package com.godson.kekbot.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;

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
                Guild server = context.getGuild();
                TextChannel channel = context.getTextChannel();
                List<Role> checkForMeme = server.getRolesByName("Living Meme", true);
                if (checkForMeme.size() == 0) {
                    channel.sendMessage(":exclamation: __**Living Meme**__ role not found! Please add this role and assign it to me!").queue();
                } else {
                    Role meme = checkForMeme.get(0);
                    if (server.getSelfMember().getRoles().contains(meme)) {
                        if (new File("justright").isDirectory()) {
                            File justrights[] = new File("justright").listFiles();
                            Random random = new Random();
                            int index = random.nextInt(justrights.length);
                                try {
                                    channel.sendTyping();
                                    channel.sendFile(justrights[index], null).queue();
                                } catch (PermissionException e) {
                                    out.println("I do not have the 'Send Messages' permission in server: " + server.getName() + " - #" + channel.getName() + "! Aborting!");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        }
                    } else {
                        channel.sendMessage(":exclamation: This command requires me to have the __**Living Meme**__ role.").queue();
                    }
                }
            });
}
