package com.godson.kekbot.command.commands.admin;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.util.LocaleUtils;
import com.godson.kekbot.TriConsumer;
import com.godson.kekbot.util.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.settings.Settings;
import com.jagrosh.jdautilities.menu.Paginator;
import com.jagrosh.jdautilities.menu.SelectionDialog;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SettingsCommand extends Command {

    private Map<String, Setting> settings = new HashMap<>();

    public SettingsCommand() {
        name = "settings";
        description = "Allows you to edit KekBot settings for your server.";
        usage.add("settings");
        usage.add("settings review");
        category = new Category("Admin");
        requiredUserPerms = new Permission[]{Permission.ADMINISTRATOR};

        settings.put("prefix", new Setting("settings.prefix.description",
                "settings.prefix.noargs",
                (event, settings, newPrefix) -> {
            String oldPrefix = event.getPrefix();
            if (newPrefix.equals(oldPrefix)) {
                event.getChannel().sendMessage(event.getString("settings.prefix.oldprefix")).queue();
                return;
            }

            if (newPrefix.length() > 5) {
                event.getChannel().sendMessage(event.getString("settings.prefix.charlimit", 5)).queue();
                return;
            }

            if (newPrefix.startsWith(" ")) newPrefix = newPrefix.replace(" ","");

            settings.setPrefix(newPrefix).save();
            event.getClient().setCustomPrefix(event.getGuild().getId(), newPrefix);
            event.getChannel().sendMessage(event.getString("settings.prefix.success", "`" + oldPrefix + "`", "`" + newPrefix + "`")).queue();
        }));
        settings.put("autorole", new Setting("settings.autorole.description",
                "settings.autorole.noargs",
                ((event, settings, role) -> {
                    if (role.equalsIgnoreCase("reset")) {
                        settings.setAutoRoleID(null).save();
                        event.getChannel().sendMessage(event.getString("settings.autorole.reset")).queue();
                        return;
                    }

                    List<Role> check = event.getGuild().getRolesByName(role, false);
                    if (check.size() == 0) {
                        event.getChannel().sendMessage(event.getString("command.norolefound", role)).queue();
                        return;
                    }
                        settings.setAutoRoleID(check.get(0).getId()).save();
                        event.getChannel().sendMessage(event.getString("settings.autorole.success", "`" + check.get(0).getName() + "`")).queue();
                }), "`reset`"));
        settings.put("getrole", new Setting("settings.getrole.description",
                "settings.getrole.noargs",
                ((event, settings, role) -> {
                    if (role.equalsIgnoreCase("list")) {
                        Paginator.Builder builder = new Paginator.Builder();
                        List<Role> roles = settings.getFreeRoles().stream().map(r -> event.getGuild().getRoleById(r)).collect(Collectors.toList());
                        roles.forEach(r -> builder.addItems(r.getName()));
                        builder.setEventWaiter(KekBot.waiter);
                        builder.setFinalAction(m -> m.clearReactions().queue());
                        builder.waitOnSinglePage(true);
                        builder.setUsers(event.getAuthor());
                        builder.setText("");
                        builder.useNumberedItems(true);
                        builder.build().display(event.getChannel());
                        return;
                    }

                    List<Role> check = event.getGuild().getRolesByName(role, false);
                    if (check.size() == 0) {
                        event.getChannel().sendMessage(event.getString("command.norolefound", role)).queue();
                        return;
                    }

                    if (settings.getFreeRoles().contains(check.get(0).getId())) {
                        settings.removeFreeRole(check.get(0).getId()).save();
                        event.getChannel().sendMessage(event.getString("settings.getrole.removed")).queue();
                        return;
                    }

                    settings.addFreeRole(check.get(0).getId()).save();
                    event.getChannel().sendMessage(event.getString("settings.getrole.added")).queue();
                }), "`list`"));
        settings.put("welcomechannel", new Setting("settings.welcomechannel.description",
                "settings.welcomechannel.noargs",
                (event, settings, channel) -> {
                    if (channel.equalsIgnoreCase("reset")) {
                        settings.getAnnounceSettings().setWelcomeChannel(null);
                        settings.save();
                        event.getChannel().sendMessage(event.getString("settings.welcomechannel.reset")).queue();
                        return;
                    }

                    TextChannel wChannel;
                    try {
                        wChannel = Utils.resolveChannelMention(event.getGuild(), channel);
                    } catch (IllegalArgumentException e) {
                        event.getChannel().sendMessage(event.getString("settings.welcomechannel.invalidchannel")).queue();
                        return;
                    }

                    if (wChannel == null) {
                        event.getChannel().sendMessage(event.getString("settings.welcomechannel.notachannel")).queue();
                        return;
                    }

                    settings.getAnnounceSettings().setWelcomeChannel(wChannel);
                    settings.save();
                    event.getChannel().sendMessage(event.getString("settings.welcomechannel.success", wChannel.getAsMention())).queue();
                }, "`reset`"));
        settings.put("welcomemessage", new Setting("settings.welcomemessage.description",
                "settings.welcomemessage.noargs",
                (event, settings, message) -> {
                    if (message.equalsIgnoreCase("reset")) {
                        settings.getAnnounceSettings().setWelcomeMessage(null);
                        settings.save();
                        event.getChannel().sendMessage(event.getString("settings.welcomemessage.reset")).queue();
                        return;
                    }

                    settings.getAnnounceSettings().setWelcomeMessage(message);
                    settings.save();
                    event.getChannel().sendMessage(event.getString("settings.welcomemessage.success")).queue();
                }, "`reset`"));
        settings.put("farewellmessage", new Setting("settings.farewellmessage.description",
                "settings.farewellmessage.noargs",
                (event, settings, message) -> {
                    if (message.equalsIgnoreCase("reset")) {
                        settings.getAnnounceSettings().setFarewellMessage(null);
                        settings.save();
                        event.getChannel().sendMessage(event.getString("settings.farewellmessage.reset")).queue();
                        return;
                    }

                    settings.getAnnounceSettings().setFarewellMessage(message);
                    settings.save();
                    event.getChannel().sendMessage(event.getString("settings.welcomemessage.success")).queue();
                }, "`reset`"));
        settings.put("antiad", new Setting("settings.antiad.description",
                "settings.antiad.noargs",
                (event, settings, state) -> {
                    if (state.equalsIgnoreCase("on")) {
                        settings.setAntiAd(true).save();
                        event.getChannel().sendMessage(event.getString("settings.antiad.on")).queue();
                        return;
                    }

                    if (state.equalsIgnoreCase("off")) {
                        settings.setAntiAd(false).save();
                        event.getChannel().sendMessage(event.getString("settings.antiad.off")).queue();
                        return;
                    }

                    event.getChannel().sendMessage(event.getString("settings.antiad.invalid")).queue();
                }, "`on`", "`off`"));
        settings.put("language", new Setting("settings.language.description", null,
                (event, settings, h) -> {
                    SelectionDialog.Builder builder = new SelectionDialog.Builder();
                    LocaleUtils.languages.forEach(language -> builder.addChoices(language.getLeft()));
                    builder.setEventWaiter(KekBot.waiter);
                    builder.setUsers(event.getAuthor());
                    builder.useLooping(true);
                    builder.setSelectionConsumer((m, i) -> {
                        settings.setLocale(LocaleUtils.languages.get(i-1).getRight());
                        event.getClient().setCustomLocale(event.getGuild().getId(), settings.getLocale());
                        settings.save();
                        event.getChannel().sendMessage(LocaleUtils.getString("settings.language.set", settings.getLocale(), LocaleUtils.languages.get(i-1).getLeft())).queue();
                        m.clearReactions().queue();
                    });
                    builder.setCanceled(m -> m.clearReactions().queue());
                    builder.setDefaultEnds("\u23F9", "");
                    builder.setSelectedEnds("➡", "");
                    builder.build().display(event.getChannel());
                }));
        settings.put("updates", new Setting("settings.updates.description", "settings.updates.noargs",
                ((event, settings, channel) -> {
                    if (channel.equalsIgnoreCase("reset")) {
                        settings.getAnnounceSettings().setWelcomeChannel(null);
                        settings.save();
                        event.getChannel().sendMessage(event.getString("settings.updates.reset")).queue();
                        return;
                    }

                    TextChannel wChannel;
                    try {
                        wChannel = Utils.resolveChannelMention(event.getGuild(), channel);
                    } catch (IllegalArgumentException e) {
                        event.getChannel().sendMessage(event.getString("settings.welcomechannel.invalidchannel")).queue();
                        return;
                    }

                    if (wChannel == null) {
                        event.getChannel().sendMessage(event.getString("settings.welcomechannel.notachannel")).queue();
                        return;
                    }

                    settings.setUpdateChannel(wChannel);
                    settings.save();
                    event.getChannel().sendMessage(event.getString("settings.updates.success", wChannel.getAsMention())).queue();
                }), "`reset`"));
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        if (event.getArgs().length < 1) {
            final String[] missingArg = {event.getString("command.admin.settings.noargs") + "\n\n"};

            settings.forEach((s, setting) -> missingArg[0] += "`" + s + "` - " + event.getString(setting.description) + "\n");

            Questionnaire.newQuestionnaire(event).withTimeout(1, TimeUnit.MINUTES).addQuestion(missingArg[0], QuestionType.STRING).execute(r -> editSetting(event, r.getAnswerAsType(0, String.class), Optional.of(r)));
        } else {
            if (event.getArgs()[0].equalsIgnoreCase("review")) {
                Settings settings = Settings.getSettings(event.getGuild());
                Role autoRole = null;
                TextChannel welcomeChannel = null;
                if (settings.getAutoRoleID() != null) autoRole = event.getGuild().getRoleById(settings.getAutoRoleID());
                if (autoRole == null && settings.getAutoRoleID() != null) settings.setAutoRoleID(null).save();
                if (settings.getAnnounceSettings().getWelcomeChannelID() != null) welcomeChannel = event.getGuild().getTextChannelById(settings.getAnnounceSettings().getWelcomeChannelID());
                if (welcomeChannel == null && settings.getAnnounceSettings().getWelcomeChannelID() != null) {
                    settings.getAnnounceSettings().setWelcomeChannel(null);
                    settings.save();
                }
                String welcomeMessage = settings.getAnnounceSettings().getWelcomeMessage();
                String farewellMessage = settings.getAnnounceSettings().getFarewellMessage();
                event.getChannel().sendMessage("`prefix` - " + event.getPrefix() + "\n" +
                        "`autorole` - " + (autoRole == null ? event.getString("command.admin.settings.norole") : autoRole.getName()) + "\n" +
                        "`welcomechannel` - " + (welcomeChannel == null ? event.getString("command.admin.settings.nochannel") : welcomeChannel.getAsMention()) + "\n" +
                        "`welcomemessage` - " + (welcomeMessage == null ? event.getString("command.admin.settings.nomessage") : "`" + welcomeMessage + "`") + "\n" +
                        "`farewellmessage` - " + (farewellMessage == null ? event.getString("command.admin.settings.nomessage") : "`" + farewellMessage + "`") + "\n" +
                        "`antiad` - " + (settings.isAntiAdEnabled() ? event.getString("command.admin.settings.on") : event.getString("command.admin.settings.off"))).queue();
                return;
            }
            editSetting(event, event.getArgs()[0], Optional.empty());
        }
    }

    private void editSetting(CommandEvent event, String value, Optional<Questionnaire.Results> questionnaire) {
        if (settings.containsKey(value)) {
            Setting setting = settings.get(value);
            if (event.getArgs().length < 2) {
                if (setting.missingArgMessage == null) {
                    setting.action.accept(event, Settings.getSettings(event.getGuild()), null);
                    return;
                }

                Questionnaire.newQuestionnaire(event).withTimeout(1, TimeUnit.MINUTES).useRawInput()
                        .addQuestion(event.getString(setting.missingArgMessage, (Object[]) setting.substitutes), QuestionType.STRING)
                        .execute(r -> setting.action.accept(event, Settings.getSettings(event.getGuild()), r.getAnswerAsType(0, String.class)));
            } else {
                setting.action.accept(event, Settings.getSettings(event.getGuild()), event.combineArgs(1));
            }
        } else questionnaire.ifPresent(Questionnaire.Results::reExecuteWithoutMessage);
    }

    private static class Setting {
        private String description;
        private TriConsumer<CommandEvent, Settings, String> action;
        private String missingArgMessage;
        private String[] substitutes;

        Setting(String description, String missingArgMessage, TriConsumer<CommandEvent, Settings, String> action, String... substitutes) {
            this.description = description;
            this.missingArgMessage = missingArgMessage;
            this.action = action;
            this.substitutes = substitutes;
        }
    }
}
