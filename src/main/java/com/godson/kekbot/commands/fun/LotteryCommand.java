package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;

public class LotteryCommand {
    public static Command lottery = new Command("lottery")
            .withCategory(CommandCategory.FUN)
            .withDescription("Takes you to the lottery.")
            .withUsage("{p}lottery\n{p}lottery buy {tickets}")
            .onExecuted(context -> {
                String[] args = context.getArgs();
                if (args.length < 1) {
                    context.getTextChannel().sendMessage(KekBot.lottery.printStats(context.getAuthor(), context.getGuild())).queue();
                    return;
                }

                if (args.length > 0) {
                    switch (args[0].toLowerCase()) {
                        case "buy":
                            if (args.length > 1) {
                                int tickets;
                                try {
                                    tickets = Integer.valueOf(context.getArgs()[1]);
                                } catch (NumberFormatException e) {
                                    context.getTextChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + context.getArgs()[1] + "`")).queue();
                                    return;
                                }
                                if (tickets > 0) context.getTextChannel().sendMessage(KekBot.lottery.addTicket(context.getAuthor(), tickets)).queue();
                                else context.getTextChannel().sendMessage("You wanna buy *how many* tickets? " + tickets + "? That doesn't make any sense...").queue();
                            } else context.getTextChannel().sendMessage(KekBot.lottery.addTicket(context.getAuthor())).queue();
                            break;
                        case "winners":
                            context.getTextChannel().sendMessage(KekBot.lottery.listWinners()).queue();
                            break;
                        case "draw":
                            if (context.getAuthor().equals(context.getJDA().getUserById(GSONUtils.getConfig().getBotOwner()))) {
                                KekBot.lottery.forceDraw(false);
                            }
                            break;
                        case "forcejackpot":
                            if (context.getAuthor().equals(context.getJDA().getUserById(GSONUtils.getConfig().getBotOwner()))) {
                                KekBot.lottery.forceDraw(true);
                            }
                            break;
                    }
                }
            });
}
