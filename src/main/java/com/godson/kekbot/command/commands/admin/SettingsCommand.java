package com.godson.kekbot.command.commands.admin;

import com.godson.kekbot.TriConsumer;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.settings.Settings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.utils.tuple.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SettingsCommand extends Command {

    private Map<String, Setting> settings = new HashMap<>();

    public SettingsCommand() {
        name = "settings";
        description = "Allows you to edit KekBot settings for your server.";
        usage.add("settings");
        category = new Category("Admin");
        requiredUserPerms = new Permission[]{Permission.ADMINISTRATOR};

        settings.put("prefix", new Setting("Prefix used to call commands", "Enter the prefix you'd like to use:", (event, settings, newPrefix) -> {
            String oldPrefix = event.getPrefix();
            if (newPrefix.equals(oldPrefix)) {
                event.getChannel().sendMessage("This is already the currently set prefix.").queue();
                return;
            }

            if (newPrefix.length() > 2) {
                event.getChannel().sendMessage("For your convenience, and due to limitations, I cannot allow you to set prefixes more than __**2**__ character long.").queue();
                return;
            }

            if (newPrefix.startsWith(" ")) newPrefix = newPrefix.replace(" ","");

            settings.setPrefix(newPrefix).save();
            event.getClient().setCustomPrefix(event.getGuild().getId(), newPrefix);
            event.getChannel().sendMessage("Successfully changed prefix from `" + oldPrefix + "` " + "to `" + newPrefix + "`").queue();
        }));
        //settings.put("", new Setting());
        //settings.put("", new Setting());
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length < 1) {
            final String[] missingArg = {"Here are the available settings you can edit: \n\n"};

            settings.forEach((s, setting) -> missingArg[0] += s + " - " + setting.description + "\n");

            Questionnaire.newQuestionnaire(event).addQuestion(missingArg[0], QuestionType.STRING).execute(r -> editSetting(event, r.getAnswerAsType(0, String.class), Optional.of(r)));
        } else {
            editSetting(event, event.getArgs()[0], Optional.empty());
        }
    }

    private void editSetting(CommandEvent event, String value, Optional<Questionnaire.Results> questionnaire) {
        if (settings.containsKey(value)) {
            Setting setting = settings.get(value);
            if (event.getArgs().length < 2) {
                Questionnaire.newQuestionnaire(event)
                        .addQuestion(setting.missingArgMessage, QuestionType.STRING)
                        .execute(r -> setting.action.accept(event, Settings.getSettings(event.getGuild()), r.getAnswerAsType(0, String.class)));
            } else {
                setting.action.accept(event, Settings.getSettings(event.getGuild()), event.getArgs()[1]);
            }
        } else questionnaire.ifPresent(Questionnaire.Results::reExecuteWithoutMessage);
    }

    private static class Setting {
        private String description;
        private TriConsumer<CommandEvent, Settings, String> action;
        private String missingArgMessage;

        Setting(String description, String missingArgMessage, TriConsumer<CommandEvent, Settings, String> action) {
            this.description = description;
            this.missingArgMessage = missingArgMessage;
            this.action = action;
        }
    }
}
