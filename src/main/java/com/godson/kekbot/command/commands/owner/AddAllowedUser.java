package com.godson.kekbot.command.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.XMLUtils;
import org.jdom2.JDOMException;
import sx.blah.discord.handle.obj.IUser;

import java.io.IOException;
import java.util.Optional;

public class AddAllowedUser {
    public static Command addAllowedUser = new Command("addUser")
            .onExecuted(context -> {
                String args[] = context.getArgs();
                if (context.getMessage().getAuthor().getID().equals(XMLUtils.getBotOwner())) {
                    if (args.length != 0) {
                        Optional<IUser> user = Optional.ofNullable(KekBot.client.getUserByID(args[0]));
                        if (user.isPresent()) {
                            try {
                                XMLUtils.addAllowedUsers(args[0]);
                                EasyMessage.send(context.getMessage().getChannel(), "Added " + KekBot.client.getUserByID(args[0]).getName() + " to list of allowed users.");
                            } catch (JDOMException | IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            EasyMessage.send(context.getMessage().getChannel(), "Not a valid user ID!");
                        }
                    } else {
                        EasyMessage.send(context.getMessage().getChannel(), "No user ID specified.");
                    }
                }
            });
}
