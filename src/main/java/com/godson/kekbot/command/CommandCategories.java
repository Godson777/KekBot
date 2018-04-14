package com.godson.kekbot.command;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;

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
                    event.getChannel().sendMessage(KekBot.respond(Action.MEME_NOT_APPLIED, "__**Living Meme**__")).queue();
                else {
                    if (event.getEvent().getMember().hasPermission(Permission.MANAGE_ROLES)) {
                        Questionnaire.newQuestionnaire(event).addYesNoQuestion(KekBot.respond(Action.MEME_NOT_APPLIED, "__**Living Meme**__") +
                                " Although, I do see it already added in this server. If you'd like, I could put it on myself. (Y/N)")
                                .execute(results -> {
                                    if (results.getAnswer(0).equals(true)) {
                                        event.getGuild().getController().addRolesToMember(event.getGuild().getSelfMember(), meme).queue();
                                        event.getChannel().sendMessage("Done. Meme commands should now work unless another admin takes away the role.").queue();
                                    } else {
                                        event.getChannel().sendMessage("Cancelled.").queue();
                                    }
                                });
                    } else {
                        event.getChannel().sendMessage(KekBot.respond(Action.MEME_NOT_APPLIED, "__**Living Meme**__")).queue();
                    }
                }
                return false;
                }
        }
        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES))
            event.getChannel().sendMessage(KekBot.respond(Action.MEME_NOT_FOUND, "__**Living Meme**__")).queue();
        else {
            if (event.getEvent().getMember().hasPermission(Permission.MANAGE_ROLES)) {
                Questionnaire.newQuestionnaire(event).addYesNoQuestion(KekBot.respond(Action.MEME_NOT_FOUND, "__**Living Meme**__") +
                        " Although, I do have the perms to make it myself. If you'd like, I could create it and put it on myself. (Y/N)")
                        .execute(results -> {
                            if (results.getAnswer(0).equals(true)) {
                                event.getGuild().getController().createRole().setName("Living Meme").queue(role -> {
                                    event.getGuild().getController().addRolesToMember(event.getGuild().getSelfMember(), role).queue();
                                });
                                event.getChannel().sendMessage("Done. Meme commands should now work unless another admin takes away the role.").queue();
                            } else {
                                event.getChannel().sendMessage("Cancelled.").queue();
                            }
                        });
            } else event.getChannel().sendMessage(KekBot.respond(Action.MEME_NOT_FOUND, "__**Living Meme**__")).queue();
        }
        return false;
    });
}
