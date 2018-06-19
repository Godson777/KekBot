package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import javafx.scene.control.DatePicker;
import net.dv8tion.jda.core.entities.User;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
            if (event.getMentionedUsers().size() > 0) {
                if (event.getArgs().length > 1) {
                    double toPay;
                    try {
                        BigDecimal bd = new BigDecimal(Double.valueOf(event.getArgs()[1]));
                        bd = bd.setScale(2, RoundingMode.HALF_UP);
                        toPay = bd.doubleValue();
                    } catch (NumberFormatException e) {
                        event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), "`" + event.getArgs()[1] + "`")).queue();
                        return;
                    }
                    if (payer.canSpend(toPay)) {
                        User payee = event.getMentionedUsers().get(0);
                        if (payee.isBot()) {
                            event.getChannel().sendMessage(event.getString("command.fun.pay.bot")).queue();
                            return;
                        }

                        if (toPay < 0) {
                            event.getChannel().sendMessage(event.getString("command.fun.pay.negative")).queue();
                            return;
                        }

                        payer.payUser(toPay, payee);
                        event.getChannel().sendMessage(event.getString("command.fun.pay.success", payee.getName() + "#" + payee.getDiscriminator(), CustomEmote.printPrice(toPay))).queue();
                    } else {
                        event.getChannel().sendMessage(event.getString("command.fun.pay.notenoughfunds")).queue();
                    }
                } else event.getChannel().sendMessage(event.getString("command.fun.pay.noamount")).queue();
            }
        } else event.getChannel().sendMessage(event.getString("command.fun.pay.noargs")).queue();
    }
}
