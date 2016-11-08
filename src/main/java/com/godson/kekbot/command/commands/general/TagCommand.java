package com.godson.kekbot.command.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.Settings.Tag;
import com.godson.kekbot.Settings.TagManager;
import com.godson.kekbot.XMLUtils;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class TagCommand {
    public static Command tagCommand = new Command("tag")
            .withAliases("t")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Allows you to ADD, REMOVE, and LIST tags, you can also get INFO on a tag. Which will send a message based on what's stored in the tag.")
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
                    channel.sendMessageAsync("Not enough parameters. Check " + prefix + "help for usage on this command!", null);
                } else if (rawSplit.length >= 2) {
                    switch (rawSplit[1]) {
                        case "add":
                            if (PermissionUtil.checkPermission(channel, context.getJDA().getSelfInfo(), Permission.MESSAGE_WRITE)) {
                                if (rawSplit.length >= 3) {
                                    if (rawSplit.length == 4) {
                                        Date creation = Calendar.getInstance().getTime();
                                        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM dd, hh:mma ('EST')");
                                        Tag tag = new Tag(rawSplit[2]).setContents(rawSplit[3]).setCreator(context.getAuthor()).setTime(format.format(creation));
                                        try {
                                            manager.addTag(tag);
                                            manager.save(context.getGuild());
                                        } catch (IllegalArgumentException e) {

                                        }
                                    } else {
                                        channel.sendMessageAsync("No value specified for \"" + rawSplit[2] + "\"!", null);
                                    }
                                } else {
                                    channel.sendMessageAsync("```md" +
                                            "\n[Subcommand](tag add)" +
                                            "\n\n[Description](Adds a tag to this server.)" +
                                            "\n\n# Paramaters (<> Required, {} Optional)" +
                                            "\n[Usage](" + prefix + "tag add <name> <contents>```", null);
                                }
                            }
                            break;
                        case "remove":
                            if (rawSplit.length == 2) {
                                channel.sendMessageAsync("```md" +
                                        "\n[Subcommand](tag remove)" +
                                        "\n\n[Description](Removes a tag from this server, provided the tag is yours, or you have the \"Administrator\" permission.)" +
                                        "\n\n# Paramaters (<> Required, {} Optional)" +
                                        "\n[Usage](" + prefix + "tag remove <name>```", null);
                            } else {
                                if (manager.hasNoTags()) channel.sendMessageAsync("This server doesn't seem to have any tags...", null);
                                else {
                                    Optional<Tag> tag = manager.getTagByName(rawSplit[2]);
                                    if (tag.isPresent()) {
                                        if (tag.get().getCreatorID().equals(context.getAuthor().getId()) || PermissionUtil.checkPermission(channel, context.getAuthor(), Permission.ADMINISTRATOR)) {
                                            manager.removeTag(tag.get());
                                            manager.save(context.getGuild());
                                            channel.sendMessageAsync("Successfully removed tag \"" + rawSplit[2] + "\".", null);
                                        } else channel.sendMessageAsync("You can't delete tags that don't belong to you!", null);
                                    }
                                    else channel.sendMessageAsync("No such tag exists!", null);
                                }
                            }
                            break;
                        case "list":
                            if (manager.hasNoTags()) channel.sendMessageAsync("This server doesn't seem to have any tags...", null);
                            else {
                                List<Tag> tags = manager.getTags();
                                List<String> names = tags.stream().map(Tag::getName).collect(Collectors.toList());
                                channel.sendMessageAsync("The tags for " + context.getGuild().getName() + " are: \n`" + StringUtils.join(names, ", ") + "`", null);
                            }
                            break;
                        case "info":
                            if (rawSplit.length == 2) {
                                channel.sendMessageAsync("```md" +
                                        "\n[Subcommand](tag info)" +
                                        "\n\n[Description](Gets information on a specified tag, if it exists.)" +
                                        "\n\n# Paramaters (<> Required, {} Optional)" +
                                        "\n[Usage](" + prefix + "tag info <name>```", null);
                            } else {
                                if (manager.hasNoTags()) channel.sendMessageAsync("This server doesn't seem to have any tags...", null);
                                else {
                                    Optional<Tag> tag = manager.getTagByName(rawSplit[2]);
                                    if (tag.isPresent()) {
                                        channel.sendMessageAsync("Creator: " + context.getJDA().getUserById(tag.get().getCreatorID()).getUsername() +
                                                "\nCreated at: " + tag.get().getTimeCreated() + (tag.get().getTimeLastEdited() != null ? "\nEdited At: " + tag.get().getTimeLastEdited() : ""), null);
                                    } else channel.sendMessageAsync("No such tag exists!", null);
                                }
                            }
                            break;
                        case "edit":
                            if (PermissionUtil.checkPermission(channel, context.getJDA().getSelfInfo(), Permission.ADMINISTRATOR)) {
                                if (rawSplit.length >= 3) {
                                    if (rawSplit.length == 4) {
                                        Date edit = Calendar.getInstance().getTime();
                                        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM dd, hh:mma ('EST')");
                                        if (manager.getTagByName(rawSplit[2]).isPresent()) {
                                            manager.editTag(manager.getTagByName(rawSplit[2]).get(), rawSplit[3], format.format(edit));
                                        }
                                        manager.save(context.getGuild());
                                    } else {
                                        channel.sendMessageAsync("No value specified for \"" + rawSplit[2] + "\"!", null);
                                    }
                                } else {
                                    channel.sendMessageAsync("```md" +
                                            "\n[Subcommand](tag edit)" +
                                            "\n\n[Description](Edits an existing tag in this server, provided the tag is yours, or you have the \"Administrator\" permission.)" +
                                            "\n\n# Paramaters (<> Required, {} Optional)" +
                                            "\n[Usage](" + prefix + "tag edit <name> <new contents>```", null);
                                }
                            }
                            break;
                        default:
                            Optional<Tag> tag = manager.getTagByName(rawSplit[1]);
                            if (tag.isPresent()) channel.sendMessageAsync(tag.get().getContents(), null);
                            else channel.sendMessageAsync("No such tag exists!", null);
                            break;
                    }
                }
            });
}
