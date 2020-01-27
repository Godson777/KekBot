package com.godson.kekbot.command;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;

import java.util.List;

public class CommandCategories {

    public static Command.Category meme = new Command.Category("Memes", null, event -> {
        List<Role> memeCheck = event.getEvent().getGuild().getRolesByName("Living Meme", true);
        if (memeCheck.size() > 0) {
            Role meme = memeCheck.get(0);

            if (event.getEvent().getGuild().getSelfMember().getRoles().contains(meme))
                return true;
            else {
                if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES) &&
                        !(event.getEvent().getGuild().getSelfMember().getRoles().stream().map(Role::getPositionRaw).max(Integer::compareTo).get() >= meme.getPositionRaw()))
                    event.getChannel().sendMessage(KekBot.respond(Action.MEME_NOT_APPLIED, event.getLocale(),"__**Living Meme**__")).queue();
                else {
                    if (event.getEvent().getMember().hasPermission(Permission.MANAGE_ROLES)) {
                        Questionnaire.newQuestionnaire(event)
                                .addYesNoQuestion(KekBot.respond(Action.MEME_NOT_APPLIED, event.getLocale(), "__**Living Meme**__") +
                                event.getString("livingmeme.rolenotequipped"))
                                //Have an "interruption" checking if the user manually assigned the "Living Meme" role.
                                .withInterruption(e -> e instanceof GuildMemberRoleAddEvent && ((GuildMemberRoleAddEvent) e).getMember().equals(event.getSelfMember()) && ((GuildMemberRoleAddEvent) e).getRoles().contains(meme),
                                        e -> event.getChannel().sendMessage(event.getString("livingmeme.interrupt")).queue())
                                //Now do the thing with the thing.
                                .execute(results -> {
                                    if (results.getAnswer(0).equals(true)) {
                                        event.getGuild().addRoleToMember(event.getGuild().getSelfMember(), meme).queue();
                                        event.getChannel().sendMessage(event.getString("livingmeme.autoaddsuccess")).queue();
                                    } else {
                                        event.getChannel().sendMessage(event.getString("questionnaire.cancelled")).queue();
                                    }
                                });
                    } else {
                        event.getChannel().sendMessage(KekBot.respond(Action.MEME_NOT_APPLIED, event.getLocale(), "__**Living Meme**__")).queue();
                    }
                }
                return false;
                }
        }
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES))
            event.getChannel().sendMessage(KekBot.respond(Action.MEME_NOT_FOUND, event.getLocale(), "__**Living Meme**__")).queue();
        else {
            if (event.getEvent().getMember().hasPermission(Permission.MANAGE_ROLES)) {
                Questionnaire.newQuestionnaire(event).addYesNoQuestion(KekBot.respond(Action.MEME_NOT_FOUND, event.getLocale(), "__**Living Meme**__") +
                        event.getString("livingmeme.rolemissing"))
                        //Have an "interruption" checking if the user manually created the "Living Meme" role.
                        .withInterruption(e -> e instanceof RoleCreateEvent && ((RoleCreateEvent) e).getRole().getName().equals("Living Meme"),
                                e -> event.getChannel().sendMessage(event.getString("livingmeme.interrupt")).queue())
                        //Now let's do the thing with the thing.
                        .execute(results -> {
                            if (results.getAnswer(0).equals(true)) {
                                event.getGuild().createRole().setName("Living Meme").queue(role -> {
                                    event.getGuild().addRoleToMember(event.getGuild().getSelfMember(), role).queue();
                                });
                                event.getChannel().sendMessage(event.getString("livingmeme.autoaddsuccess")).queue();
                            } else {
                                event.getChannel().sendMessage(event.getString("questionnaire.cancelled")).queue();
                            }
                        });
            } else event.getChannel().sendMessage(KekBot.respond(Action.MEME_NOT_FOUND, event.getLocale(), "__**Living Meme**__")).queue();
        }
        return false;
    });
}
