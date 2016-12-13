package com.godson.kekbot.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Poosy {
    public static Command destroyer = new Command("poosy")
            .withAliases("destroyer")
            .withCategory(CommandCategory.MEME)
            .withDescription("\"Poosy...De...stroyer.\" ~Vinesauce Joel")
            .withUsage("{p}pussydestroyer")
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                Guild server = context.getGuild();
                List<Role> checkForMeme = server.getRolesByName("Living Meme", true);
                if (checkForMeme.size() == 0) {
                    channel.sendMessage(":exclamation: __**Living Meme**__ role not found! Please add this role and assign it to me!").queue();
                } else {
                    Role meme = checkForMeme.get(0);
                    if (server.getSelfMember().getRoles().contains(meme)) {
                        try {
                            channel.sendTyping();
                            channel.sendFile(new File("poosy.png"), null).queue();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        channel.sendMessage(":exclamation: This command requires me to have the __**Living Meme**__ role.").queue();
                    }
                }
            });
}
