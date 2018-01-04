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
        event.getChannel().sendMessage("Want to add KekBot to your server? Use the link below! \nhttps://discordapp.com/oauth2/authorize?&client_id=213151748855037953&scope=bot&permissions=8").queue();
    }
}
