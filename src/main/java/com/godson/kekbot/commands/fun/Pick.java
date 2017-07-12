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
    /**
     * Removes "or " from the last element of coll.
     */
    protected static Collection<String> stripOr(Collection<String> coll) {
        int lastIndex = coll.size() - 1;
        coll[lastIndex] = coll[lastIndex].replace("^or ", "");
        return coll;
    }

    protected static List<String> prepareChoices(String choicesString, String splitOn) {
        return Arrays.stream(choicesString.split(splitOn))
            .map(c -> KekBot.removeWhitespaceEdges(c))
            .filter(c -> !c.isEmpty())
            .collect(Collectors.toList());
    }

    protected static List<String> parseChoices(String choicesString) {
        List<String> choices = Pick.prepareChoices(choicesString, "\\u007c");
        if (choices.size() == 1) {
            // choices[0] is obviously the only element
            choices = choices.contains(",") ?
                Pick.stripOr(Pick.prepareChoices(choices[0], ",")) :
                Pick.prepareChoices(choices[0], " ");
        }
        return choices;
    }

    public static Command pick = new Command("pick")
            .withAliases("choose", "decide")
            .withCategory(CommandCategory.FUN)
            .withDescription("Has KekBot pick ")
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
                    channel.sendMessage("Well, I guess I'm choosing `" + choices[0] + "`, since you haven't given me anything else to pick...").queue();
                } else {
                    channel.sendMessage(noChoicesGiven).queue();
                }
            });
}
