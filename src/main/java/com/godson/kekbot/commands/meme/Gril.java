package com.godson.kekbot.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
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
                    channel.sendMessageAsync(":exclamation: __**Living Meme**__ role not found! Please add this role and assign it to me!", null);
                } else {
                    Role meme = checkForMeme.get(0);
                    if (server.getRolesForUser(context.getJDA().getSelfInfo()).contains(meme)) {
                        try {
                            channel.sendTyping();
                            channel.sendFileAsync(new File("topless_grill.png"), null, null);
                        } catch (PermissionException e) {
                            out.println("I do not have the 'Send Messages' permission in server: " + server.getName() + " - #" + channel.getName() + "! Aborting!");
                        }
                    } else {
                        channel.sendMessageAsync(":exclamation: This command requires me to have the __**Living Meme**__ role.", null);
                    }
                }
            });
}
