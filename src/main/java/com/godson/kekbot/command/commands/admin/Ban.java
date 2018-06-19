package com.godson.kekbot.command.commands.admin;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.LocaleUtils;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
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
            event.getChannel().sendMessage(KekBot.respond(Action.BAN_EMPTY, event.getLocale())).queue();
            return;
        }

        if (event.getMentionedUsers().size() == 0) {
            event.getChannel().sendMessage(LocaleUtils.getString("command.admin.ban.nomention", event.getLocale())).queue();
        } else if (event.getMentionedUsers().size() == 1) {
            if (event.getMentionedUsers().get(0) == event.getJDA().getSelfUser()) {
                event.getChannel().sendMessage(LocaleUtils.getString("command.admin.ban.banself", event.getLocale())).queue();
            } else if (event.getMentionedUsers().get(0).equals(event.getMessage().getAuthor())) {
                event.getChannel().sendMessage(LocaleUtils.getString("command.admin.ban.banauthor", event.getLocale())).queue();
            } else {
                if (Utils.checkHierarchy(event.getGuild().getMember(event.getMentionedUsers().get(0)), event.getMember())) {
                    event.getChannel().sendMessage(LocaleUtils.getString("command.admin.ban.hierarchyusererror", event.getLocale())).queue();
                    return;
                }

                try {
                    event.getGuild().getController().ban(event.getMentionedUsers().get(0), 0).reason("Banned by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                    event.getChannel().sendMessage(KekBot.respond(Action.BAN_SUCCESS, event.getLocale(), "`" + event.getMentionedUsers().get(0).getName() + "`")).queue();
                } catch (PermissionException e) {
                    event.getChannel().sendMessage(LocaleUtils.getString("command.admin.ban.hierarchyboterror", event.getLocale())).queue();
                }
            }
        } else {
            List<String> users = new ArrayList<>();
            List<String> failed = new ArrayList<>();
            for (int i = 0; i < event.getMentionedUsers().size(); i++) {
                if (event.getMentionedUsers().get(i) != event.getJDA().getSelfUser()) {
                    User user = event.getMentionedUsers().get(i);
                    Member member = event.getGuild().getMember(user);
                    if (Utils.checkHierarchy(member, event.getMember())) {
                        failed.add(event.getMentionedUsers().get(i).getName());
                    } else {
                        try {
                            event.getGuild().getController().ban(event.getMentionedUsers().get(i), 0).reason("Mass Banned by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                            users.add(event.getMentionedUsers().get(i).getName());
                        } catch (PermissionException e) {
                            failed.add(event.getMentionedUsers().get(i).getName());
                        }
                    }
                }
            }
            if (users.size() >= 1) {
                event.getChannel().sendMessage(KekBot.respond(Action.BAN_SUCCESS, event.getLocale(), "`" + StringUtils.join(users, ", ") + "`")).queue();
                if (failed.size() > 0) {//   `" + StringUtils.join(failed, ", ") + "`
                    event.getChannel().sendMessage(LocaleUtils.getString("command.admin.ban.massban.exceptions", event.getLocale(),
                            failed.size() == 1 ? LocaleUtils.getString("amount.users.single", event.getLocale()) : LocaleUtils.getString("amount.users.plural", event.getLocale()),
                                    "`" + StringUtils.join(failed, ", ") + "`")).queue();
                }
            } else {
                if (failed.size() >= 1) {
                    event.getChannel().sendMessage(LocaleUtils.getString("command.admin.ban.massban.fail", event.getLocale())).queue();
                }
            }
        }
    }
}
