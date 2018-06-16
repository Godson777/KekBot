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
                                    event.getChannel().sendMessage("You're not allowed to make a tag of that name.").queue();
                                    return;
                                }
                            }
                            if (settings.getTags().getTagByName(event.getArgs()[1]).isPresent()) {
                                event.getChannel().sendMessage("A tag already exists with that name!").queue();
                                return;
                            }
                            if (event.getArgs().length > 2) {
                                addTag(settings, event, event.getArgs()[1], event.combineArgs(2));
                            } else addTagContents(settings, event, event.getArgs()[1]);
                        } else addTagName(settings, event);
                    }
                    break;
                case "remove":
                    if (settings.getTags().hasNoTags())
                        event.getChannel().sendMessage("This server doesn't seem to have any tags...").queue();
                    else {
                        if (event.getArgs().length > 1) {
                            removeTag(settings, event, event.getArgs()[1]);
                        } else removeTagName(settings, event);
                    }
                    break;
                case "list":
                    if (settings.getTags().hasNoTags())
                        event.getChannel().sendMessage("This server doesn't seem to have any tags...").queue();
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
                    if (settings.getTags().hasNoTags())
                        event.getChannel().sendMessage("This server doesn't seem to have any tags...").queue();
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
                    else event.getChannel().sendMessage("No such tag exists!").queue();
                    break;
            }
        } else event.getChannel().sendMessage("Not enough parameters. Check " + prefix + "help for usage on this command!").queue();
    }

    private void addTagName(Settings settings, CommandEvent event) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion("Enter the name of your tag.", QuestionType.STRING)
                .execute(results -> {
                    String name = results.getAnswer(0).toString();
                    String[] invalidWords = {"add", "remove", "list", "info", "list"};
                    for (String word : invalidWords) {
                        if (name.equalsIgnoreCase(word)) {
                            event.getChannel().sendMessage("You're not allowed to make a tag of that name. Please pick a different name.").queue();
                            results.reExecuteWithoutMessage();
                            return;
                        }
                    }
                    if (!settings.getTags().getTagByName(name).isPresent()) addTagContents(settings, event, name);
                    else {
                        event.getChannel().sendMessage("A tag already exists with that name. Please pick a different name.").queue();
                        results.reExecuteWithoutMessage();
                    }
                });
    }

    private void addTagContents(Settings settings, CommandEvent event, String name) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion("Enter the value of `" + name + "`.", QuestionType.STRING)
                .execute(results -> addTag(settings, event, name, results.getAnswer(0).toString()));
    }

    private void addTag(Settings settings, CommandEvent event, String name, String value) {
        Date creation = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM dd, hh:mma ('EST')");
        format.setTimeZone(TimeZone.getTimeZone("EST"));
        Tag tag = new Tag(name).setContents(value).setCreator(event.getAuthor()).setTime(format.format(creation));
        settings.getTags().addTag(tag);
        settings.save();
        event.getChannel().sendMessage("Successfully added tag! :thumbsup:").queue();
    }

    private void removeTagName(Settings settings, CommandEvent event) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion("Enter the name of the tag you wish to remove.", QuestionType.STRING)
                .execute(results -> removeTag(settings, event, results.getAnswer(0).toString()));
    }

    private void removeTag(Settings settings, CommandEvent event, String name) {
        Optional<Tag> tag = settings.getTags().getTagByName(name);
        if (tag.isPresent()) {
            if (tag.get().getCreatorID().equals(event.getAuthor().getId()) || event.getMember().hasPermission(event.getTextChannel(), Permission.ADMINISTRATOR)) {
                settings.getTags().removeTag(tag.get());
                settings.save();
                event.getChannel().sendMessage("Successfully removed tag \"" + name + "\".").queue();
            } else event.getChannel().sendMessage("You can't delete tags that don't belong to you!").queue();
        } else event.getChannel().sendMessage("No such tag exists!").queue();
    }

    private void getTagInfoByName(Settings settings, CommandEvent event) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion("Enter the name of the tag you wish to get info on.", QuestionType.STRING)
                .execute(results -> getTagInfo(settings, event, results.getAnswer(0).toString()));
    }

    private void getTagInfo(Settings settings, CommandEvent event, String name) {
        Optional<Tag> tag = settings.getTags().getTagByName(name);
        if (tag.isPresent()) {
            event.getChannel().sendMessage("Creator: " + event.getJDA().getUserById(tag.get().getCreatorID()).getName() +
                    "\nCreated at: " + tag.get().getTimeCreated() + (tag.get().getTimeLastEdited() != null ? "\nEdited At: " + tag.get().getTimeLastEdited() : "")).queue();
        } else event.getChannel().sendMessage("No such tag exists!").queue();
    }

    private void editTagName(Settings settings, CommandEvent event) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion("Enter the name of the tag you wish to edit.", QuestionType.STRING)
                .execute(results -> {
                    Optional<Tag> tag = settings.getTags().getTagByName(results.getAnswer(0).toString());
                    if (tag.isPresent()) {
                        if (tag.get().getCreatorID().equals(event.getAuthor().getId()) || event.getMember().hasPermission(event.getTextChannel(), Permission.ADMINISTRATOR)) editTagContents(settings, event, results.getAnswer(0).toString());
                        else event.getChannel().sendMessage("You can't edit a tag that you didn't create! Operation Canceled.").queue();
                    } else event.getChannel().sendMessage("No such tag exists. Operation Canceled.").queue();
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
            } else event.getChannel().sendMessage("You can't edit a tag that you didn't create!").queue();
        } else event.getChannel().sendMessage("No tag exists with that name.").queue();
    }
}
