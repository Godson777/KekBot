package com.godson.kekbot.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.exceptions.PermissionException;

import java.io.File;
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
                List<Role> checkForMeme = server.getRolesByName("Living Meme");
                if (checkForMeme.size() == 0) {
                    channel.sendMessageAsync(":exclamation: __**Living Meme**__ role not found! Please add this role and assign it to me!", null);
                } else {
                    Role meme = checkForMeme.get(0);
                    if (server.getRolesForUser(context.getJDA().getSelfInfo()).contains(meme)) {
                        if (new File("justright").isDirectory()) {
                            File justrights[] = new File("justright").listFiles();
                            Random random = new Random();
                            int index = random.nextInt(justrights.length);
                                try {
                                    channel.sendTyping();
                                    channel.sendFile(justrights[index], null);
                                } catch (PermissionException e) {
                                    out.println("I do not have the 'Send Messages' permission in server: " + server.getName() + " - #" + channel.getName() + "! Aborting!");
                                }
                        }
                    } else {
                        channel.sendMessageAsync(":exclamation: This command requires me to have the __**Living Meme**__ role.", null);
                    }
                }
            });
}
