package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.objects.WaifuManager;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class RateWaifu extends Command{
    private final WaifuManager waifuManager;
    private final String ratings[] = { "Not a good match, after all... \uD83D\uDC94", "Well, if you're desperate enough...", "On the bright side, it could be worse.",
            "If anything, you could *probably* be friends with benefits.", "It might be a rocky road, but it could still work. ",
            "Well, it could be better.", "Yeah, I could see this relationship working.", "Yeah, I'd tap that.",
            "Honestly, you should feel glad seeing a 9.", "WEW LAD, DAS A 10/10 WAIFU. ...Wait, do I hear wedding bells?" };

    public RateWaifu() {
        name = "ratewaifu";
        aliases = new String[]{"waifu"};
        description = "Feel bad about your waifu? Need to verify that she's the one? This is for you.";
        usage.add("ratewaifu <waifu's name>");
        category = new Category("Fun");
        waifuManager = new WaifuManager();
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length > 0) {
            int score = waifuManager.rateWaifu(event.combineArgs().toLowerCase());
            event.getChannel().sendMessage( CustomEmote.think() + " I'd give *" + event.combineArgs() + "* a " + score + "/10. " + ratings[score-1]).queue();
        } else event.getChannel().sendMessage(CustomEmote.think() + " I could rate *you*, but I'd be giving a biased score just cause you didn't tell me who I'm supposed to rate.").queue();
    }
}
