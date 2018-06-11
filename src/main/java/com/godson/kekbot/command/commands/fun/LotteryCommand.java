package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class LotteryCommand extends Command {

    public LotteryCommand() {
        name = "lottery";
        description = "Takes you to the lottery.";
        aliases = new String[]{"lotto"};
        usage.add("lottery");
        usage.add("lottery buy {tickets}");
        category = new Category("Fun");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage(KekBot.lottery.printStats(event.getAuthor(), event.getGuild())).queue();
            return;
        }

        switch (event.getArgs()[0].toLowerCase()) {
            case "buy":
                if (event.getArgs().length > 1) {
                    int tickets;
                    try {
                        tickets = Integer.valueOf(event.getArgs()[1]);
                    } catch (NumberFormatException e) {
                        event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), "`" + event.getArgs()[1] + "`")).queue();
                        return;
                    }
                    if (tickets > 0) event.getChannel().sendMessage(KekBot.lottery.addTicket(event.getAuthor(), tickets, event.getLocale())).queue();
                    else event.getChannel().sendMessage(event.getString("command.fun.lottery.invalidamount")).queue();
                } else event.getChannel().sendMessage(KekBot.lottery.addTicket(event.getAuthor(), event.getLocale())).queue();
                break;
            case "winners":
                event.getChannel().sendMessage(KekBot.lottery.listWinners(event.getLocale())).queue();
                break;
            case "draw":
                if (event.isBotOwner()) {
                    KekBot.lottery.forceDraw(false);
                }
                break;
            case "forcejackpot":
                if (event.isBotOwner()) {
                    KekBot.lottery.forceDraw(true);
                }
                break;
        }
    }
}
