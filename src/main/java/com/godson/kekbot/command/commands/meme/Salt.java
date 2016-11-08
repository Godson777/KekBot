package com.godson.kekbot.command.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;

import java.util.List;

public class Salt {
    public static Command salt = new Command("salt")
            .withCategory(CommandCategory.MEME)
            .withDescription("Tells a story of how a specified user discovered salt.")
            .withUsage("{p}salt <@user>")
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                Guild server = context.getGuild();
                String args[] = context.getArgs();
                List<Role> checkForMeme = server.getRolesByName("Living Meme");
                if (checkForMeme.size() == 0) {
                    channel.sendMessageAsync(":exclamation: __**Living Meme**__ role not found! Please add this role and assign it to me!", null);
                } else {
                    Role meme = checkForMeme.get(0);
                    if (server.getRolesForUser(context.getJDA().getSelfInfo()).contains(meme)) {
                        if (args.length == 0) {
                            channel.sendMessageAsync("You must supply a name or mention!", null);
                        } else {
                            String input = args[0];
                            channel.sendMessageAsync("Sodium, atomic number 11, was first isolated by " + input + " in 1807. A chemical component of salt, he named it Na in honor of the saltiest region on earth, North America.", null);
                        }
                    } else {
                        channel.sendMessageAsync(":exclamation: This command requires me to have the __**Living Meme**__ role.", null);
                    }
                }
            });
}
