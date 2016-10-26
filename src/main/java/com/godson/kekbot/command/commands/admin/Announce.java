package com.godson.kekbot.command.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.XMLUtils;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.TextChannel;
import org.jdom2.JDOMException;

import java.io.IOException;

public class Announce {
    public static Command announce = new Command("announce")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Allows you to config various \"announcement\" settings, including the welcome message, farewell message, and KekBot's broadcasts.")
            .withUsage("{p}announce <welcome|farewell|broadcasts>")
            .userRequiredPermissions(Permission.ADMINISTRATOR)
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                String prefix = CommandRegistry.getForClient(KekBot.client).getPrefixForGuild(context.getGuild()) == null
                        ? CommandRegistry.getForClient(KekBot.client).getPrefix()
                        : CommandRegistry.getForClient(KekBot.client).getPrefixForGuild(context.getGuild());
                String rawSplit[] = context.getMessage().getContent().split(" ", 4);
                String serverID = context.getGuild().getId();
                if (rawSplit.length == 1) {
                    channel.sendMessage("```md\n[Command](announce)" +
                            "\n\n[Category](Administration)" +
                            "\n\n[Description](Allows you to config various \"announcement\" settings, including the welcome message, farewell message, and KekBot's broadcasts.)" +
                            "\n\n# Paramaters (<> Required, {} Optional)" +
                            "\n[Usage](" + prefix + "announce <welcome|farewell|broadcasts>))```");
                } else {
                    try {
                        switch (rawSplit[1]) {
                            case "welcome":
                                if (rawSplit.length == 2) {
                                    channel.sendMessage("```md\n[Subcommand](announce welcome)" +
                                            "\n\n[Description](Allows the user to set the welcome message, the channel the message will be sent to, as well as review their server's settings.)" +
                                            "\n\n# Paramaters (<> Required, {} Optional)" +
                                            "\n[Usage](" + prefix + "announce welcome <message|channel|review>)```");
                            } else {
                                    switch (rawSplit[2]) {
                                        case "message":
                                            if (rawSplit.length >= 4) {
                                                if (rawSplit[3].equals("reset")) {
                                                    XMLUtils.deleteWelcomeMessage(serverID);
                                                    channel.sendMessage("Done, I will no longer remember what to tell people when they join this server.");
                                                } else {
                                                    XMLUtils.setWelcomeMessage(serverID, channel, rawSplit[3]);
                                                }
                                            } else {
                                                channel.sendMessage("What's the message? :neutral_face:");
                                            }
                                            break;
                                        case "channel":
                                            if (rawSplit.length >= 4) {
                                                if (rawSplit[3].equals("reset")) {
                                                    XMLUtils.deleteWelcomeChannel(serverID);
                                                    channel.sendMessage("Done, I will no longer remember what channel to welcome people in.");
                                                } else {
                                                    if (context.getMessage().getMentionedChannels().size() == 0) {
                                                        channel.sendMessage("The channel you want to assign welcomes to *must* be in the form of a mention1");
                                                    } else {
                                                        XMLUtils.setWelcomeChannel(serverID, channel, context.getMessage().getMentionedChannels().get(0));
                                                    }
                                                }
                                            } else {
                                                channel.sendMessage("Where am I supposed to welcome new people? :neutral_face:");
                                            }
                                            break;
                                        case "review":
                                            XMLUtils.reviewWelcomeSettings(serverID, channel);
                                            break;
                                    }

                                }
                                break;
                            case "farewell":
                                if (rawSplit.length == 2) {
                                    channel.sendMessage("```md\n[Subcommand](announce farewell)" +
                                            "\n\n[Description](Allows the user to set the farewell message, the channel the message will be sent to, as well as review their server's settings.)" +
                                            "\n\n# Paramaters (<> Required, {} Optional)" +
                                            "\n[Usage](" + prefix + "announce farewell <message|channel|review>)```");
                                } else {
                                    switch (rawSplit[2]) {
                                        case "message":
                                            if (rawSplit.length == 4) {
                                                if (rawSplit[3].equals("reset")) {
                                                    XMLUtils.deleteGoodbyeMessage(serverID);
                                                    channel.sendMessage("Done, I will go back to using the default message.");
                                                } else {
                                                    XMLUtils.setGoodbyeMessage(serverID, channel, rawSplit[3]);
                                                }
                                            } else {
                                                channel.sendMessage("What do you want me to tell everyone when people leave? :neutral_face:");
                                            }
                                            break;
                                        case "channel":
                                            if (rawSplit.length == 4) {
                                                if (rawSplit[3].equals("reset")) {
                                                    XMLUtils.deleteGoodbyeChannel(serverID);
                                                    channel.sendMessage("Done, I will no longer remember where to announce people leaving this server.");
                                                } else {
                                                    if (context.getMessage().getMentionedChannels().size() == 0) {
                                                        channel.sendMessage("The channel you want to assign welcomes to *must* be in the form of a mention1");
                                                    } else {
                                                        XMLUtils.setGoodbyeChannel(serverID, channel, context.getMessage().getMentionedChannels().get(0));
                                                        channel.sendMessage("Alright, I will let everyone know when someone leaves in " + context.getMessage().getMentionedChannels().get(0).getAsMention() + ". :thumbsup:");
                                                    }
                                                }
                                            } else {
                                                channel.sendMessage("Where do you want me to tell everyone about people leaving? :neutral_face:");
                                            }
                                            break;
                                        case "review":
                                            XMLUtils.reviewFarewellSettings(serverID, channel);
                                            break;
                                    }
                                }
                                break;
                            case "broadcasts":
                                if (rawSplit.length == 2) {
                                    channel.sendMessage("```md\n[Subcommand](announce broadcasts)" +
                                            "\n\n[Description](Allows the user to enable or disabled KekBot's broadcasts, set the channel KekBot's broadcats will be sent to, as well as review their server's settings.)" +
                                            "\n\n# Paramaters (<> Required, {} Optional)" +
                                            "\n[Usage](" + prefix + "announce broadcasts <channel|enable|disable|review>)```");
                                } else {
                                    switch (rawSplit[2]) {
                                        case "channel":
                                            if (rawSplit.length == 4) {
                                                if (rawSplit[3].equals("reset")) {
                                                    XMLUtils.deleteBroadcastsChannel(serverID);
                                                    channel.sendMessage("Done, I will revert to using the first channel I find.");
                                                } else {
                                                    if (context.getMessage().getMentionedChannels().size() == 0) {
                                                        channel.sendMessage("The channel you want to assign welcomes to *must* be in the form of a mention1");
                                                    } else {
                                                        XMLUtils.setBroadcastsChannel(serverID, context.getMessage().getMentionedChannels().get(0).getId());
                                                        channel.sendMessage("Alright, all future broadcasts will be posted in " + context.getMessage().getMentionedChannels().get(0).getAsMention() + ". :thumbsup:");
                                                    }
                                                }
                                            } else {
                                                channel.sendMessage("Where do you want Broadcasts to be sent? :neutral_face:");
                                            }
                                            break;
                                        case "enable":
                                            XMLUtils.enableBroadcasts(serverID, channel);
                                            break;
                                        case "disable":
                                            XMLUtils.disableBroadcasts(serverID, channel);
                                            break;
                                        case "review":
                                            XMLUtils.reviewBroadcastSettings(serverID, channel);
                                            break;
                                    }
                                }
                        }
                    } catch (JDOMException | IOException e) {
                        e.printStackTrace();
                    }
                }
            })
            .onFailure((context, reason) -> context.getTextChannel().sendMessage(context.getMessage().getAuthor().getAsMention() + ", you don't have the `Administrator` permission!")
            );
}
