package com.godson.kekbot.command.commands.admin;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.GuildController;
import org.apache.commons.lang3.StringUtils;

import javax.rmi.CORBA.Util;
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
        if (event.getArgs().length > 0) {
            switch (event.getArgs()[0].toLowerCase()) {
                case "add":
                    if (event.getArgs().length < 1) {
                        event.getChannel().sendMessage("No role specified.").queue();
                        return;
                    }
                    String[] args = event.combineArgs(1).split("\\u007c", 2);
                    args[0] = Utils.removeWhitespaceEdges(args[0]);

                    if (event.getGuild().getRolesByName(args[0], true).size() == 0) {
                        event.getChannel().sendMessage("Unable to find any roles by the name of \"" + args[0] + "\"!").queue();
                        return;
                    }

                    if (args.length < 2) {
                        event.getChannel().sendMessage("Who am I supposed to give this role to? :neutral_face:").queue();
                        return;
                    }

                    if (event.getEvent().getMessage().getMentionedUsers().size() == 0) {
                        event.getChannel().sendMessage("The user(s) you want to assign this role to __**must**__ be in the form of a mention!").queue();
                    } else if (event.getEvent().getMessage().getMentionedUsers().size() == 1) {
                        Member member = event.getGuild().getMember(event.getEvent().getMessage().getMentionedUsers().get(0));
                        if (!member.getRoles().contains(event.getGuild().getRolesByName(args[0], true).get(0))) {
                            if (member.getRoles().stream().map(net.dv8tion.jda.core.entities.Role::getPositionRaw).max(Integer::compareTo).get() >= event.getMember().getRoles().stream().map(net.dv8tion.jda.core.entities.Role::getPositionRaw).max(Integer::compareTo).get()) {
                                event.getChannel().sendMessage("You can't edit someone's roles when their highest role is the same as or is higher than yours.").queue();
                            } else {
                                try {
                                    event.getGuild().getController().addRolesToMember(member, event.getGuild().getRolesByName(args[0], true).get(0)).reason("Role Given by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                                    event.getChannel().sendMessage(KekBot.respond(Action.ROLE_ADDED, event.getEvent().getMessage().getMentionedUsers().get(0).getName() + "#" + event.getEvent().getMessage().getMentionedUsers().get(0).getDiscriminator())).queue();
                                } catch (PermissionException e) {
                                    event.getChannel().sendMessage("That role is higher than mine! I cannot assign it to any users!").queue();
                                }
                            }
                        } else {
                            event.getChannel().sendMessage("This user already has the role you specified!").queue();
                        }

                    } else {
                        List<User> users = event.getEvent().getMessage().getMentionedUsers();
                        GuildController controller = event.getGuild().getController();
                        net.dv8tion.jda.core.entities.Role role = event.getGuild().getRolesByName(args[0], true).get(0);
                        List<String> success = new ArrayList<String>();
                        List<String> exist = new ArrayList<String>();
                        boolean failed = false;
                        for (User user : users) {
                            Member member = event.getGuild().getMember(user);
                            if (member.getRoles().contains(role)) {
                                try {
                                    controller.addRolesToMember(member, role).reason("Mass Role Given by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                                    success.add(user.getName() + "#" + user.getDiscriminator());
                                } catch (PermissionException e) {
                                    failed = true;
                                    break;
                                }
                            } else {
                                exist.add(user.getName() + "#" + user.getDiscriminator());
                            }
                        }
                        if (failed) {
                            event.getChannel().sendMessage("That role is higher than mine! I cannot assign it to any users!").queue();
                        } else {
                            if (success.size() != 0) {
                                event.getChannel().sendMessage(KekBot.respond(Action.ROLE_ADDED, StringUtils.join(success, ", ")) +
                                        (exist.size() != 0 ? "\nHowever, " + exist.size() + (exist.size() == 1 ? "user" : "users") + ": `" + StringUtils.join(exist, ", ") + "` already have this role. So they were ignored." : "")).queue();
                            } else {
                                event.getChannel().sendMessage("All users you specified already have this role!").queue();
                            }
                        }
                    }
                    break;
                case "remove":
                    if (event.getArgs().length < 1) {
                        event.getChannel().sendMessage("No role specified.").queue();
                        return;
                    }
                    args = event.combineArgs(1).split("\\u007c", 2);
                    args[0] = Utils.removeWhitespaceEdges(args[0]);

                    if (event.getGuild().getRolesByName(args[0], true).size() == 0) {
                        event.getChannel().sendMessage("Unable to find any roles by the name of \"" + args[0] + "\"!").queue();
                        return;
                    }

                    if (args.length < 2) {
                        event.getChannel().sendMessage("Who am I supposed to give this role to? :neutral_face:").queue();
                        return;
                    }

                    if (event.getEvent().getMessage().getMentionedUsers().size() == 0) {
                        event.getChannel().sendMessage("The user(s) you want to remove this role from __**must**__ be in the form of a mention!").queue();
                    } else if (event.getEvent().getMessage().getMentionedUsers().size() == 1) {
                        Member member = event.getGuild().getMember(event.getEvent().getMessage().getMentionedUsers().get(0));
                        if (member.getRoles().contains(event.getGuild().getRolesByName(args[0], true).get(0))) {
                            if (member.getRoles().stream().map(net.dv8tion.jda.core.entities.Role::getPositionRaw).max(Integer::compareTo).get() >= event.getMember().getRoles().stream().map(net.dv8tion.jda.core.entities.Role::getPositionRaw).max(Integer::compareTo).get()) {
                                event.getChannel().sendMessage("You can't edit someone's roles when their highest role is the same as or is higher than yours.").queue();
                            } else {
                                try {
                                    event.getGuild().getController().removeRolesFromMember(member, event.getGuild().getRolesByName(args[0], true).get(0)).reason("Role Removed by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                                    event.getChannel().sendMessage(KekBot.respond(Action.ROLE_TAKEN, member.getUser().getName() + "#" + member.getUser().getDiscriminator())).queue();
                                } catch (PermissionException e) {
                                    event.getChannel().sendMessage("That role is higher than mine! I cannot remove it from any users!").queue();
                                }
                            }
                        } else {
                            event.getChannel().sendMessage("This user doesn't have the role you specified!").queue();
                        }
                    } else {
                        List<User> users = event.getEvent().getMessage().getMentionedUsers();
                        GuildController controller = event.getGuild().getController();
                        net.dv8tion.jda.core.entities.Role role = event.getGuild().getRolesByName(args[0], true).get(0);
                        List<String> success = new ArrayList<String>();
                        List<String> exist = new ArrayList<String>();
                        boolean failed = false;
                        for (User user : users) {
                            Member member = event.getGuild().getMember(user);
                            if (member.getRoles().contains(role)) {
                                try {
                                    controller.removeRolesFromMember(member, role).reason("Mass Role Removed by: " + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " (" + event.getAuthor().getId() + ")").queue();
                                    success.add(user.getName() + "#" + user.getDiscriminator());
                                } catch (PermissionException e) {
                                    failed = true;
                                    break;
                                }
                            } else {
                                exist.add(user.getName() + "#" + user.getDiscriminator());
                            }
                        }
                        if (failed) {
                            event.getChannel().sendMessage("That role is higher than mine! I cannot remove it from any users!").queue();
                        } else {
                            if (success.size() != 0) {
                                event.getChannel().sendMessage(KekBot.respond(Action.ROLE_TAKEN, StringUtils.join(success, ", ")) +
                                        (exist.size() != 0 ? "\nHowever, " + exist.size() + (exist.size() == 1 ? "user" : "users") + ": `" + StringUtils.join(exist, ", ") + "` don't have this role. So they were ignored." : "")).queue();
                            } else {
                                event.getChannel().sendMessage("All users you specified don't have this role!").queue();
                            }
                        }
                    }
                    break;
            }
        }
    }
}
