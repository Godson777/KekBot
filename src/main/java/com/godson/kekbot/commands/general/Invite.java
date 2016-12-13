package com.godson.kekbot.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;

public class Invite {
    public static Command invite = new Command("invite")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Gives you KekBot's invite link.")
            .withUsage("{p}invite")
            .onExecuted(context -> context.getTextChannel().sendMessage("Want to add KekBot to your server? Use the link below! \nhttps://discordapp.com/oauth2/authorize?&client_id=213151748855037953&scope=bot&permissions=8").queue());
}
