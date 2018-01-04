package com.godson.kekbot.command.commands.unported.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.File;
import java.util.List;

public class Poosy {
    public static Command poosy = new Command("poosy")
            .withAliases("destroyer")
            .withCategory(CommandCategory.MEME)
            .withDescription("\"Poosy...De...stroyer.\" ~Vinesauce Joel")
            .withUsage("{p}poosy")
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                Guild server = context.getGuild();
                List<Role> checkForMeme = server.getRolesByName("Living Meme", true);
                if (checkForMeme.size() == 0) {
                    channel.sendMessage(KekBot.respond(context, Action.MEME_NOT_FOUND, "__**Living Meme**__")).queue();
                } else {
                    Role meme = checkForMeme.get(0);
                    if (server.getSelfMember().getRoles().contains(meme)) {
                        channel.sendTyping().queue();
                        channel.sendFile(new File("resources/memegen/poosy.png"), "poosy.png", null).queue();
                    } else {
                        channel.sendMessage(KekBot.respond(context, Action.MEME_NOT_APPLIED, "__**Living Meme**__")).queue();
                    }
                }
            });
}