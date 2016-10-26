package com.godson.kekbot.command.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.XMLUtils;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.utils.PermissionUtil;
import org.jdom2.IllegalNameException;
import org.jdom2.JDOMException;

import java.io.IOException;

public class Tag {
    public static Command tag = new Command("tag")
            .withAliases("t")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Allows you to ADD, REMOVE, and LIST tags, you can also get INFO on a tag. Which will send a message based on what's stored in the tag.")
            .withUsage("{p}tag {list|add|remove|list} <tag name>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getContent().split(" ", 4);
                TextChannel channel = context.getTextChannel();
                String serverID = context.getGuild().getId();
                String prefix = CommandRegistry.getForClient(KekBot.client).getPrefixForGuild(context.getGuild()) == null
                        ? CommandRegistry.getForClient(KekBot.client).getPrefix()
                        : CommandRegistry.getForClient(KekBot.client).getPrefixForGuild(context.getGuild());
                if (rawSplit.length == 1) {
                    channel.sendMessage("Not enough parameters. Check " + prefix + "help for usage on this command!");
                } else if (rawSplit.length >= 2) {
                    switch (rawSplit[1]) {
                        case "add":
                            if (PermissionUtil.checkPermission(channel, KekBot.client.getSelfInfo(), Permission.MESSAGE_WRITE)) {
                                if (rawSplit.length >= 3) {
                                    if (rawSplit.length == 4) {
                                        try {
                                            XMLUtils.addTag(serverID, channel, context.getMessage().getAuthor(), rawSplit[2], rawSplit[3]);
                                        } catch (IOException | JDOMException e) {
                                            e.printStackTrace();
                                        } catch (IllegalNameException e) {
                                            channel.sendMessage("Tag names must not contain any symbols such as `<, [, {, etc...` and cannot start with a number!");
                                        }
                                    } else {
                                        channel.sendMessage("No value specified for \"" + rawSplit[2] + "\"!");
                                    }
                                } else {
                                    channel.sendMessage("```md" +
                                            "\n[Subcommand](tag add)" +
                                            "\n\n[Description](Adds a tag to this server.)" +
                                            "\n\n# Paramaters (<> Required, {} Optional)" +
                                            "\n[Usage](" + prefix + "tag add <name> <contents>```");
                                }
                            }
                            break;
                        case "remove":
                            if (rawSplit.length == 2) {
                                channel.sendMessage("```md" +
                                        "\n[Subcommand](tag remove)" +
                                        "\n\n[Description](Removes a tag from this server, provided if it's your tag, or you have the \"Administrator\" permission.)" +
                                        "\n\n# Paramaters (<> Required, {} Optional)" +
                                        "\n[Usage](" + prefix + "tag remove <name>```");
                            } else {
                                try {
                                    XMLUtils.removeTag(serverID, channel, context.getMessage().getAuthor(), rawSplit[2]);
                                } catch (JDOMException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case "list":
                            try {
                                XMLUtils.listTags(context.getGuild(), channel);
                            } catch (JDOMException | IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "info":
                            if (rawSplit.length == 2) {
                                channel.sendMessage("```md" +
                                        "\n[Subcommand](tag info)" +
                                        "\n\n[Description](Gets information on a specified tag, if it exists.)" +
                                        "\n\n# Paramaters (<> Required, {} Optional)" +
                                        "\n[Usage](" + prefix + "tag info <name>```");
                            } else {
                                try {
                                    XMLUtils.getTagInfo(serverID, channel, rawSplit[2]);
                                } catch (JDOMException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        default:
                            try {
                                XMLUtils.sendTag(serverID, channel, rawSplit[1]);
                            } catch (JDOMException | IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            });
}
