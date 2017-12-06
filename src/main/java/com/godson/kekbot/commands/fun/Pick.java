package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Utils;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Pick {
    /**
     * Removes "or " from the last element of list.
     */
    //This method may be moved later to Utils.
    private static List<String> stripOr(List<String> list) {
        int lastIndex = list.size() - 1;
        list.set(lastIndex, list.get(lastIndex).replace("^or ", ""));
        return list;
    }

    //This method may be moved later to Utils.
    private static List<String> prepareChoices(String choicesString, String splitOn) {
        return Arrays.stream(choicesString.split(splitOn))
            .map(Utils::removeWhitespaceEdges)
            .filter(c -> !c.isEmpty())
            .collect(Collectors.toList());
    }

    //This method may be moved later to Utils.
    private static List<String> parseChoices(String choicesString) {
        List<String> choices = Pick.prepareChoices(choicesString, "\\u007c");
        if (choices.size() == 1) {
            // choices.get(0) is obviously the only element
            choices = choices.get(0).contains(",") ?
                Pick.stripOr(Pick.prepareChoices(choices.get(0), ",")) :
                Pick.prepareChoices(choices.get(0), " ");
        }
        return choices;
    }

    public static Command pick = new Command("pick")
            .withAliases("choose", "decide")
            .withCategory(CommandCategory.FUN)
            .withDescription("Has KekBot pick one of X choices for you.")
            .withUsage("{p}pick <option> | <option> {can continue adding more options by seperating them with | }\n" +
                "{p}pick <option>, <option>, [or ]<option> {can continue adding more options by seperating them with a comma}\n" +
                "{p}pick <option> <option> {can continue adding more options by seperating them with a space}")
            .onExecuted(context -> {
                String noChoicesGiven = "You haven't given me any choices, though...";

                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                if (rawSplit.length <= 1) {
                    channel.sendMessage(noChoicesGiven).queue();
                    return;
                }

                List<String> choices = Pick.parseChoices(rawSplit[1]);
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
