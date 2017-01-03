package com.godson.kekbot.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Settings.Settings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;

public class Announce {
    public static Command announce = new Command("announce")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Allows you to config various \"announcement\" Settings, including the welcome message, farewell message, and KekBot's broadcasts.")
            .withUsage("{p}announce <welcome|farewell|broadcasts>")
            .userRequiredPermissions(Permission.ADMINISTRATOR)
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                String prefix = CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(context.getGuild()) == null
                        ? CommandRegistry.getForClient(context.getJDA()).getPrefix()
                        : CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(context.getGuild());
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 4);
                Settings settings = GSONUtils.getSettings(context.getGuild());
                if (rawSplit.length == 1) {
                    channel.sendMessage("```md\n[Command](announce)" +
                            "\n\n[Category](Administration)" +
                            "\n\n[Description](Allows you to config various \"announcement\" Settings, including the welcome message, farewell message, and KekBot's broadcasts.)" +
                            "\n\n# Paramaters (<> Required, {} Optional)" +
                            "\n[Usage](" + prefix + "announce <welcome|farewell|broadcasts>))```").queue();
                } else {
                    switch (rawSplit[1]) {
                        case "welcome":
                            if (rawSplit.length == 2) {
                                channel.sendMessage("```md\n[Subcommand](announce welcome)" +
                                        "\n\n[Description](Allows the user to set the welcome message, the channel the message will be sent to, as well as review their server's Settings.)" +
                                        "\n\n# Paramaters (<> Required, {} Optional)" +
                                        "\n[Usage](" + prefix + "announce welcome <message|channel|toggle|review>)```").queue();
                            } else {
                                switch (rawSplit[2]) {
                                    case "message":
                                        if (rawSplit.length >= 4) {
                                            if (rawSplit[3].equals("reset")) {
                                                settings.setWelcomeMessage(null).save(context.getGuild());
                                                channel.sendMessage("Done, I will no longer remember what to tell people when they join this server.").queue();
                                            } else {
                                                settings.setWelcomeMessage(rawSplit[3]).save(context.getGuild());
                                                channel.sendMessage("Successfully set welcome message to: \n\"" + rawSplit[3].replace("{mention}", "@Example User").replace("{name}", "Example User") + "\"").queue();
                                                if (!settings.welcomeChannelIsSet())
                                                    channel.sendMessage("However, you still need to tell me which channel I'll be welcoming people in!").queue();
                                            }
                                        } else {
                                            channel.sendMessage("What's the message? :neutral_face:").queue();
                                        }
                                        break;
                                    case "channel":
                                        if (rawSplit.length >= 4) {
                                            if (rawSplit[3].equals("reset")) {
                                                settings.setWelcomeChannel(null).save(context.getGuild());
                                                channel.sendMessage("Done, I will no longer remember what channel to welcome people in.").queue();
                                            } else {
                                                if (context.getMessage().getMentionedChannels().size() == 0) {
                                                    channel.sendMessage("The channel you want to assign welcomes to *must* be in the form of a mention!").queue();
                                                } else {
                                                    settings.setWelcomeChannel(context.getMessage().getMentionedChannels().get(0)).save(context.getGuild());
                                                    channel.sendMessage("Alright, I will now welcome people who join this server in " + context.getMessage().getMentionedChannels().get(0).getAsMention() + ". :thumbsup:").queue();
                                                    if (!settings.welcomeMessageIsSet())
                                                        channel.sendMessage("However, I still need the welcome message, how else am I supposed to welcome people if I don't know what to tell them?").queue();
                                                }
                                            }
                                        } else {
                                            channel.sendMessage("Where am I supposed to welcome new people? :neutral_face:").queue();
                                        }
                                        break;
                                    case "toggle":
                                        if (!settings.welcomeEnabled()) {
                                            settings.toggleWelcome(true).save(context.getGuild());
                                            channel.sendMessage("Welcome announcements are now **ON**.").queue();
                                        } else {
                                            settings.toggleWelcome(false).save(context.getGuild());
                                            channel.sendMessage("Welcome announcements are now **OFF**.").queue();
                                        }
                                        break;
                                }

                            }
                            break;
                        case "farewell":
                            if (rawSplit.length == 2) {
                                channel.sendMessage("```md\n[Subcommand](announce farewell)" +
                                        "\n\n[Description](Allows the user to set the farewell message, the channel the message will be sent to, as well as review their server's Settings.)" +
                                        "\n\n# Paramaters (<> Required, {} Optional)" +
                                        "\n[Usage](" + prefix + "announce farewell <message|channel|toggle|review>)```").queue();
                            } else {
                                switch (rawSplit[2]) {
                                    case "message":
                                        if (rawSplit.length == 4) {
                                            if (rawSplit[3].equals("reset")) {
                                                settings.setFarewellMessage(null).save(context.getGuild());
                                                channel.sendMessage("Done, I will go back to using the default message.").queue();
                                            } else {
                                                settings.setFarewellMessage(rawSplit[3]).save(context.getGuild());
                                                channel.sendMessage("Successfully set goodbye message to: \n\"" + rawSplit[3].replace("{mention}", "@Example User").replace("{name}", "Example User") + "\"").queue();
                                                if (!settings.farewellChannelIsSet())
                                                    channel.sendMessage("However, you still need to tell me which channel to announce people leaving!").queue();
                                            }
                                        } else {
                                            channel.sendMessage("What do you want me to tell everyone when people leave? :neutral_face:").queue();
                                        }
                                        break;
                                    case "channel":
                                        if (rawSplit.length == 4) {
                                            if (rawSplit[3].equals("reset")) {
                                                settings.setFarewellChannel(null).save(context.getGuild());
                                                channel.sendMessage("Done, I will no longer remember where to announce people leaving this server.").queue();
                                            } else {
                                                if (context.getMessage().getMentionedChannels().size() == 0) {
                                                    channel.sendMessage("The channel you want to assign welcomes to *must* be in the form of a mention!").queue();
                                                } else {
                                                    settings.setFarewellChannel(context.getMessage().getMentionedChannels().get(0)).save(context.getGuild());
                                                    channel.sendMessage("Alright, I will let everyone know when someone leaves in " + context.getMessage().getMentionedChannels().get(0).getAsMention() + ". :thumbsup:").queue();
                                                    if (!settings.farewellMessageIsSet())
                                                        channel.sendMessage("However, you still need to tell me what to say when people leave!").queue();
                                                }
                                            }
                                        } else {
                                            channel.sendMessage("Where do you want me to tell everyone about people leaving? :neutral_face:").queue();
                                        }
                                        break;
                                    case "toggle":
                                        if (!settings.farewellEnabled()) {
                                            settings.toggleFarewell(true).save(context.getGuild());
                                            channel.sendMessage("Farewell announcements are now **ON**.").queue();
                                        } else {
                                            settings.toggleFarewell(false).save(context.getGuild());
                                            channel.sendMessage("Farewell announcements are now **OFF**.").queue();
                                        }
                                        break;
                                }
                            }
                            break;
                        case "broadcasts":
                            if (rawSplit.length == 2) {
                                channel.sendMessage("```md\n[Subcommand](announce broadcasts)" +
                                        "\n\n[Description](Allows the user to enable or disabled KekBot's broadcasts, set the channel KekBot's broadcats will be sent to, as well as review their server's Settings.)" +
                                        "\n\n# Paramaters (<> Required, {} Optional)" +
                                        "\n[Usage](" + prefix + "announce broadcasts <channel|toggle|review>)```").queue();
                            } else {
                                switch (rawSplit[2]) {
                                    case "channel":
                                        if (rawSplit.length == 4) {
                                            if (rawSplit[3].equals("reset")) {
                                                settings.setBroadcastChannel(null).save(context.getGuild());
                                                channel.sendMessage("Done, I will revert to using the first available channel I find.").queue();
                                            } else {
                                                if (context.getMessage().getMentionedChannels().size() == 0) {
                                                    channel.sendMessage("The channel you want to assign welcomes to *must* be in the form of a mention!").queue();
                                                } else {
                                                    settings.setBroadcastChannel(context.getMessage().getMentionedChannels().get(0)).save(context.getGuild());
                                                    channel.sendMessage("Alright, all future broadcasts will be posted in " + context.getMessage().getMentionedChannels().get(0).getAsMention() + ". :thumbsup:").queue();
                                                }
                                            }
                                        } else {
                                            channel.sendMessage("Where do you want Broadcasts to be sent? :neutral_face:").queue();
                                        }
                                        break;
                                    case "toggle":
                                        if (settings.broadcastsEnabled()) {
                                            settings.toggleBroadcasts(false).save(context.getGuild());
                                            channel.sendMessage("Broadcasts are now **OFF**.").queue();
                                        } else {
                                            settings.toggleBroadcasts(true).save(context.getGuild());
                                            channel.sendMessage("Broadcasts are now **ON**.").queue();
                                        }
                                        break;
                                }
                            }
                            break;
                        case "review":
                            channel.sendMessage("**REVIEW**" +
                                    "\n\n__Welcome:__" +
                                    "\nStatus: " + (settings.welcomeEnabled() ? "ON" : "OFF") +
                                    "\nChannel: " + (settings.welcomeChannelIsSet() ? settings.getWelcomeChannel(context.getJDA()).getAsMention() : "None") +
                                    "\nMessage: " + (settings.welcomeMessageIsSet() ? settings.getWelcomeMessage().replace("{mention}", "@Example User").replace("{name}", "Example User") : "None") +
                                    "\n\n__Farewell:__" +
                                    "\nStatus: " + (settings.farewellEnabled() ? "ON" : "OFF") +
                                    "\nChannel: " + (settings.farewellChannelIsSet() ? settings.getFarewellChannel(context.getJDA()).getAsMention() : "None") +
                                    "\nMessage: " + (settings.farewellMessageIsSet() ? settings.getFarewellMessage().replace("{mention}", "@Example User").replace("{name}", "Example User") : "None") +
                                    "\n\n__Broadcasts:__" +
                                    "\nStatus: " + (settings.broadcastsEnabled() ? "ON" : "OFF") +
                                    "\nChannel " + (settings.broadcastChannelIsSet() ? settings.getBroadcastChannel(context.getJDA()).getAsMention() : "None")).queue();
                            break;
                    }
                }
            })
            .onFailure((context, reason) -> context.getTextChannel().sendMessage(KekBot.respond(context, Action.NOPERM_USER, "`Administrator`")).queue()
            );
}
