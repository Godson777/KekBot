package com.godson.kekbot.commands.owner;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.GSONUtils;
import net.dv8tion.jda.entities.Channel;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.utils.InviteUtil;

import java.util.Optional;

public class GetInvite {
    public static Command getInvite = new Command("getinvite")
            .withCategory(CommandCategory.BOT_OWNER)
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().equals(context.getJDA().getUserById(GSONUtils.getConfig().getBotOwner()))) {
                    String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                    if (rawSplit.length == 1) {
                        context.getTextChannel().sendMessageAsync("No guild specified.", null);
                    } else {
                        Optional<Guild> guild = context.getJDA().getGuilds().stream().filter(g -> g.getName().equals(rawSplit[1])).findFirst();
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
                            if (invite[0] != null) context.getTextChannel().sendMessageAsync("http://discord.gg/" + invite[0].getCode(), null);
                            else context.getTextChannel().sendMessageAsync("Couldn't get an invite for \"" + rawSplit[1] + "\". :frowning:", null);
                        } else {
                            context.getTextChannel().sendMessageAsync("Server not found.", null);
                        }
                    }
                } else context.getTextChannel().sendMessageAsync("This command can only be used by the bot owner!", null);
            });
}
