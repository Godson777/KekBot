package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.core.entities.User;

public class Pay extends Command {

    public Pay() {
        name = "pay";
        description = "Pays a user in topkeks.";
        usage.add("pay <@user> <amount>");
        category = new Category("Fun");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        Profile payer = Profile.getProfile(event.getAuthor());
        if (event.getArgs().length > 0) {
            if (event.getMessage().getMentionedUsers().size() > 0) {
                if (event.getArgs().length > 1) {
                    int toPay;
                    try {
                        toPay = Integer.valueOf(event.getArgs()[1]);
                    } catch (NumberFormatException e) {
                        event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + event.getArgs()[1] + "`")).queue();
                        return;
                    }
                    if (payer.canSpend(toPay)) {
                        User payee = event.getMessage().getMentionedUsers().get(0);
                        if (payee.isBot()) {
                            event.getChannel().sendMessage("You can't pay a bot! They have no use for topkeks, anyway.").queue();
                            return;
                        }
                        payer.payUser(toPay, payee);
                        event.getChannel().sendMessage("You have successfully paid " + payee.getName() + "#" + payee.getDiscriminator() + " " + CustomEmote.printPrice(toPay) + ".").queue();
                    } else {
                        event.getChannel().sendMessage("You don't have that many topkeks to pay to that user!").queue();
                    }
                } else event.getChannel().sendMessage("You didn't specify how much to pay.").queue();
            }
        } else event.getChannel().sendMessage("You didn't specify anyone to pay.").queue();
    }
}
