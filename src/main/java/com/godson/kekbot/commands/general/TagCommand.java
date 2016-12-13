package com.godson.kekbot.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.Settings.Tag;
import com.godson.kekbot.Settings.TagManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TagCommand {
    public static Command tagCommand = new Command("tag")
            .withAliases("t")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Allows you to ADD, REMOVE, and LIST tags, you can also getResponder INFO on a tag. Which will send a message based on what's stored in the tag.")
            .withUsage("{p}tag {list|add|remove|list} <tag name>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 4);
                TextChannel channel = context.getTextChannel();
                String serverID = context.getGuild().getId();
                String prefix = CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(context.getGuild()) == null
                        ? CommandRegistry.getForClient(context.getJDA()).getPrefix()
                        : CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(context.getGuild());
                TagManager manager = GSONUtils.getTagManager(context.getGuild());
                if (rawSplit.length == 1) {
                    channel.sendMessage("Not enough parameters. Check " + prefix + "help for usage on this command!").queue();
                } else if (rawSplit.length >= 2) {
                    switch (rawSplit[1]) {
                        case "add":
                            if (context.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE)) {
                                if (rawSplit.length >= 3) {
                                    if (rawSplit.length == 4) {
                                        Date creation = Calendar.getInstance().getTime();
                                        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM dd, hh:mma ('EST')");
                                        Tag tag = new Tag(rawSplit[2]).setContents(rawSplit[3]).setCreator(context.getAuthor()).setTime(format.format(creation));
                                        try {
                                            manager.addTag(tag);
                                            manager.save(context.getGuild());
                                            channel.sendMessage("Successfully added tag! :thumbsup:").queue();
                                        } catch (IllegalArgumentException e) {
                                            channel.sendMessage("A tag already exists with that name!").queue();
                                        }
                                    } else {
                                        channel.sendMessage("No value specified for \"" + rawSplit[2] + "\"!").queue();
                                    }
                                } else {
                                    channel.sendMessage("```md" +
                                            "\n[Subcommand](tag add)" +
                                            "\n\n[Description](Adds a tag to this server.)" +
                                            "\n\n# Paramaters (<> Required, {} Optional)" +
                                            "\n[Usage](" + prefix + "tag add <name> <contents>```").queue();
                                }
                            }
                            break;
                        case "remove":
                            if (rawSplit.length == 2) {
                                channel.sendMessage("```md" +
                                        "\n[Subcommand](tag remove)" +
                                        "\n\n[Description](Removes a tag from this server, provided the tag is yours, or you have the \"Administrator\" permission.)" +
                                        "\n\n# Paramaters (<> Required, {} Optional)" +
                                        "\n[Usage](" + prefix + "tag remove <name>```").queue();
                            } else {
                                if (manager.hasNoTags()) channel.sendMessage("This server doesn't seem to have any tags...").queue();
                                else {
                                    Optional<Tag> tag = manager.getTagByName(rawSplit[2]);
                                    if (tag.isPresent()) {
                                        if (tag.get().getCreatorID().equals(context.getAuthor().getId()) || context.getMember().hasPermission(channel, Permission.ADMINISTRATOR)) {
                                            manager.removeTag(tag.get());
                                            manager.save(context.getGuild());
                                            channel.sendMessage("Successfully removed tag \"" + rawSplit[2] + "\".").queue();
                                        } else channel.sendMessage("You can't delete tags that don't belong to you!").queue();
                                    }
                                    else channel.sendMessage("No such tag exists!").queue();
                                }
                            }
                            break;
                        case "list":
                            if (manager.hasNoTags()) channel.sendMessage("This server doesn't seem to have any tags...").queue();
                            else {
                                List<Tag> tags = manager.getTags();
                                List<String> names = tags.stream().map(Tag::getName).collect(Collectors.toList());
                                channel.sendMessage("The tags for " + context.getGuild().getName() + " are: \n`" + StringUtils.join(names, ", ") + "`").queue();
                            }
                            break;
                        case "info":
                            if (rawSplit.length == 2) {
                                channel.sendMessage("```md" +
                                        "\n[Subcommand](tag info)" +
                                        "\n\n[Description](Gets information on a specified tag, if it exists.)" +
                                        "\n\n# Paramaters (<> Required, {} Optional)" +
                                        "\n[Usage](" + prefix + "tag info <name>```").queue();
                            } else {
                                if (manager.hasNoTags()) channel.sendMessage("This server doesn't seem to have any tags...").queue();
                                else {
                                    Optional<Tag> tag = manager.getTagByName(rawSplit[2]);
                                    if (tag.isPresent()) {
                                        channel.sendMessage("Creator: " + context.getJDA().getUserById(tag.get().getCreatorID()).getName() +
                                                "\nCreated at: " + tag.get().getTimeCreated() + (tag.get().getTimeLastEdited() != null ? "\nEdited At: " + tag.get().getTimeLastEdited() : "")).queue();
                                    } else channel.sendMessage("No such tag exists!").queue();
                                }
                            }
                            break;
                        case "edit":
                            if (context.getGuild().getSelfMember().hasPermission(channel, Permission.ADMINISTRATOR)) {
                                if (rawSplit.length >= 3) {
                                    if (rawSplit.length == 4) {
                                        Date edit = Calendar.getInstance().getTime();
                                        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM dd, hh:mma ('EST')");
                                        if (manager.getTagByName(rawSplit[2]).isPresent()) {
                                            manager.editTag(manager.getTagByName(rawSplit[2]).get(), rawSplit[3], format.format(edit));
                                        }
                                        manager.save(context.getGuild());
                                    } else {
                                        channel.sendMessage("No value specified for \"" + rawSplit[2] + "\"!").queue();
                                    }
                                } else {
                                    channel.sendMessage("```md" +
                                            "\n[Subcommand](tag edit)" +
                                            "\n\n[Description](Edits an existing tag in this server, provided the tag is yours, or you have the \"Administrator\" permission.)" +
                                            "\n\n# Paramaters (<> Required, {} Optional)" +
                                            "\n[Usage](" + prefix + "tag edit <name> <new contents>```").queue();
                                }
                            }
                            break;
                        default:
                            Optional<Tag> tag = manager.getTagByName(rawSplit[1]);
                            if (tag.isPresent()) channel.sendMessage(tag.get().getContents()).queue();
                            else channel.sendMessage("No such tag exists!").queue();
                            break;
                    }
                }
            });
}
