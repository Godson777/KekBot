package com.godson.kekbot.command.commands.admin;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Ban extends Command {

    public Ban() {
        name = "ban";
        description = "Bans a specified user or users.";
        usage.add("ban <@user> {@user}...");
        category = new Category("Admin");
        requiredBotPerms = new Permission[]{Permission.BAN_MEMBERS};
        requiredUserPerms = new Permission[]{Permission.BAN_MEMBERS};
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage(KekBot.respond(Action.BAN_EMPTY)).queue();
        }

        if (event.getMessage().getMentionedUsers().size() == 0) {
            event.getChannel().sendMessage(event.getMessage().getAuthor().getAsMention() + " The user you want to ban __**must**__ be in the form of a mention!").queue();
        } else if (event.getMessage().getMentionedUsers().size() == 1) {
            if (event.getMessage().getMentionedUsers().get(0) == event.getJDA().getSelfUser()) {
                event.getChannel().sendMessage(":frowning: I can't ban myself...").queue();
            } else if (event.getMessage().getMentionedUsers().get(0).equals(event.getMessage().getAuthor())) {
                event.getChannel().sendMessage("Why would you want to ban yourself? That seems kinda useless to me...").queue();
            } else {
                if (event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0)).getRoles().stream().map(net.dv8tion.jda.core.entities.Role::getPositionRaw).max(Integer::compareTo).get() >= event.getMember().getRoles().stream().map(net.dv8tion.jda.core.entities.Role::getPositionRaw).max(Integer::compareTo).get()) {
                    event.getChannel().sendMessage("You can't ban someone who's highest role is the same as or is higher than yours.").queue();
                } else {
                    try {
                        event.getGuild().getController().ban(event.getMessage().getMentionedUsers().get(0), 0).reason("Banned by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                        event.getChannel().sendMessage(KekBot.respond(Action.BAN_SUCCESS, "`" + event.getMessage().getMentionedUsers().get(0).getName() + "`")).queue();
                    } catch (PermissionException e) {
                        event.getChannel().sendMessage("`" + event.getMessage().getMentionedUsers().get(0).getName() + "`'s role is higher than mine. I am unable to ban them.").queue();
                    }
                }
            }
        } else {
            List<String> users = new ArrayList<>();
            List<String> failed = new ArrayList<>();
            for (int i = 0; i < event.getMessage().getMentionedUsers().size(); i++) {
                if (event.getMessage().getMentionedUsers().get(i) != event.getJDA().getSelfUser()) {
                    Member member = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(i));
                    if (member.getRoles().stream().map(net.dv8tion.jda.core.entities.Role::getPositionRaw).max(Integer::compareTo).get() >= event.getMember().getRoles().stream().map(Role::getPositionRaw).max(Integer::compareTo).get()) {
                        failed.add(event.getMessage().getMentionedUsers().get(i).getName());
                    } else {
                        try {
                            event.getGuild().getController().ban(event.getMessage().getMentionedUsers().get(i), 0).reason("Mass Banned by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                            users.add(event.getMessage().getMentionedUsers().get(i).getName());
                        } catch (PermissionException e) {
                            failed.add(event.getMessage().getMentionedUsers().get(i).getName());
                        }
                    }
                }
            }
            if (users.size() >= 1) {
                event.getChannel().sendMessage(KekBot.respond(Action.BAN_SUCCESS, "`" + StringUtils.join(users, ", ") + "`")).queue();
                if (failed.size() > 0) {
                    event.getChannel().sendMessage("However, " + failed.size() + (failed.size() == 1 ? " user" : " users") + " (`" + StringUtils.join(failed, ", ") + "`) couldn't be banned due to having a higher rank than I do. ¯\\_(ツ)_/¯").queue();
                }
            } else {
                if (failed.size() >= 1) {
                    event.getChannel().sendMessage("All of the users you have specified couldn't be banned due to having a higher rank than I do. ¯\\_(ツ)_/¯").queue();
                }
            }
        }
    }
}
