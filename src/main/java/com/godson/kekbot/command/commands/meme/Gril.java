package com.godson.kekbot.command.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.exceptions.PermissionException;

import java.io.File;
import java.util.List;

import static java.lang.System.out;

public class Gril {
    public static Command gril = new Command("gril")
            .withAliases("girl", "topless")
            .withCategory(CommandCategory.MEME)
            .withDescription("Shows a topless gril.")
            .withUsage("{p}gril")
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                Guild server = context.getGuild();
                List<Role> checkForMeme = server.getRolesByName("Living Meme");
                if (checkForMeme.size() == 0) {
                    channel.sendMessage(":exclamation: __**Living Meme**__ role not found! Please add this role and assign it to me!");
                } else {
                    Role meme = checkForMeme.get(0);
                    if (server.getRolesForUser(KekBot.client.getSelfInfo()).contains(meme)) {
                        try {
                            channel.sendTyping();
                            channel.sendFile(new File("topless_grill.png"), null);
                        } catch (PermissionException e) {
                            out.println("I do not have the 'Send Messages' permission in server: " + server.getName() + " - #" + channel.getName() + "! Aborting!");
                        }
                    } else {
                        channel.sendMessage(":exclamation: This command requires me to have the __**Living Meme**__ role.");
                    }
                }
            });
}
