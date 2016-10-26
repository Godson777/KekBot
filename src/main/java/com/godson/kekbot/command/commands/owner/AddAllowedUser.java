package com.godson.kekbot.command.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.XMLUtils;
import net.dv8tion.jda.entities.User;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.Optional;

public class AddAllowedUser {
    public static Command addAllowedUser = new Command("addUser")
            .onExecuted(context -> {
                String args[] = context.getArgs();
                if (context.getMessage().getAuthor().getId().equals(XMLUtils.getBotOwner())) {
                    if (args.length != 0) {
                        Optional<User> user = Optional.ofNullable(KekBot.client.getUserById(args[0]));
                        if (user.isPresent()) {
                            try {
                                XMLUtils.addAllowedUsers(args[0]);
                                context.getMessage().getChannel().sendMessage("Added " + KekBot.client.getUserById(args[0]).getUsername() + " to list of allowed users.");
                            } catch (JDOMException | IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            context.getMessage().getChannel().sendMessage("Not a valid user ID!");
                        }
                    } else {
                        context.getMessage().getChannel().sendMessage("No user ID specified.");
                    }
                }
            });
}
