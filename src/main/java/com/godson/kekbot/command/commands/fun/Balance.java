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
        if (event.getArgs().length > 0) {
            if (event.getMentionedUsers().size() != 1) {
                event.getChannel().sendMessage((event.getMentionedUsers().size() > 1 ? event.getString("command.fun.balance.toomanyusers") : event.getString("command.fun.balance.nousers"))).queue();
                return;
            }
            Profile profile = Profile.getProfile(event.getMentionedUsers().get(0));
            event.getChannel().sendMessage(event.getString("command.fun.balance.otherbal",event.getMentionedUsers().get(0).getName(), CustomEmote.printPrice(profile.getTopkeks()))).queue();
            return;
        }

        Profile profile = Profile.getProfile(event.getAuthor());
        event.getChannel().sendMessage(event.getString("command.fun.balance.authorbal", CustomEmote.printPrice(profile.getTopkeks()))).queue();
    }
}
