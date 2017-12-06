package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Objects.PollObject;
import com.godson.kekbot.Utils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Poll {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static Command poll = new Command("poll")
            .withDescription("Creates a poll.")
            .withUsage("{p}poll <title> | <MM:SS> | <option...> {can continue adding more options by seperating them with | }" +
                    "\n(If a poll is open, you can also say {p}poll stop to end it early, or {p}poll cancel to cancel it altogether.)")
            .withCategory(CommandCategory.FUN)
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                if (!KekBot.manager.guildHasPoll(context.getGuild())) {
                    if (rawSplit.length == 1) {
                        channel.sendMessage("No poll title specified!").queue();
                    } else {
                        String timeSplit[] = rawSplit[1].split("\\u007c", 2);
                        if (timeSplit.length == 1) {
                            channel.sendMessage("No time specified!").queue();
                        } else {
                            String pollVariables[] = timeSplit[1].split("\\u007c");
                            long time = 0;
                            String timeStr = Utils.removeWhitespaceEdges(pollVariables[0]);
                            String[] split = timeStr.split(":");
                            try {
                                for (int i = split.length - 1, num = 0; i >= 0; i--, num++) {
                                    String s = split[i];
                                    switch (num) {
                                        case 0:
                                            time += TimeUnit.SECONDS.toMillis(Long.parseLong(s));
                                            break;
                                        case 1:
                                            time += TimeUnit.MINUTES.toMillis(Long.parseLong(s));
                                            break;
                                    }
                                }
                            } catch (NumberFormatException e) {
                                channel.sendMessage("`" + timeStr + "` is not a valid time format. (Valid time format: MM:SS)").queue();
                                return;
                            }
                            if (!(time >= TimeUnit.MINUTES.toMillis(1) && time <= TimeUnit.HOURS.toMillis(1))) {
                                channel.sendMessage("I'm sorry, polls must last longer than one minute, but shorter than an hour.").queue();
                            }
                            else {
                                if (pollVariables.length == 1) {
                                    channel.sendMessage("No poll options specified!").queue();
                                } else {
                                    List<String> list = new ArrayList<>();
                                    for (String option : pollVariables) {
                                        if (!option.equals(pollVariables[0])) {
                                            String formattedOption = Utils.removeWhitespaceEdges(option);
                                            if (!formattedOption.equals("")) list.add(formattedOption);
                                        }
                                    }
                                    String options[] = list.toArray(EMPTY_STRING_ARRAY);
                                    if (options.length != 1) {
                                        PollObject poll = KekBot.manager.createPoll(context, time, Utils.removeWhitespaceEdges(rawSplit[1].substring(0, rawSplit[1].indexOf("|"))), options);
                                        StringBuilder builder = new StringBuilder();
                                        for (int i = 0; i < poll.getOptions().length; i++) {
                                            builder.append(i + 1).append(". ").append("**").append(poll.getOptions()[i]).append("**").append("\n");
                                        }
                                        channel.sendMessage(context.getAuthor().getAsMention() + KekBot.replacePrefix(context.getGuild(), " has just started a poll! Cast your vote by using {p}vote <number>!\n\n") +
                                                "__**" + poll.getTitle() + "**__\n\n" + builder.toString()).queue();
                                    } else {
                                        channel.sendMessage("You have to supply at least more than one option!").queue();
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (rawSplit.length == 1) {
                        PollObject poll = KekBot.manager.getGuildsPoll(context.getGuild());
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < poll.getOptions().length; i++) {
                            builder.append(i+1).append(".").append("**").append(poll.getOptions()[i]).append("**").append("\n");
                        }
                        channel.sendMessage(poll.getCreator().getName() + "# " + poll.getCreator().getDiscriminator() + "started the following poll:\n\n" +
                                poll.getTitle() + "\n" + builder.toString()).queue();
                    } else {
                        switch (rawSplit[1]) {
                            case "stop":
                                if (context.getAuthor().equals(KekBot.manager.getGuildsPoll(context.getGuild()).getCreator()) || context.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                                    KekBot.manager.interruptPoll(context.getGuild());
                                } else {
                                    channel.sendMessage("Only the poll's creator, or someone with `Administrator` permissions can stop polls!").queue();
                                }
                                break;
                            case "cancel":
                                if (context.getAuthor().equals(KekBot.manager.getGuildsPoll(context.getGuild()).getCreator()) || context.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                                    KekBot.manager.cancelPoll(context.getGuild());
                                    channel.sendMessage("The poll has been cancelled.").queue();
                                } else {
                                    channel.sendMessage("Only the poll's creator, or someone with `Administrator` permissions can cancel polls!").queue();
                                }
                                break;
                            default:
                                    channel.sendMessage("There's already an ongoing poll!").queue();

                        }
                    }
                }
            });
    public static Command vote = new Command("vote")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                if (KekBot.manager.guildHasPoll(context.getGuild())) {
                    PollObject poll = KekBot.manager.getGuildsPoll(context.getGuild());
                    try {
                        poll.castVote(Integer.valueOf(rawSplit[1]) - 1, context.getAuthor());
                    } catch (NumberFormatException e) {
                        channel.sendMessage("`" + rawSplit[1] + "` is not a valid number!").queue();
                    } catch (IllegalArgumentException e) {
                        channel.sendMessage(context.getAuthor().getAsMention() + " You've already voted for \"" + poll.getOptions()[Integer.valueOf(rawSplit[1]) - 1] + "\"!").queue();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        channel.sendMessage(context.getAuthor().getAsMention() + " That's not a valid option!").queue();
                    }
                } else {
                    channel.sendMessage(context.getAuthor().getAsMention() + ", There's no ongoing poll!").queue();
                }
            });
}
