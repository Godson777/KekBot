package com.godson.kekbot.commands.owner;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.GSONUtils;
import net.dv8tion.jda.core.entities.Guild;

import java.util.Optional;

public class GetInvite {
    public static Command getInvite = new Command("getinvite")
            .withCategory(CommandCategory.BOT_OWNER)
            .onExecuted(context -> {
                if (context.getMessage().getAuthor().equals(context.getJDA().getUserById(GSONUtils.getConfig().getBotOwner()))) {
                    String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                    if (rawSplit.length == 1) {
                        context.getTextChannel().sendMessage("No guild specified.").queue();
                    } else {
                        Optional<Guild> guild = context.getJDA().getGuilds().stream().filter(g -> g.getName().equals(rawSplit[1])).findFirst();
                        if (guild.isPresent()) {
                            /*final InviteUtil.AdvancedInvite[] invite = new InviteUtil.AdvancedInvite[1];
                            for (Channel channel : guild.getResponder().getTextChannels()) {
                                    try {
                                        invite[0] = InviteUtil.createInvite(channel, InviteUtil.InviteDuration.THIRTY_MINUTES, 1, false);
                                        break;
                                    } catch (PermissionException e) {
                                        //¯\_(ツ)_/¯
                                    }
                            }
                            if (invite[0] != null) context.getTextChannel().sendMessage("http://discord.gg/" + invite[0].getCode()).queue();
                            else context.getTextChannel().sendMessage("Couldn't getResponder an invite for \"" + rawSplit[1] + "\". :frowning:").queue();*/
                        } else {
                            context.getTextChannel().sendMessage("Server not found.").queue();
                        }
                    }
                } else context.getTextChannel().sendMessage("This command can only be used by the bot owner!").queue();
            });
}
