package com.godson.kekbot.command.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.XMLUtils;
import org.jdom2.JDOMException;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

import java.io.IOException;


public class Prefix {
    public static Command prefix = new Command("prefix")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Allows the user to change the prefix KekBot uses for a specific server, as long as it isn't over 2 characters long.")
            .withUsage("{p}prefix <new Prefix>")
            .onExecuted(context -> {
                String args[] = context.getArgs();
                IGuild server = context.getMessage().getGuild();
                String oldPrefix = (CommandRegistry.getForClient(KekBot.client).getPrefixForGuild(server) != null
                        ? CommandRegistry.getForClient(KekBot.client).getPrefixForGuild(server)
                        : CommandRegistry.getForClient(KekBot.client).getPrefix());
                IChannel channel = context.getMessage().getChannel();
                if (server.getOwner().equals(context.getMessage().getAuthor())) {
                    if (args.length == 0) {
                        EasyMessage.send(channel, context.getMessage().getAuthor().mention() + " :anger: You must supply the prefix you want me to use!");
                    } else {
                        String input = args[0];
                        if (input.length() <= 2) {
                            try {
                                XMLUtils.setPrefix(server, args[0]);
                                EasyMessage.send(channel, "Successfully changed prefix from " + oldPrefix + " " + "to " + input);
                            } catch (JDOMException | IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            EasyMessage.send(channel, "For your convenience, and due to limitations, I cannot allow you to set prefixes more than __**2**__ character long.");
                        }
                    }
                } else {
                    EasyMessage.send(channel, "Sorry, only the owner of the server can run this command!");
                }
            });
}
