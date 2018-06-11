package com.godson.kekbot.command.commands.botowner.botadmin;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Responses extends Command {

    private final String paramError = "Not enough params.";

    public Responses() {
        name = "responses";
        category = new Category("Bot Admin");
        commandPermission = CommandPermission.ADMIN;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        String[] args = event.getArgs();
        TextChannel channel = event.getTextChannel();
        if (args.length > 0) {
            switch (args[0]) {
                case "add":
                    if (args.length < 3) {
                        channel.sendMessage(paramError).queue();
                        return;
                    }
                    String response = event.combineArgs(2);
                    try {
                        List<String> numberSlots = new ArrayList<String>();
                        for (int i = 0; i < Action.valueOf(args[1]).getBlanksNeeded(); i++) {
                            numberSlots.add("{" + (i + 1) + "}");
                        }
                        int filled = StringUtils.countMatches(response, "{}");
                        int blanks = Action.valueOf(args[1]).getBlanksNeeded();
                        if (numberSlots.stream().anyMatch(response::contains) && response.contains("{}")) {
                            channel.sendMessage("You cannot mix regular blanks with numeric blanks!").queue();
                        } else if (numberSlots.stream().allMatch(response::contains)) {
                            KekBot.addResponse(Action.valueOf(args[1]), response);
                            channel.sendMessage("Success.").queue();
                        } else if (numberSlots.stream().noneMatch(response::contains)) {
                            if (filled == blanks) {
                                KekBot.addResponse(Action.valueOf(args[1]), response);
                                channel.sendMessage("Success.").queue();
                            } else if (filled < blanks) channel.sendMessage(String.format("Missing %s blanks.", blanks - filled)).queue();
                            else if (filled > blanks) channel.sendMessage("Too many blanks. (Required: " + blanks + ". Filled: " + filled + ")").queue();
                        } else {
                            channel.sendMessage(String.format("Not enough numeric blanks. (This action requires %s blanks to be filled.)", blanks)).queue();
                        }
                    } catch (IllegalArgumentException e) {
                        channel.sendMessage("Action not found.").queue();
                    }
                    break;
                case "remove":
                    if (args.length < 3) {
                        channel.sendMessage(paramError).queue();
                        return;
                    }
                    try {
                        int toRemove = Integer.valueOf(args[2]) - 1;
                        Action action = Action.valueOf(args[1]);
                        if (toRemove > KekBot.getResponses(action).size()) {
                            event.getChannel().sendMessage("Number provided is larger than the current length of responses for that action.").queue();
                            return;
                        }
                        KekBot.removeResponse(action, toRemove);
                        event.getChannel().sendMessage("Removed response.").queue();
                    } catch (NumberFormatException e) {
                        event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), "`" + args[2] + "`")).queue();
                    } catch (IllegalArgumentException e) {
                        event.getChannel().sendMessage("Action not found.").queue();
                    }
                    break;
                case "list":
                    if (args.length < 2) {
                        channel.sendMessage(paramError).queue();
                        return;
                    }
                    try {
                        List<String> responses = KekBot.getResponses(Action.valueOf(args[1]));
                        Paginator.Builder builder = new Paginator.Builder()
                                .setEventWaiter(KekBot.waiter)
                                .addItems(responses.toArray(new String[responses.size()]))
                                .useNumberedItems(true)
                                .addUsers(event.getAuthor())
                                .waitOnSinglePage(true)
                                .setText("");
                        builder.build().display(channel);
                    } catch (IllegalArgumentException e) {
                        channel.sendMessage("Action not found.").queue();
                    }
                    break;
            }
        } else {
            channel.sendMessage(paramError).queue();
        }
    }
}
