package com.godson.kekbot.command.commands.owner;

import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.concurrent.TimeUnit;

public class GetInvite extends Command {

    //This command is only used for messing with friends servers and joining servers where tickets are made, depending on the conditions of the ticket.
    public GetInvite() {
        name = "getinvite";
        category = CommandCategories.botOwner;
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        if (event.getArgs().length > 0) {
            Guild guild = KekBot.jda.getGuildById(event.getArgs()[0]);
            if (guild != null) {
                for (TextChannel channel : guild.getTextChannels()) {
                    try {
                        channel.createInvite().setMaxUses(1).setMaxAge(10L, TimeUnit.MINUTES).queue(invite -> event.getTextChannel().sendMessage("http://discord.gg/" + invite.getCode()).queue());
                        break;
                    } catch (PermissionException e) {
                        if (channel == guild.getTextChannels().get(guild.getTextChannels().size() - 1)) {
                            event.getChannel().sendMessage("Couldn't get an invite for \"" + event.getArgs()[0] + "\". :frowning:").queue();
                        }
                    }
                }
            } else event.getChannel().sendMessage("Server not found.").queue();
        } else event.getChannel().sendMessage("No guild specified.").queue();
    }
}
