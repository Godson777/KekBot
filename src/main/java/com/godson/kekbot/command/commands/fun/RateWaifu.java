package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.objects.WaifuManager;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class RateWaifu extends Command{
    private final WaifuManager waifuManager;

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
            event.getChannel().sendMessage( CustomEmote.think() + " " + event.getString("command.fun.ratewaifu.rating", "`" + (isMention(event.combineArgs()) ? event.getMentionedUsers().get(0).getName() : event.combineArgs()) + "`", score) + " " + event.getString("command.fun.ratewaifu." + score)).queue();
        } else event.getChannel().sendMessage(CustomEmote.think() + " " + event.getString("command.fun.ratewaifu.noargs")).queue();
    }
}
