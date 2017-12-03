package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Profile.Profile;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Settings.Config;
import net.dv8tion.jda.core.entities.User;

public class Pay {
    public static Command pay = new Command("pay")
            .withCategory(CommandCategory.FUN)
            .withDescription("Pays a user.")
            .withUsage("{p}pay <@user> <amount>")
            .onExecuted(context -> {
                Profile payer = Profile.getProfile(context.getAuthor());
                if (context.getArgs().length > 0) {
                    if (context.getMessage().getMentionedUsers().size() > 0) {
                        if (context.getArgs().length > 1) {
                            int toPay;
                            try {
                                toPay = Integer.valueOf(context.getArgs()[1]);
                            } catch (NumberFormatException e) {
                                context.getTextChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + context.getArgs()[1] + "`")).queue();
                                return;
                            }
                            if (payer.canSpend(toPay)) {
                                User payee = context.getMessage().getMentionedUsers().get(0);
                                payer.payUser(toPay, payee);
                                context.getTextChannel().sendMessage("You have successfully paid " + payee.getName() + "#" + payee.getDiscriminator() + " " + CustomEmote.printPrice(toPay) + ".").queue();
                            } else {
                                context.getTextChannel().sendMessage("You don't have that many topkeks to pay to that user!").queue();
                            }
                        } else context.getTextChannel().sendMessage("You didn't specify how much to pay.").queue();
                    }
                } else context.getTextChannel().sendMessage("You didn't specify anyone to pay.").queue();
            });
}
