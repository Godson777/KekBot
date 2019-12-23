package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.objects.PollManager;
import com.godson.kekbot.util.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandClient;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.api.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Poll extends Command {

    private final String[] EMPTY_STRING_ARRAY = new String[0];
    private final PollManager manager = new PollManager();

    public Poll(CommandClient client) {
        name = "poll";
        description = "Creates a poll.";
        usage.add("poll <title> | <MM:SS> | <option> | <option> | {option...}");
        extendedDescription = "You can continue adding more options by seperating them with the | symbol." +
                "\n(If a poll is open, you can also say \"{p}poll stop\" to end it early, or \"{p}poll cancel\" to cancel it altogether.)";
        exDescPos = ExtendedPosition.AFTER;
        client.addCommand(new Vote());
        category = new Category("General");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (!manager.guildHasPoll(event.getGuild())) {
            if (event.getArgs().length > 0) {
                String combinedArgs = event.combineArgs();
                String timeSplit[] = combinedArgs.split("\\u007c", 2);
                if (timeSplit.length == 1) {
                    event.getChannel().sendMessage(event.getString("command.general.poll.notime")).queue();
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
                        event.getChannel().sendMessage(event.getString("command.general.poll.invalidtimeformat", "`" + timeStr + "`")).queue();
                        return;
                    }
                    if (!(time >= TimeUnit.MINUTES.toMillis(1) && time <= TimeUnit.HOURS.toMillis(1))) {
                        event.getChannel().sendMessage(event.getString("command.general.poll.invalidtime")).queue();
                    }
                    else {
                        if (pollVariables.length == 1) {
                            event.getChannel().sendMessage(event.getString("command.general.poll.nooptions")).queue();
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
                                PollManager.Poll poll = manager.createPoll(event, time, Utils.removeWhitespaceEdges(combinedArgs.substring(0, combinedArgs.indexOf("|"))), options);
                                StringBuilder builder = new StringBuilder();
                                for (int i = 0; i < poll.getOptions().length; i++) {
                                    builder.append(i + 1).append(". ").append("**").append(poll.getOptions()[i]).append("**").append("\n");
                                }
                                event.getChannel().sendMessage(event.getString("command.general.poll.start", event.getAuthor().getName(), event.getClient().getPrefix(event.getGuild().getId()) + "vote <number>!") + "\n\n" +
                                        "__**" + poll.getTitle() + "**__\n\n" + builder.toString()).queue();
                            } else {
                                event.getChannel().sendMessage(event.getString("command.general.poll.oneoption")).queue();
                            }
                        }
                    }
                }
            } else event.getChannel().sendMessage(event.getString("command.noargs", "`" + event.getPrefix() + "help poll`")).queue();
        } else {
            if (event.getArgs().length == 0) {
                PollManager.Poll poll = manager.getGuildsPoll(event.getGuild());
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < poll.getOptions().length; i++) {
                    builder.append(i+1).append(".").append("**").append(poll.getOptions()[i]).append("**").append("\n");
                }
                event.getChannel().sendMessage( event.getString("command.general.poll.currentpoll", poll.getCreator().getName() + "#" + poll.getCreator().getDiscriminator()) + "\n\n" +
                        poll.getTitle() + "\n" + builder.toString()).queue();
            } else {
                switch (event.getArgs()[0]) {
                    case "stop":
                        if (event.getAuthor().equals(manager.getGuildsPoll(event.getGuild()).getCreator()) || event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                            manager.interruptPoll(event.getGuild());
                        } else {
                            event.getChannel().sendMessage(event.getString("command.general.poll.stoperror", "`Administrator`")).queue();
                        }
                        break;
                    case "cancel":
                        if (event.getAuthor().equals(manager.getGuildsPoll(event.getGuild()).getCreator()) || event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                            manager.cancelPoll(event.getGuild());
                            event.getChannel().sendMessage(event.getString("command.general.poll.cancelled")).queue();
                        } else {
                            event.getChannel().sendMessage(event.getString("command.general.poll.cancelerror", "`Administrator`")).queue();
                        }
                        break;
                    default:
                        event.getChannel().sendMessage(event.getString("command.general.poll.existingpoll")).queue();

                }
            }
        }
    }

    public class Vote extends Command {

        public Vote() {
            name = "vote";
        }

        @Override
        public void onExecuted(CommandEvent event) {
            if (manager.guildHasPoll(event.getGuild())) {
                if (event.getArgs().length == 0) return;
                PollManager.Poll poll = manager.getGuildsPoll(event.getGuild());
                try {
                    poll.castVote(Integer.valueOf(event.getArgs()[0]) - 1, event.getAuthor());
                    event.getMessage().addReaction("\u2705").queue();
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), "`" + event.getArgs()[0] + "`")).queue();
                } catch (IllegalArgumentException e) {
                    event.getChannel().sendMessage(event.getString("command.general.poll.vote.samevote", event.getAuthor().getAsMention(), "`" + poll.getOptions()[Integer.valueOf(event.getArgs()[0]) - 1] + "`")).queue();
                } catch (ArrayIndexOutOfBoundsException e) {
                    event.getChannel().sendMessage(event.getString("command.general.poll.vote.invalidoption")).queue();
                }
            } else {
                event.getChannel().sendMessage(event.getString("command.general.poll.vote.nopoll")).queue();
            }
        }
    }
}