package com.godson.kekbot.command.commands.admin;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.util.LocaleUtils;
import com.godson.kekbot.util.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Role extends Command {

    public Role() {
        name = "role";
        description = "Gives or takes a role from a user.";
        usage.add("role add <role name> | <@user> {@user}...");
        usage.add("role remove <role name> | <@user> {@user}...");
        category = new Category("Admin");
        requiredBotPerms = new Permission[]{Permission.MANAGE_ROLES};
        requiredUserPerms = new Permission[]{Permission.MANAGE_ROLES};
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage(event.getString("command.noargs", event.getPrefix() + "help")).queue();
            return;
        }

        switch (event.getArgs()[0].toLowerCase()) {
            case "add":
                if (event.getArgs().length < 1) {
                    event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.norole", event.getLocale())).queue();
                    return;
                }
                String[] args = event.combineArgs(1).split("\\u007c", 2);
                args[0] = Utils.removeWhitespaceEdges(args[0]);

                if (event.getGuild().getRolesByName(args[0], true).size() == 0) {
                    event.getChannel().sendMessage(LocaleUtils.getString("command.norolefound", event.getLocale(), args[0])).queue();
                    return;
                }

                if (args.length < 2) {
                    event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.add.nouser", event.getLocale())).queue();
                    return;
                }

                if (event.getMentionedUsers().size() == 0) {
                    event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.add.nomention", event.getLocale())).queue();
                } else if (event.getMentionedUsers().size() == 1) {
                    Member member = event.getGuild().getMember(event.getMentionedUsers().get(0));
                    net.dv8tion.jda.api.entities.Role role = event.getGuild().getRolesByName(args[0], true).get(0);
                    if (!member.getRoles().contains(role)) {
                        if (Utils.checkHierarchy(role, event.getMember())) {
                            if (Utils.checkHierarchy(role, event.getSelfMember())) {
                                event.getGuild().addRoleToMember(member, role).reason("Role Given by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                                event.getChannel().sendMessage(KekBot.respond(Action.ROLE_ADDED, event.getLocale(), event.getMentionedUsers().get(0).getName() + "#" + event.getMentionedUsers().get(0).getDiscriminator())).queue();
                            } else
                                event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.add.hierarchyboterror", event.getLocale())).queue();
                        } else
                            event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.add.hierarchyusererror", event.getLocale())).queue();
                    } else event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.add.exists", event.getLocale())).queue();
                } else {
                    List<User> users = event.getMentionedUsers();
                    net.dv8tion.jda.api.entities.Role role = event.getGuild().getRolesByName(args[0], true).get(0);
                    List<String> success = new ArrayList<String>();
                    List<String> exist = new ArrayList<String>();
                    for (User user : users) {
                        Member member = event.getGuild().getMember(user);
                        if (!member.getRoles().contains(role)) {
                            if (Utils.checkHierarchy(role, event.getMember())) {
                                if (Utils.checkHierarchy(role, event.getGuild().getSelfMember())) {
                                    event.getGuild().addRoleToMember(member, role).reason("Mass Role Given by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                                    success.add(user.getName() + "#" + user.getDiscriminator());
                                } else {
                                    event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.add.hierarchyboterror", event.getLocale())).queue();
                                    break;
                                }
                            } else {
                                event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.add.hierarchyusererror", event.getLocale())).queue();
                                break;
                            }
                        } else {
                            exist.add(user.getName() + "#" + user.getDiscriminator());
                        }
                    }
                    if (success.size() != 0) {
                        event.getChannel().sendMessage(KekBot.respond(Action.ROLE_ADDED, event.getLocale(), StringUtils.join(success, ", ")) +
                                (exist.size() != 0 ? LocaleUtils.getString("command.admin.role.add.mass.exceptions", event.getLocale(), exist.size() + (exist.size() == 1 ? "user" : "users"), StringUtils.join(exist, ", ")) : "")).queue();
                    } else {
                        event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.add.mass.fail", event.getLocale())).queue();
                    }
                }
                break;
            case "remove":
                if (event.getArgs().length < 1) {
                    event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.norole", event.getLocale())).queue();
                    return;
                }
                args = event.combineArgs(1).split("\\u007c", 2);
                args[0] = Utils.removeWhitespaceEdges(args[0]);

                if (event.getGuild().getRolesByName(args[0], true).size() == 0) {
                    event.getChannel().sendMessage(LocaleUtils.getString("command.norolefound", event.getLocale(), "`" + args[0] + "`")).queue();
                    return;
                }

                if (args.length < 2) {
                    event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.remove.nouser", event.getLocale())).queue();
                    return;
                }

                if (event.getMentionedUsers().size() == 0) {
                    event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.remove.nomention", event.getLocale())).queue();
                } else if (event.getMentionedUsers().size() == 1) {
                    Member member = event.getGuild().getMember(event.getMentionedUsers().get(0));
                    net.dv8tion.jda.api.entities.Role role = event.getGuild().getRolesByName(args[0], true).get(0);
                    if (member.getRoles().contains(role)) {
                        if (Utils.checkHierarchy(role, event.getMember())) {
                            if (Utils.checkHierarchy(role, event.getSelfMember())) {
                                event.getGuild().removeRoleFromMember(member, event.getGuild().getRolesByName(args[0], true).get(0)).reason("Role Removed by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                                event.getChannel().sendMessage(KekBot.respond(Action.ROLE_TAKEN, event.getLocale(), member.getUser().getName() + "#" + member.getUser().getDiscriminator())).queue();
                            } else
                                event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.remove.hierarchyboterror", event.getLocale())).queue();
                        } else
                            event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.remove.hierarchyusererror", event.getLocale())).queue();
                    } else event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.remove.exists", event.getLocale())).queue();
                } else {
                    List<User> users = event.getMentionedUsers();
                    net.dv8tion.jda.api.entities.Role role = event.getGuild().getRolesByName(args[0], true).get(0);
                    List<String> success = new ArrayList<String>();
                    List<String> exist = new ArrayList<String>();
                    //Iterate through every user pinged.
                    for (User user : users) {
                        Member member = event.getGuild().getMember(user);
                        //Check if user contains role
                        if (member.getRoles().contains(role)) {
                            //Check if the role is lower than the mod's.
                            if (Utils.checkHierarchy(role, event.getMember())) {
                                //Check if the role is lower than the bot's.
                                if (Utils.checkHierarchy(role, event.getGuild().getSelfMember())) {
                                    event.getGuild().removeRoleFromMember(member, role).reason("Mass Role Removed by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                                    success.add(user.getName() + "#" + user.getDiscriminator());
                                } else {
                                    event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.remove.hierarchyboterror", event.getLocale())).queue();
                                    break;
                                }
                            } else {
                                event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.remove.hierarchyusererror", event.getLocale())).queue();
                                break;
                            }
                        } else {
                            exist.add(user.getName() + "#" + user.getDiscriminator());
                        }
                    }

                    if (success.size() != 0) {
                        event.getChannel().sendMessage(KekBot.respond(Action.ROLE_TAKEN, event.getLocale(), StringUtils.join(success, ", ")) +
                                (exist.size() != 0 ? LocaleUtils.getString("command.admin.role.remove.mass.exceptions", event.getLocale(), exist.size() + (exist.size() == 1 ? "user" : "users"), StringUtils.join(exist, ", ")) : "")).queue();
                    } else {
                        event.getChannel().sendMessage(LocaleUtils.getString("command.admin.role.remove.mass.fail", event.getLocale())).queue();
                    }
                }
                break;
        }
    }
}
