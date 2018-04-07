package com.godson.kekbot.command.commands.admin;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Kick extends Command {

    public Kick() {
        name = "kick";
        description = "Kicks a specified user or users.";
        usage.add("kick <@user> {@user}...");
        category = new Category("Admin");
        requiredBotPerms = new Permission[]{Permission.KICK_MEMBERS};
        requiredUserPerms = new Permission[]{Permission.KICK_MEMBERS};
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage(KekBot.respond(Action.KICK_EMPTY)).queue();
            return;
        }


        if (event.getMessage().getMentionedUsers().size() == 0) {
            event.getChannel().sendMessage(event.getMessage().getAuthor().getAsMention() + " The user you want to kick __**must**__ be in the form of a mention!").queue();
        } else if (event.getMessage().getMentionedUsers().size() == 1) {
            if (event.getMessage().getMentionedUsers().get(0) == event.getJDA().getSelfUser()) {
                event.getChannel().sendMessage("How would I kick myself? :thinking:").queue();
            } else if (event.getMessage().getMentionedUsers().get(0).equals(event.getMessage().getAuthor())) {
                event.getChannel().sendMessage("You can't kick yourself, it just doesn't work that way.").queue();
            } else {
                if (Utils.checkHierarchy(event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0)), event.getMember())) {
                    event.getChannel().sendMessage("You can't kick someone who's highest role is the same as or is higher than yours.").queue();
                    return;
                }

                try {
                    event.getGuild().getController().kick(event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0))).reason("Kicked by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                    event.getChannel().sendMessage(KekBot.respond(Action.KICK_SUCCESS, "`" + event.getMessage().getMentionedUsers().get(0).getName() + "`")).queue();
                } catch (PermissionException e) {
                    event.getChannel().sendMessage("`" + event.getMessage().getMentionedUsers().get(0).getName() + "`'s role is higher than mine. I am unable to kick them.").queue();
                }
            }
        } else {
            List<String> users = new ArrayList<>();
            List<String> failed = new ArrayList<>();
            for (int i = 0; i < event.getMessage().getMentionedUsers().size(); i++) {
                if (event.getMessage().getMentionedUsers().get(i) != event.getJDA().getSelfUser()) {
                    User user = event.getMessage().getMentionedUsers().get(i);
                    Member member = event.getGuild().getMember(user);
                    if (!Utils.checkHierarchy(member, event.getMember())) {
                        failed.add(event.getMessage().getMentionedUsers().get(i).getName());
                    } else {
                        try {
                            event.getGuild().getController().kick(member).reason("Mass Kicked by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                            users.add(user.getName());
                        } catch (PermissionException e) {
                            failed.add(user.getName());
                        }
                    }
                }
            }
            if (users.size() >= 1) {
                event.getChannel().sendMessage(KekBot.respond(Action.KICK_SUCCESS, "`" + StringUtils.join(users, ", ") + "`")).queue();
                if (failed.size() > 0) {
                    event.getChannel().sendMessage("However, " + failed.size() + (failed.size() == 1 ? " user" : " users") + "(`" + StringUtils.join(failed, ", ") + "`) couldn't be kicked due to having a higher rank than I do. ¯\\_(ツ)_/¯").queue();
                }
            } else {
                if (failed.size() >= 1) {
                    event.getChannel().sendMessage("All of the users you have specified could not be kicked due to having a higher rank than I do. ¯\\_(ツ)_/¯").queue();
                }
            }
        }
    }
}
