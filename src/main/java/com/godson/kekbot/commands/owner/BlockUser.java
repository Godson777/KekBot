package com.godson.kekbot.commands.owner;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.Settings.Config;
import net.dv8tion.jda.core.entities.User;

import java.util.Optional;

public class BlockUser {
    public static Command blockUser = new Command("blockuser")
            .withCategory(CommandCategory.BOT_OWNER)
            .onExecuted(context -> {
                String args[] = context.getArgs();
                Config config = GSONUtils.getConfig();
                if (context.getMessage().getAuthor().getId().equals(config.getBotOwner())) {
                    if (args.length != 0) {
                        Optional<User> user = Optional.ofNullable(context.getJDA().getUserById(args[0]));
                        if (user.isPresent()) {
                            config.addBlockedUser(args[0]).save();

                            context.getMessage().getChannel().sendMessage("Added " + context.getJDA().getUserById(args[0]).getName() + " to list of blocked users.").queue();
                        } else {
                            context.getMessage().getChannel().sendMessage("Not a valid user ID!").queue();
                        }
                    } else {
                        context.getMessage().getChannel().sendMessage("No user ID specified.").queue();
                    }
                }
            });
}
