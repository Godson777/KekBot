package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Pick {
    public static Command pick = new Command("pick")
            .withAliases("choose", "decide")
            .withCategory(CommandCategory.FUN)
            .withDescription("Has KekBot pick ")
            .withUsage("{p}pick <option> | <option> {can continue adding more options by seperating them with | }")
            .onExecuted(context -> {
                String noChoicesGiven = "You haven't given me any choices, though...";

                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                if (rawSplit.length <= 1) {
                    channel.sendMessage(noChoicesGiven).queue();
                    return;
                }

                List<String> toFormat = Arrays.asList(rawSplit[1].split("\\u007c"));
                List<String> choices = new ArrayList<>();
                for (String string : toFormat) {
                    String choice = KekBot.removeWhitespaceEdges(string);
                    if (!choice.equals("")) choices.add(choice);
                }
                if (choices.size() > 1) {
                    Random random = new Random();
                    channel.sendMessage(KekBot.respond(context, Action.CHOICE_MADE, choices.get(random.nextInt(choices.size())))).queue();
                } else if (choices.size() == 1) {
                    channel.sendMessage("Well, I guess I'm choosing `" + choices.get(0) + "`, since you haven't given me anything else to pick...").queue();
                } else {
                    channel.sendMessage(noChoicesGiven).queue();
                }
            });
}
