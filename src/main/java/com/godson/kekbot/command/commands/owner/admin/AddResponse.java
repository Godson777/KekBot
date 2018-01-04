package com.godson.kekbot.command.commands.owner.admin;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AddResponse extends Command {

    public AddResponse() {
        name = "addresponse";
        category = CommandCategories.botAdmin;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        String[] args = event.getArgs();
        TextChannel channel = event.getTextChannel();
        if (args.length >= 2) {
            String response = event.combineArgs(1);
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
}
