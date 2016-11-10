package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Objects.PollObject;
import net.dv8tion.jda.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

public class Poll {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static Command poll = new Command("poll")
            .withDescription("Creates a poll.")
            .withUsage("")
            .withCategory(CommandCategory.FUN)
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                if (!KekBot.manager.guildHasPoll(context.getGuild())) {
                    if (rawSplit.length == 1) {
                        channel.sendMessageAsync("No poll title specified!", null);
                    } else {
                        String pollVariables[] = rawSplit[1].split("\\u007c");
                        if (pollVariables.length == 1) {
                            channel.sendMessageAsync("No poll options specified!", null);
                        } else {
                            List<String> list = new ArrayList<>();
                            for (String option : pollVariables) {
                                if (option.matches(".*\\w.*") && !option.equals(pollVariables[0])) {
                                    if (option.startsWith(" ")) option = option.replaceFirst("([ ]+)", "");
                                    if (option.endsWith(" ")) option = option.replaceAll("([ ]+$)", "");
                                    list.add(option);
                                }
                            }
                            String options[] = list.toArray(EMPTY_STRING_ARRAY);
                            if (options.length != 1) {
                                PollObject poll = KekBot.manager.createPoll(context.getGuild(), channel, pollVariables[0], context.getAuthor(), options);
                                StringBuilder builder = new StringBuilder();
                                for (int i = 0; i < poll.getOptions().length; i++) {
                                    builder.append(i + 1).append(".").append("**").append(poll.getOptions()[i]).append("**").append("\n");
                                }
                                channel.sendMessageAsync(context.getAuthor().getAsMention() + " has just started a poll! Cast your vote by using {p}vote <number>!\n\n".replace("{p}",
                                        (CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(context.getGuild()) != null
                                                ? CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(context.getGuild()) : "$")) +
                                        "__**" + poll.getTitle() + "**__\n\n" + builder.toString(), null);
                            } else {
                                channel.sendMessageAsync("You have to supply at least more than one option!", null);
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
                        channel.sendMessageAsync(poll.getCreator().getUsername() + "#" + poll.getCreator().getDiscriminator() + "started the following poll:\n\n" +
                                poll.getTitle() + "\n" + builder.toString(), null);
                    } else channel.sendMessageAsync("There's already an ongoing poll!", null);
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
                        channel.sendMessageAsync("`" + rawSplit[1] + "` is not a valid number!", null);
                    } catch (IllegalArgumentException e) {
                        channel.sendMessageAsync(context.getAuthor().getAsMention() + " You've already voted for \"" + poll.getOptions()[Integer.valueOf(rawSplit[1]) - 1] + "\"!", null);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        channel.sendMessageAsync(context.getAuthor().getAsMention() + " That's not a valid option!", null);
                    }
                } else {
                    channel.sendMessageAsync(context.getAuthor().getAsMention() + ", There's no ongoing poll!", null);
                }
            });
}
