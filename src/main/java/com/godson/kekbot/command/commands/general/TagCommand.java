package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.settings.Settings;
import com.godson.kekbot.settings.Tag;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.core.Permission;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class TagCommand extends Command {

    public TagCommand() {
        name = "tag";
        aliases = new String[]{"t"};
        description = "Allows you add, remove, list, and get info on tags, which will send a message based on what's stored on it.";
        usage.add("tag <tag name>");
        usage.add("tag add <name> <contents>");
        usage.add("tag remove <name>");
        usage.add("tag edit <name> <contents>");
        usage.add("tag list");
        category = new Category("General");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        String prefix = event.getPrefix();
        if (event.getArgs().length > 0) {
            Settings settings = Settings.getSettings(event.getGuild());
            switch (event.getArgs()[0].toLowerCase()) {
                case "add":
                    if (event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE)) {
                        if (event.getArgs().length > 1) {
                            String[] invalidWords = {"add", "remove", "list", "info", "list"};
                            for (String word : invalidWords) {
                                if (event.getArgs()[1].equalsIgnoreCase(word)) {
                                    event.getChannel().sendMessage(event.getString("command.general.tag.add.invalidname")).queue();
                                    return;
                                }
                            }
                            if (settings.getTags().getTagByName(event.getArgs()[1]).isPresent()) {
                                event.getChannel().sendMessage(event.getString("command.general.tag.add.exists")).queue();
                                return;
                            }
                            if (event.getArgs().length > 2) {
                                addTag(settings, event, event.getArgs()[1], event.combineArgs(2));
                            } else addTagContents(settings, event, event.getArgs()[1]);
                        } else addTagName(settings, event);
                    }
                    break;
                case "remove":
                    if (settings.getTags() == null || settings.getTags().hasNoTags())
                        event.getChannel().sendMessage(event.getString("command.general.tag.notags")).queue();
                    else {
                        if (event.getArgs().length > 1) {
                            removeTag(settings, event, event.getArgs()[1]);
                        } else removeTagName(settings, event);
                    }
                    break;
                case "list":
                    if (settings.getTags() == null || settings.getTags().hasNoTags())
                        event.getChannel().sendMessage(event.getString("command.general.tag.notags")).queue();
                    else {
                        List<Tag> tags = settings.getTags().getList();
                        List<String> names = tags.stream().map(tag -> tag.getName() + " - Creator: " + (event.getGuild().getMemberById(tag.getCreatorID()) == null ? "Not in Server" : event.getGuild().getMemberById(tag.getCreatorID()).getUser().getName())).collect(Collectors.toList());
                        Paginator.Builder builder = new Paginator.Builder();
                        builder.setText("The tags for " + event.getGuild().getName() + " are:");
                        builder.addItems(names.toArray(new String[names.size()]));
                        builder.addUsers(event.getAuthor());
                        builder.setEventWaiter(KekBot.waiter);
                        builder.showPageNumbers(true);
                        builder.waitOnSinglePage(true);
                        builder.setColor(event.getMember().getColor());
                        builder.build().display(event.getChannel());
                    }
                    break;
                case "info":
                    if (settings.getTags() == null || settings.getTags().hasNoTags())
                        event.getChannel().sendMessage(event.getString("command.general.tag.notags")).queue();
                    else {
                        if (event.getArgs().length > 1) {
                            getTagInfo(settings, event, event.getArgs()[1]);
                        } else getTagInfoByName(settings, event);
                    }
                    break;
                case "edit":
                    if (event.getArgs().length > 1) {
                        if (event.getArgs().length > 2)
                            editTag(settings, event, event.getArgs()[1], event.combineArgs(2));
                        else editTagContents(settings, event, event.getArgs()[1]);
                    } else editTagName(settings, event);
                    break;
                default:
                    Optional<Tag> tag = settings.getTags().getTagByName(event.getArgs()[0]);
                    if (tag.isPresent()) event.getChannel().sendMessage(tag.get().getContents()).queue();
                    else event.getChannel().sendMessage(event.getString("command.general.tag.tagnotfound")).queue();
                    break;
            }
        } else event.getChannel().sendMessage(event.getString("command.noargs", "`" + prefix + "help tag" + "`")).queue();
    }

    private void addTagName(Settings settings, CommandEvent event) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion(event.getString("command.general.tag.add.noargs"), QuestionType.STRING)
                .execute(results -> {
                    String name = results.getAnswer(0).toString();
                    String[] invalidWords = {"add", "remove", "list", "info", "list"};
                    for (String word : invalidWords) {
                        if (name.equalsIgnoreCase(word)) {
                            event.getChannel().sendMessage(event.getString("command.general.tag.add.invalidname")).queue();
                            results.reExecuteWithoutMessage();
                            return;
                        }
                    }
                    if (!settings.getTags().getTagByName(name).isPresent()) addTagContents(settings, event, name);
                    else {
                        event.getChannel().sendMessage(event.getString("command.general.tag.add.exists")).queue();
                        results.reExecuteWithoutMessage();
                    }
                });
    }

    private void addTagContents(Settings settings, CommandEvent event, String name) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion(event.getString("command.general.tag.add.valuenoargs", "`" + name + "`"), QuestionType.STRING)
                .execute(results -> {
                    if (!results.getAnswer(0).equals("")) {
                        addTag(settings, event, name, results.getAnswer(0).toString());
                    }
                        event.getChannel().sendMessage("Attachments cannot be used in tags. Please try again.").queue();
                        results.reExecute();
                });
    }

    private void addTag(Settings settings, CommandEvent event, String name, String value) {
        Date creation = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM dd, hh:mma ('EST')");
        format.setTimeZone(TimeZone.getTimeZone("EST"));
        Tag tag = new Tag(name).setContents(value).setCreator(event.getAuthor()).setTime(format.format(creation));
        settings.getTags().addTag(tag);
        settings.save();
        event.getChannel().sendMessage(event.getString("command.general.tag.add.success")).queue();
    }

    private void removeTagName(Settings settings, CommandEvent event) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion(event.getString("command.general.tag.remove.noargs"), QuestionType.STRING)
                .execute(results -> removeTag(settings, event, results.getAnswer(0).toString()));
    }

    private void removeTag(Settings settings, CommandEvent event, String name) {
        Optional<Tag> tag = settings.getTags().getTagByName(name);
        if (tag.isPresent()) {
            if (tag.get().getCreatorID().equals(event.getAuthor().getId()) || event.getMember().hasPermission(event.getTextChannel(), Permission.ADMINISTRATOR)) {
                settings.getTags().removeTag(tag.get());
                settings.save();
                event.getChannel().sendMessage(event.getString("command.general.tag.remove.success", "`" + name + "`")).queue();
            } else event.getChannel().sendMessage(event.getString("command.general.tag.remove.noperms")).queue();
        } else event.getChannel().sendMessage(event.getString("command.general.tag.tagnotfound")).queue();
    }

    private void getTagInfoByName(Settings settings, CommandEvent event) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion(event.getString("command.general.tag.info.noargs"), QuestionType.STRING)
                .execute(results -> getTagInfo(settings, event, results.getAnswer(0).toString()));
    }

    private void getTagInfo(Settings settings, CommandEvent event, String name) {
        Optional<Tag> tag = settings.getTags().getTagByName(name);
        if (tag.isPresent()) {
            event.getChannel().sendMessage("Creator: " + event.getJDA().getUserById(tag.get().getCreatorID()).getName() +
                    "\nCreated at: " + tag.get().getTimeCreated() + (tag.get().getTimeLastEdited() != null ? "\nEdited At: " + tag.get().getTimeLastEdited() : "")).queue();
        } else event.getChannel().sendMessage(event.getString("command.general.tag.tagnotfound")).queue();
    }

    private void editTagName(Settings settings, CommandEvent event) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion(event.getString("command.general.tag.edit.noargs"), QuestionType.STRING)
                .execute(results -> {
                    Optional<Tag> tag = settings.getTags().getTagByName(results.getAnswer(0).toString());
                    if (tag.isPresent()) {
                        if (tag.get().getCreatorID().equals(event.getAuthor().getId()) || event.getMember().hasPermission(event.getTextChannel(), Permission.ADMINISTRATOR)) editTagContents(settings, event, results.getAnswer(0).toString());
                        else event.getChannel().sendMessage(event.getString("command.general.tag.edit.noperms")).queue();
                    } else event.getChannel().sendMessage(event.getString("command.general.tag.tagnotfound")).queue();
                });
    }

    private void editTagContents(Settings settings, CommandEvent event, String name) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion("Enter the new value of this tag.", QuestionType.STRING)
                .execute(results -> editTag(settings, event, name, results.getAnswer(0).toString()));
    }

    private void editTag(Settings settings, CommandEvent event, String name, String contents) {
        Date edit = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM dd, hh:mma ('EST')");
        Optional<Tag> tag = settings.getTags().getTagByName(name);
        if (tag.isPresent()) {
            if (tag.get().getCreatorID().equals(event.getAuthor().getId()) || event.getMember().hasPermission(event.getTextChannel(), Permission.ADMINISTRATOR)) {
                settings.getTags().editTag(tag.get(), contents, format.format(edit));
                settings.save();
            } else event.getChannel().sendMessage(event.getString("command.general.tag.edit.noperms")).queue();
        } else event.getChannel().sendMessage(event.getString("command.general.tag.tagnotfound")).queue();
    }
}
