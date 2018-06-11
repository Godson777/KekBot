package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class Invite extends Command {

    public Invite() {
        name = "invite";
        description = "Gives you KekBot's invite link.";
        usage.add("invite");
        category = new Category("General");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        event.getChannel().sendMessage(event.getString("command.general.invite", "https://discordapp.com/oauth2/authorize?&client_id=213151748855037953&scope=bot&permissions=8")).queue();
    }
}
