package com.godson.kekbot.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.Settings.Settings;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;


public class Prefix {
    public static Command prefix = new Command("prefix")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Allows the user to change the prefix KekBot uses for a specific server, as long as it isn't over 2 characters long.")
            .withUsage("{p}prefix <new Prefix>")
            .onExecuted(context -> {
                String args[] = context.getArgs();
                Guild server = context.getGuild();
                String oldPrefix = (CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(server) != null
                        ? CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(server)
                        : CommandRegistry.getForClient(context.getJDA()).getPrefix());
                TextChannel channel = context.getTextChannel();
                Settings settings = GSONUtils.getSettings(context.getGuild());
                if (server.getOwner().equals(server.getMember(context.getAuthor()))) {
                    if (args.length == 0) {
                        channel.sendMessage(context.getAuthor().getAsMention() + " :anger: You must supply the prefix you want me to use!").queue();
                    } else {
                        if (args[0].length() <= 2) {
                            if (!args[0].equals(CommandRegistry.getForClient(context.getJDA()).getPrefix())) {
                                settings.setPrefix(args[0]).save(context.getGuild());
                                CommandRegistry.getForClient(context.getJDA()).setPrefixForGuild(server, args[0]);
                            } else {
                                if (settings.getPrefix() != null) settings.setPrefix(null);
                                CommandRegistry.getForClient(context.getJDA()).deletePrefixForGuild(server);
                            }
                            channel.sendMessage("Successfully changed prefix from " + oldPrefix + " " + "to " + args[0]).queue();
                        } else {
                            channel.sendMessage("For your convenience, and due to limitations, I cannot allow you to set prefixes more than __**2**__ character long.").queue();
                        }
                    }
                } else {
                    channel.sendMessage("Sorry, only the owner of the server can run this command!").queue();
                }
            });
}
