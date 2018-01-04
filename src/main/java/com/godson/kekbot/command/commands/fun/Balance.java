package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class Balance extends Command {

    public Balance() {
        name = "balance";
        aliases = new String[]{"bal"};
        description = "Shows you your balance of topkeks.";
        usage.add("balance");
        category = new Category("Fun");
        cooldownScope = CooldownScope.USER;
        cooldown = 3;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        Profile profile = Profile.getProfile(event.getAuthor());
        event.getChannel().sendMessage("You have " + CustomEmote.printPrice(profile.getTopkeks())).queue();
    }
}
