package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.Profile.Profile;

public class Balance {
    public static Command balanace = new Command("balance")
            .withAliases("bal")
            .withCategory(CommandCategory.FUN)
            .withDescription("Shows you your balance of topkeks.")
            .withUsage("{p}balance")
            .onExecuted(context -> {
                Profile profile = Profile.getProfile(context.getAuthor());
                context.getTextChannel().sendMessage("You have " + CustomEmote.printPrice(profile.getTopkeks())).queue();
            });
}
