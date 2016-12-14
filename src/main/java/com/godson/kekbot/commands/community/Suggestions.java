package com.godson.kekbot.commands.community;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandContext;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Questionaire.QuestionType;
import com.godson.kekbot.Questionaire.Questionnaire;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Responses.ResponseSuggestion;
import com.godson.kekbot.Responses.ResponseSuggestions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Suggestions {
    public static Command suggestions = new Command("suggestions")
            .onExecuted(context -> {
                if (context.getMember().getRoles().contains(context.getGuild().getRoleById("253345539989569547"))) {
                    TextChannel channel = context.getTextChannel();
                    String[] args = context.getArgs();
                    if (args.length >= 1) {
                        switch (args[0].toLowerCase()) {
                            case "responses":
                                ResponseSuggestions suggestions = GSONUtils.getSuggestions();
                                if (args.length >= 2) {
                                    switch (args[1].toLowerCase()) {
                                        case "list":
                                            if (suggestions.getSuggestions().size() > 0) {
                                                List<String> responses = new ArrayList<String>();
                                                for (int i = 0; i < GSONUtils.getSuggestions().getSuggestions().size(); i++) {
                                                    ResponseSuggestion suggestion = GSONUtils.getSuggestions().getSuggestions().get(i);
                                                    User suggester = context.getJDA().getUserById(suggestion.getSuggesterID());
                                                    responses.add("# " + String.valueOf(i + 1) + ". " + suggester.getName() + "#" + suggester.getDiscriminator() + " - " + suggestion.getActionName());
                                                }
                                                String list = "";
                                                int page = 0;
                                                if (args.length >= 3) {
                                                    try {
                                                        page = Integer.valueOf(args[2]) - 1;
                                                    } catch (NumberFormatException e) {
                                                        channel.sendMessage(KekBot.respond(context, Action.NOT_A_NUMBER, args[2])).queue();
                                                        return;
                                                    }
                                                }
                                                try {
                                                    if ((page * 30) > responses.size() && (page * 30 < 0)) {
                                                        channel.sendMessage("That page doesn't exist!").queue();
                                                        return;
                                                    } else
                                                        list = StringUtils.join(responses.subList((page * 30), ((page + 1) * 30)), "\n") +
                                                                (responses.size() > 30 ? "\n\nPage " + (page + 1) + "/" + (responses.size() / 30 + 1) : "");
                                                } catch (IndexOutOfBoundsException e) {
                                                    list = StringUtils.join(responses.subList((page * 30), responses.size()), "\n") +
                                                            (responses.size() > 30 ? "\n\nPage " + (page + 1) + "/" + (responses.size() / 30 + 1) : "");
                                                }
                                                channel.sendMessage("```md\n" + list + "```").queue();
                                            } else {
                                                channel.sendMessage("There are no responses to list.").queue();
                                            }
                                            break;
                                        case "view":
                                            if (suggestions.getSuggestions().size() > 0) {
                                                if (args.length >= 3) {
                                                    try {
                                                        int pick = Integer.valueOf(args[2]) - 1;
                                                        ResponseSuggestion suggestion = suggestions.getSuggestions().get(pick);
                                                        User suggester = context.getJDA().getUserById(suggestion.getSuggesterID());
                                                        EmbedBuilder builder = new EmbedBuilder();
                                                        builder.setTitle("RESPONSE SUGGESTION:").setThumbnail(suggester.getAvatarUrl())
                                                                .addField("Suggester:", suggester.getName() + "#" + suggester.getDiscriminator(), false)
                                                                .addField("Action:", suggestion.getActionName(), false)
                                                                .addField("Response:", suggestion.getSuggestedResponse(), false);
                                                        channel.sendMessage(builder.build()).queue();
                                                    } catch (NumberFormatException e) {
                                                        channel.sendMessage(KekBot.respond(context, Action.NOT_A_NUMBER, args[2])).queue();
                                                    } catch (IndexOutOfBoundsException e) {
                                                        //do nothing
                                                    }
                                                } else {
                                                    channel.sendMessage("asdf").queue();
                                                }
                                            }
                                            break;
                                        case "accept":
                                            if (suggestions.getSuggestions().size() > 0) {
                                                if (args.length >= 3) {
                                                    try {
                                                        int pick = Integer.valueOf(args[2]) - 1;
                                                        ResponseSuggestion response = suggestions.getSuggestions().get(pick);
                                                        User suggester = context.getJDA().getUserById(response.getSuggesterID());
                                                        EmbedBuilder builder = new EmbedBuilder();
                                                        builder.setColor(Color.green)
                                                                .setDescription("Response accepted.");
                                                        KekBot.addResponse(Action.valueOf(response.getActionName()), response.getSuggestedResponse());
                                                        String messsage = "Your response suggestion was accepted by " + context.getAuthor().getName() + "! (*" + response.getActionName() + "* || **\"" + response.getSuggestedResponse() + "\"**)";
                                                        if (!suggester.hasPrivateChannel())
                                                            suggester.openPrivateChannel().queue(priv -> priv.sendMessage(messsage).queue());
                                                        else
                                                            suggester.getPrivateChannel().sendMessage(messsage).queue();
                                                        suggestions.getSuggestions().remove(response);
                                                        suggestions.save();
                                                        channel.sendMessage(builder.build()).queue();
                                                    } catch (NumberFormatException e) {
                                                        channel.sendMessage(KekBot.respond(context, Action.NOT_A_NUMBER, args[2]));
                                                    } catch (IndexOutOfBoundsException e) {
                                                        //do nothing
                                                    }
                                                }
                                            }
                                            break;
                                        case "reject":
                                            if (suggestions.getSuggestions().size() > 0) {
                                                if (args.length >= 3) {
                                                    try {
                                                        int pick = Integer.valueOf(args[2]) - 1;
                                                        ResponseSuggestion response = suggestions.getSuggestions().get(pick);
                                                        new Questionnaire(context).addChoiceQuestion("Would you like to add a reason? (Yes/No)", "Yes", "No")
                                                                .execute(results -> {
                                                                    if (results.getAnswer(0).equals("Yes")) {
                                                                        new Questionnaire(results).addQuestion("Insert your reason:", QuestionType.STRING)
                                                                                .execute(results1 -> rejectResponse(context, response, results1.getAnswer(0).toString()));
                                                                    } else {
                                                                        rejectResponse(context, response, null);
                                                                        suggestions.getSuggestions().remove(response);
                                                                        suggestions.save();
                                                                    }
                                                                });
                                                    } catch (NumberFormatException e) {
                                                        channel.sendMessage(KekBot.respond(context, Action.NOT_A_NUMBER, args[2]));
                                                    }
                                                }
                                            }
                                            break;
                                    }
                                    break;
                                } else {
                                    //insert error message here
                                }
                                break;
                        }

                    } //insert error message here
                }
            });

    public static void rejectResponse(CommandContext context, ResponseSuggestion response, String reason) {
        String message = "Your response suggestion was unfortunately rejected by " + context.getAuthor().getName() + "." + (reason != null ? "\nReason: " + reason : "") + "\n(*" + response.getActionName() + "* || **" + response.getSuggestedResponse() + "**)";
        User suggester = context.getJDA().getUserById(response.getSuggesterID());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.red)
                .setDescription("Response rejected.");
        context.getTextChannel().sendMessage(builder.build()).queue();
        if (!suggester.hasPrivateChannel()) suggester.openPrivateChannel().queue(priv -> priv.sendMessage(message).queue());
        else suggester.getPrivateChannel().sendMessage(message).queue();
    }
}
