package com.godson.kekbot.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;

import java.io.File;
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
                List<Role> checkForMeme = server.getRolesByName("Living Meme");
                if (checkForMeme.size() == 0) {
                    channel.sendMessageAsync(":exclamation: __**Living Meme**__ role not found! Please add this role and assign it to me!", null);
                } else {
                    Role meme = checkForMeme.get(0);
                    if (server.getRolesForUser(context.getJDA().getSelfInfo()).contains(meme)) {
                        channel.sendTyping();
                        channel.sendFileAsync(new File("poosy.png"), null, null);
                    } else {
                        channel.sendMessageAsync(":exclamation: This command requires me to have the __**Living Meme**__ role.", null);
                    }
                }
            });
}
