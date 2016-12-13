package com.godson.kekbot.commands.community;

import com.darichey.discord.api.Command;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AddResponse {
    public static Command addResponse = new Command("addresponse")
            .onExecuted(context -> {
                if (context.getMember().getRoles().contains(context.getGuild().getRoleById("253345539989569547"))) {
                    String[] args = context.getArgs();
                    TextChannel channel = context.getTextChannel();
                    if (args.length >= 2) {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            if (!(args[i].equals("") && builder.length() < 1)) builder.append(args[i]);
                            if (i + 1 != args.length) builder.append(" ");
                        }
                        String response = builder.toString();
                        try {
                            List<String> numberSlots = new ArrayList<String>();
                            for (int i = 0; i < Action.valueOf(args[0]).getBlanksNeeded(); i++) {
                                numberSlots.add("{" + (i + 1) + "}");
                            }
                            int filled = StringUtils.countMatches(response, "{}");
                            int blanks = Action.valueOf(args[0]).getBlanksNeeded();
                            if (numberSlots.stream().anyMatch(response::contains) && response.contains("{}")) {
                                channel.sendMessage("You cannot mix regular blanks with numeric blanks!").queue();
                            } else if (numberSlots.stream().allMatch(response::contains)) {
                                KekBot.addResponse(Action.valueOf(args[0]), response);
                                channel.sendMessage("Success.").queue();
                            } else if (numberSlots.stream().noneMatch(response::contains)) {
                                if (filled == blanks) {
                                KekBot.addResponse(Action.valueOf(args[0]), response);
                                channel.sendMessage("Success.").queue();
                            } else if (filled < blanks) channel.sendMessage(String.format("Missing %s blanks.", blanks - filled)).queue();
                            else if (filled > blanks) channel.sendMessage("Too many blanks. (Required: " + blanks + ". Filled: " + filled + ")").queue();
                            } else {
                                channel.sendMessage(String.format("Not enough numeric blanks. (This action requires %s blanks to be filled.)", blanks)).queue();
                            }

                        } catch (IllegalArgumentException e) {
                            channel.sendMessage("Action not found. Please refer to <#251272936693039105> for list of actions.").queue();
                        }
                    } else {
                        channel.sendMessage("Not enough params.").queue();
                    }
                }
            });
}
