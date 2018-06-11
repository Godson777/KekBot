package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Pick extends Command {

    private final String noChoicesGiven = "command.fun.pick.noargs";

    public Pick() {
        name = "pick";
        aliases = new String[]{"choose", "decide"};
        description = "Has KekBot pick one of X choices for you.";
        usage.add("pick <option> | <option> {can continue adding more options by seperating them with | }");
        usage.add("pick <option>, <option>, [or ]<option> {can continue adding more options by seperating them with a comma}");
        usage.add("pick <option> <option> {can continue adding more options by seperating them with a space}");
        category = new Category("Fun");
        cooldownScope = CooldownScope.USER_GUILD;
        cooldown = 10;
    }

    /**
     * Removes "or " from the last element of list.
     */
    //This method may be moved later to Utils.
    private List<String> stripOr(List<String> list) {
        int lastIndex = list.size() - 1;
        list.set(lastIndex, list.get(lastIndex).replaceAll("^or ", ""));
        return list;
    }

    //This method may be moved later to Utils.
    private List<String> prepareChoices(String choicesString, String splitOn) {
        return Arrays.stream(choicesString.split(splitOn))
                .map(Utils::removeWhitespaceEdges)
                .filter(c -> !c.isEmpty())
                .collect(Collectors.toList());
    }

    //This method may be moved later to Utils.
    private List<String> parseChoices(String choicesString) {
        List<String> choices = prepareChoices(choicesString, "\\u007c");
        if (choices.size() == 1) {
            // choices.get(0) is obviously the only element
            choices = choices.get(0).contains(",") ?
                    stripOr(prepareChoices(choices.get(0), ",")) :
                    prepareChoices(choices.get(0), " ");
        }
        return choices;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length == 0) {
            event.getChannel().sendMessage(noChoicesGiven).queue();
            return;
        }

        List<String> choices = parseChoices(event.combineArgs());
        if (choices.size() > 1) {
            Random random = new Random();
            event.getChannel().sendMessage(KekBot.respond(Action.CHOICE_MADE, event.getLocale(), "`" + choices.get(random.nextInt(choices.size())) + "`")).queue();
        } else if (choices.size() == 1) {
            event.getChannel().sendMessage(event.getString("command.fun.pick.onearg", "`" + choices.get(0) + "`")).queue();
        } else {
            event.getChannel().sendMessage(noChoicesGiven).queue();
        }
    }
}
