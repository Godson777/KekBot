package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;

public class RateWaifu {
    public static Command rateWaifu = new Command("ratewaifu")
            .withAliases("waifu")
            .withCategory(CommandCategory.FUN)
            .withDescription("Feel bad about your waifu? Need to verify that she's the one? This is for you.")
            .withUsage("{p}ratewaifu <waifu's name>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                if (rawSplit.length == 1) {
                    context.getTextChannel().sendMessage(CustomEmote.think() + " I could rate *you*, but I'd be giving a biased score just cause you didn't tell me who I'm supposed to rate.").queue();
                } else if (rawSplit.length == 2) {
                    int score = KekBot.waifuManager.rateWaifu(rawSplit[1].toLowerCase().replace("@everyone", "@\u200Beveryone"));
                    String ratings[] = { "Not a good match, after all... \uD83D\uDC94", "Well, if you're desperate enough...", "On the bright side, it could be worse.",
                            "If anything, you could *probably* be friends with benefits.", "It might be a rocky road, but it could still work. ",
                            "Well, it could be better.", "Yeah, I could see this relationship working.", "Yeah, I'd tap that.",
                            "Honestly, you should feel glad seeing a 9.", "WEW LAD, DAS A 10/10 WAIFU. ...Wait, do I hear wedding bells?" };
                    context.getTextChannel().sendMessage( CustomEmote.think() + " I'd give *" + rawSplit[1] + "* a " + score + "/10. " + ratings[score-1]).queue();
                }
            });
}
