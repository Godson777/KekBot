package com.godson.kekbot.command.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.XMLUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IInvite;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

import java.util.Optional;

public class GetInvite {
    public static Command getInvite = new Command("getinvite")
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().equals(KekBot.client.getUserByID(XMLUtils.getBotOwner()))) {
                    String rawSplit[] = context.getMessage().getContent().split(" ", 2);
                    if (rawSplit.length == 1) {
                        EasyMessage.send(context.getMessage().getChannel(), "No guild specified.");
                    } else {
                        Optional<IGuild> guild = KekBot.client.getGuilds().stream().filter(g -> g.getName().equals(rawSplit[1])).findFirst();
                        if (guild.isPresent()) {
                            final IInvite[] invite = new IInvite[1];
                            RequestBuffer.request(() -> {
                            for (IChannel channel : guild.get().getChannels()) {
                                    try {
                                        invite[0] = channel.createInvite(5, 1, false, false);
                                        break;
                                    } catch (MissingPermissionsException e) {
                                         if (e.getLocalizedMessage().equals("Missing permissions: CREATE_INVITE!")) {
                                             //o well
                                         }
                                    } catch (DiscordException e) {
                                        e.printStackTrace();
                                    }
                            }
                            });
                            if (invite[0] != null) EasyMessage.send(context.getMessage().getChannel(), "http://discord.gg/" + invite[0].getInviteCode());
                            else EasyMessage.send(context.getMessage().getChannel(), "Couldn't get an invite for \"" + rawSplit[1] + "\". :frowning:");
                        } else {
                            EasyMessage.send(context.getMessage().getChannel(), "Server not found.");
                        }
                    }
                } else EasyMessage.send(context.getMessage().getChannel(), "This command can only be used by the bot owner!");
            });
}
