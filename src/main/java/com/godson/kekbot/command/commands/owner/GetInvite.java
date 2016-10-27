package com.godson.kekbot.command.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.XMLUtils;
import net.dv8tion.jda.entities.Channel;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.utils.InviteUtil;

import java.util.Optional;

public class GetInvite {
    public static Command getInvite = new Command("getinvite")
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().equals(KekBot.client.getUserById(XMLUtils.getBotOwner()))) {
                    String rawSplit[] = context.getMessage().getContent().split(" ", 2);
                    if (rawSplit.length == 1) {
                        context.getTextChannel().sendMessage("No guild specified.");
                    } else {
                        Optional<Guild> guild = KekBot.client.getGuilds().stream().filter(g -> g.getName().equals(rawSplit[1])).findFirst();
                        if (guild.isPresent()) {
                            final InviteUtil.AdvancedInvite[] invite = new InviteUtil.AdvancedInvite[1];
                            for (Channel channel : guild.get().getTextChannels()) {
                                    try {
                                        invite[0] = InviteUtil.createInvite(channel, InviteUtil.InviteDuration.THIRTY_MINUTES, 1, false);
                                        break;
                                    } catch (PermissionException e) {
                                        //¯\_(ツ)_/¯
                                    }
                            }
                            if (invite[0] != null) context.getTextChannel().sendMessage("http://discord.gg/" + invite[0].getCode());
                            else context.getTextChannel().sendMessage("Couldn't get an invite for \"" + rawSplit[1] + "\". :frowning:");
                        } else {
                            context.getTextChannel().sendMessage("Server not found.");
                        }
                    }
                } else context.getTextChannel().sendMessage("This command can only be used by the bot owner!");
            });
}
