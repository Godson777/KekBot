package com.godson.kekbot.command.commands.owner;

import com.darichey.discord.api.Command;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Settings.Config;
import com.godson.kekbot.XMLUtils;
import net.dv8tion.jda.entities.User;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.Optional;

public class AddAllowedUser {
    public static Command addAllowedUser = new Command("addUser")
            .onExecuted(context -> {
                String args[] = context.getArgs();
                Config config = GSONUtils.getConfig();
                if (context.getMessage().getAuthor().getId().equals(config.getBotOwner())) {
                    if (args.length != 0) {
                        Optional<User> user = Optional.ofNullable(context.getJDA().getUserById(args[0]));
                        if (user.isPresent()) {
                            config.addAllowedUser(args[0]).save();
                            context.getMessage().getChannel().sendMessageAsync("Added " + context.getJDA().getUserById(args[0]).getUsername() + " to list of allowed users.", null);
                        } else {
                            context.getMessage().getChannel().sendMessageAsync("Not a valid user ID!", null);
                        }
                    } else {
                        context.getMessage().getChannel().sendMessageAsync("No user ID specified.", null);
                    }
                }
            });
}
