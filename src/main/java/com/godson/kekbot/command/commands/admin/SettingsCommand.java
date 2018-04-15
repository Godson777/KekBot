package com.godson.kekbot.command.commands.admin;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.TriConsumer;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.settings.Settings;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.tuple.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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

        settings.put("prefix", new Setting("Prefix used to call commands",
                "Enter the prefix you'd like to use:",
                (event, settings, newPrefix) -> {
            String oldPrefix = event.getPrefix();
            if (newPrefix.equals(oldPrefix)) {
                event.getChannel().sendMessage("This is already the currently set prefix.").queue();
                return;
            }

            if (newPrefix.length() > 5) {
                event.getChannel().sendMessage("For your convenience, and due to limitations, I cannot allow you to set prefixes more than __**5**__ characters long.").queue();
                return;
            }

            if (newPrefix.startsWith(" ")) newPrefix = newPrefix.replace(" ","");

            settings.setPrefix(newPrefix).save();
            event.getClient().setCustomPrefix(event.getGuild().getId(), newPrefix);
            event.getChannel().sendMessage("Successfully changed prefix from `" + oldPrefix + "` " + "to `" + newPrefix + "`").queue();
        }));
        settings.put("autorole", new Setting("Set a specific role to be applied to newcoming members.",
                "Enter the name of the role you'd like users to be given (or say `reset` to stop giving users a role on join):",
                ((event, settings, role) -> {
                    if (role.equalsIgnoreCase("reset")) {
                        settings.setAutoRoleID(null).save();
                        event.getChannel().sendMessage("Done, I won't give users a role upon join.").queue();
                        return;
                    }

                    List<Role> check = event.getGuild().getRolesByName(role, false);
                    if (check.size() == 0) {
                        event.getChannel().sendMessage("Unable to find any roles by the name of `" + role + "`!").queue();
                        return;
                    }
                        settings.setAutoRoleID(check.get(0).getId()).save();
                        event.getChannel().sendMessage("Got it! I will now give newcomers the role \"" + check.get(0).getName() + "\"!").queue();
                })));
        settings.put("getrole", new Setting("Add or remove a role that users can equip with the `getrole` command.",
                "Enter the name of the role you want to add or remove from the list: (Or say `list` to list the roles available to users.)",
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
                        event.getChannel().sendMessage("Unable to find any roles by the name of `" + role + "`!").queue();
                        return;
                    }

                    if (settings.getFreeRoles().contains(check.get(0).getId())) {
                        settings.removeFreeRole(check.get(0).getId()).save();
                        event.getChannel().sendMessage("This role has been removed from the list of free roles.").queue();
                        return;
                    }

                    settings.addFreeRole(check.get(0).getId()).save();
                    event.getChannel().sendMessage("This role has been added to the list of free roles.").queue();
                })));
        settings.put("welcomechannel", new Setting("Sets the channel where welcome/farewell messages will be sent.",
                "Mention the channel (`#channel`) you'd like welcome/farewell messages to be sent to (or say `reset` to stop sending welcome/farewell messages to a previously selected channel):",
                (event, settings, channel) -> {
                    if (channel.equalsIgnoreCase("reset")) {
                        settings.getAnnounceSettings().setWelcomeChannel(null);
                        settings.save();
                        event.getChannel().sendMessage("Successfully reset the welcome channel, welcome/farewell messages will no longer be sent.").queue();
                        return;
                    }

                    TextChannel wChannel;
                    try {
                        wChannel = Utils.resolveChannelMention(event.getGuild(), channel);
                    } catch (IllegalArgumentException e) {
                        event.getChannel().sendMessage("Huh? That doesn't look like a channel.").queue();
                        return;
                    }

                    if (wChannel == null) {
                        event.getChannel().sendMessage("Alright there, you and I both know that's not an actual channel. Get outta here.").queue();
                        return;
                    }

                    settings.getAnnounceSettings().setWelcomeChannel(wChannel);
                    settings.save();
                    event.getChannel().sendMessage("Successfully set " + wChannel.getAsMention() + " as the welcome/farewell channel.").queue();
                }));
        settings.put("welcomemessage", new Setting("Sets the welcome message.",
                "Type the message you want me to use to welcome newcomers. *{mention} gets replaced with `@Example User`, while {name} gets replaced with `Example User`* (or say `reset` to stop sending welcome messages):",
                (event, settings, message) -> {
                    if (message.equalsIgnoreCase("reset")) {
                        settings.getAnnounceSettings().setWelcomeMessage(null);
                        settings.save();
                        event.getChannel().sendMessage("Successfully reset the welcome message, welcome messages will no longer be sent.").queue();
                        return;
                    }

                    settings.getAnnounceSettings().setWelcomeMessage(message);
                    settings.save();
                    event.getChannel().sendMessage("Message saved.").queue();
                }));
        settings.put("farewellmessage", new Setting("Sets the farewell message.",
                "Type the message you want me to use to say goodbye. *{mention} gets replaced with `@Example User`, while {name} gets replaced with `Example User`* (or say `reset` to stop sending farewell messages):",
                (event, settings, message) -> {
                    if (message.equalsIgnoreCase("reset")) {
                        settings.getAnnounceSettings().setFarewellMessage(null);
                        settings.save();
                        event.getChannel().sendMessage("Successfully reset the farewell message, farewell messages will no longer be sent.").queue();
                        return;
                    }

                    settings.getAnnounceSettings().setFarewellMessage(message);
                    settings.save();
                    event.getChannel().sendMessage("Message saved.").queue();
                }));
        settings.put("antiad", new Setting("Whether or not KekBot should delete discord invites posted by users. (Users with `Manage Messages` permisison bypass this.)",
                "Say `on` to turn on anti-ad, say `off` to turn it off:",
                (event, settings, state) -> {
                    if (state.equalsIgnoreCase("on")) {
                        settings.setAntiAd(true).save();
                        event.getChannel().sendMessage("Anti-Ad is now on.").queue();
                        return;
                    }

                    if (state.equalsIgnoreCase("off")) {
                        settings.setAntiAd(false).save();
                        event.getChannel().sendMessage("Anti-Ad is now off.").queue();
                        return;
                    }

                    event.getChannel().sendMessage("Not a valid option.").queue();
                }));
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        if (event.getArgs().length < 1) {
            final String[] missingArg = {"Here are the available settings you can edit: \n\n"};

            settings.forEach((s, setting) -> missingArg[0] += "`" + s + "` - " + setting.description + "\n");

            Questionnaire.newQuestionnaire(event).addQuestion(missingArg[0], QuestionType.STRING).execute(r -> editSetting(event, r.getAnswerAsType(0, String.class), Optional.of(r)));
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
                        "`autorole` - " + (autoRole == null ? "No role set." : autoRole.getName()) + "\n" +
                        "`welcomechannel` - " + (welcomeChannel == null ? "No channel set." : welcomeChannel.getAsMention()) + "\n" +
                        "`welcomemessage` - " + (welcomeMessage == null ? "No message set." : "`" + welcomeMessage + "`") + "\n" +
                        "`farewellmessage` - " + (farewellMessage == null ? "No message set." : "`" + farewellMessage + "`") + "\n" +
                        "`antiad` - " + (settings.isAntiAdEnabled() ? "On" : "Off")).queue();
                return;
            }
            editSetting(event, event.getArgs()[0], Optional.empty());
        }
    }

    private void editSetting(CommandEvent event, String value, Optional<Questionnaire.Results> questionnaire) {
        if (settings.containsKey(value)) {
            Setting setting = settings.get(value);
            if (event.getArgs().length < 2) {
                Questionnaire.newQuestionnaire(event).useRawInput()
                        .addQuestion(setting.missingArgMessage, QuestionType.STRING)
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

        Setting(String description, String missingArgMessage, TriConsumer<CommandEvent, Settings, String> action) {
            this.description = description;
            this.missingArgMessage = missingArgMessage;
            this.action = action;
        }
    }
}
