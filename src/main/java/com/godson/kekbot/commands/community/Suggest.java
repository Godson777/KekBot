package com.godson.kekbot.commands.community;

import com.darichey.discord.api.Command;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.EventWaiter.EventWaiter;
import com.godson.kekbot.Questionaire.QuestionType;
import com.godson.kekbot.Questionaire.Questionnaire;
import com.godson.kekbot.Responses.ResponseSuggestions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Suggest {
    private static EventWaiter waiter = KekBot.waiter;
    public static Command suggest = new Command("suggest")
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                new Questionnaire(context)
                        .addChoiceQuestion("What would you like to suggest?\nYou can say `cancel` at any time to cancel. ", "Response")
                        .execute(results -> {
                            switch ((String) results.getAnswer(0)) {
                                case "Response":
                                    List<String> actions = Arrays.stream(Action.values()).map(Enum::name).collect(Collectors.toList());
                                    new Questionnaire(results)
                                            .addChoiceQuestion("Great! What action do you think your response best fits in?", actions.toArray(new String[actions.size()]))
                                            .execute(results1 -> {
                                                Action action = Arrays.stream(Action.values()).filter(action1 -> action1.name().equalsIgnoreCase((String) results1.getAnswer(0))).findFirst().get();
                                                new Questionnaire(results1)
                                                        .addQuestion(String.format("Alright! What's your response? (This action requires %s %s {} in your response.)", action.getBlanksNeeded(), (action.getBlanksNeeded() == 1 ? "blank" : "blanks")), QuestionType.STRING)
                                                .execute(results2 -> {
                                                    String response = (String) results2.getAnswer(0);
                                                    List<String> numberSlots = new ArrayList<String>();
                                                    for (int i = 0; i < action.getBlanksNeeded(); i++) {
                                                        numberSlots.add("{" + (i + 1) + "}");
                                                    }
                                                    int filled = StringUtils.countMatches(response, "{}");
                                                    int blanks = action.getBlanksNeeded();
                                                    if (numberSlots.stream().anyMatch(response::contains) && response.contains("{}")) {
                                                        channel.sendMessage("You cannot mix regular blanks with numeric blanks!").queue();
                                                        results2.reExecute();
                                                    } else if (numberSlots.stream().allMatch(response::contains)) {
                                                        submitResponse(context.getAuthor(), action, response, context.getJDA());
                                                        channel.sendMessage("Thanks for submitting your response suggestion! This will go straight to the Quality Control team, from there, they'll decide if it's high quality enough. If accepted, you'll get a notification, and KekBot will add it to its list!").queue();
                                                    } else if (numberSlots.stream().noneMatch(response::contains)) {
                                                        if (filled == blanks) {
                                                            submitResponse(context.getAuthor(), action, response, context.getJDA());
                                                            channel.sendMessage("Thanks for submitting your response suggestion! This will go straight to the Quality Control team, from there, they'll decide if it's high quality enough. If accepted, you'll get a notification, and KekBot will add it to its list!").queue();
                                                        } else if (filled < blanks) {
                                                            channel.sendMessage(String.format("Missing %s blanks.", blanks - filled)).queue();
                                                            results2.reExecute();
                                                        }
                                                        else if (filled > blanks) {
                                                            channel.sendMessage("Too many blanks. (Required: " + blanks + ". Filled: " + filled + ")").queue();
                                                            results2.reExecute();
                                                        }
                                                    } else {
                                                        channel.sendMessage(String.format("Not enough numeric blanks. (This action requires %s blanks to be filled.)", blanks)).queue();
                                                        results2.reExecute();
                                                    }
                                                });
                                            });
                                    break;
                            }
                        });
            });

    private static void submitResponse(User suggester, Action action, String response, JDA jda) {
        ResponseSuggestions responses = GSONUtils.getSuggestions().addSuggestion(suggester, action, response);
        EmbedBuilder builder = new EmbedBuilder();
        Guild guild = jda.getGuildById("221910104495095808");
        builder.setTitle("NEW RESPONSE SUGGESTION").setColor(Color.YELLOW).setThumbnail(suggester.getAvatarUrl())
                .addField("Suggester:", suggester.getName() + "#" + suggester.getDiscriminator(), false)
                .addField("Action:", action.name(), false)
                .addField("Response:", response, false)
                .setDescription(KekBot.replacePrefix(guild, "Use {p}suggestions responses view " + responses.getSuggestions().size() + "to view submission."));
        responses.save();
        jda.getTextChannelById("253346114584051712").sendMessage(builder.build()).queue();
    }
}
