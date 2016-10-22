package com.godson.kekbot.command.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.KekBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;

import java.util.List;

public class Salt {
    public static Command salt = new Command("salt")
            .withCategory(CommandCategory.MEME)
            .withDescription("Tells a story of how a specified user discovered salt.")
            .withUsage("{p}salt <@user>")
            .onExecuted(context -> {
                IChannel channel = context.getMessage().getChannel();
                IGuild server = context.getMessage().getGuild();
                String args[] = context.getArgs();
                List<IRole> checkForMeme = server.getRolesByName("Living Meme");
                if (checkForMeme.size() == 0) {
                    EasyMessage.send(channel, ":exclamation: __**Living Meme**__ role not found! Please add this role and assign it to me!");
                } else {
                    IRole meme = checkForMeme.get(0);
                    if (KekBot.client.getOurUser().getRolesForGuild(server).contains(meme)) {
                        if (args.length == 0) {
                            EasyMessage.send(channel, "You must supply a name or mention!");
                        } else {
                            String input = args[0];
                            EasyMessage.send(channel, "Sodium, atomic number 11, was first isolated by " + input + " in 1807. A chemical component of salt, he named it Na in honor of the saltiest region on earth, North America.");
                        }
                    } else {
                        EasyMessage.send(channel, ":exclamation: This command requires me to have the __**Living Meme**__ role.");
                    }
                }
            });
}
