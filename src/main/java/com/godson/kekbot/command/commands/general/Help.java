package com.godson.kekbot.command.commands.general;


import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.KekBot;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Help {


    public static Command help = new Command("help")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Sends you this message. Can also provide with more info on a specified command.")
            .caseSensitive(true)
            .onExecuted(context -> {
                String args[] = context.getArgs();
                if (args.length == 0) {
                    List<String> commands = new ArrayList<String>();
                    List<String> pages = new ArrayList<String>();
                    CommandRegistry registry = CommandRegistry.getForClient(KekBot.client);
                    EnumSet<CommandCategory> categories = EnumSet.allOf(CommandCategory.class);
                    commands.add("# KekBot's default prefix for commands is \"$\". However, the server you're on might have it use a different prefix. If you're unsure, feel free to go a server and say \"@KekBot prefix\"");
                    commands.add("# To add me to your server, send me an invite link!\n");
                    categories.forEach(category -> {
                        commands.add("# " + category.toString());
                        for (int i = 0; i < registry.getCommands().size(); i++) {
                            Set<String> aliases = registry.getCommands().get(i).getAliases();
                            if (registry.getCommands().get(i).getCategory() != null) {
                                if (registry.getCommands().get(i).getCategory().equals(category)) {
                                    commands.add("[$" + registry.getCommands().get(i).getName() +
                                            (registry.getCommands().get(i).getAliases().size() != 0 ? " | " + StringUtils.join(aliases, " | ") : "") + "](" + registry.getCommands().get(i).getDescription() + ")");
                                }
                            }
                        }
                        commands.add("");
                    });
                    for (int i = 0; i < registry.getCommands().size(); i += 25) {
                        try {
                            pages.add(StringUtils.join(commands.subList(i, i + 25), "\n"));
                        } catch (IndexOutOfBoundsException e) {
                            pages.add(StringUtils.join(commands.subList(i, commands.size()), "\n"));
                        }
                    }

                    context.getMessage().getAuthor().getPrivateChannel().sendMessage("__**KekBot**__\n*Your helpful meme-based bot!*\n" +
                            "```md\n" + pages.get(0) + "\n\n" + "[Page](1" + "/" + pages.size() + ")\n" +
                            "# Type \"help <number>\" to view that page!" + "```");
                    context.getTextChannel().sendMessage(context.getMessage().getAuthor().getAsMention() + " Alright, check your PMs! :thumbsup:");
                } else {
                    String name = args[0].toLowerCase();
                    Optional<Command> cmd = CommandRegistry.getForClient(KekBot.client).getCommandByName(args[0], true);
                    if (cmd.isPresent()) {
                        Command command = cmd.get();
                        Set<String> set = cmd.get().getAliases();
                        context.getTextChannel().sendMessage("```md\n[Command](" + command.getName() + ")" +
                                        (command.getAliases().size() != 0 ? "\n\n[Aliases](" + StringUtils.join(set, ", ") + ")" : "") +
                                "\n\n[Category](" + command.getCategory() + ")" +
                                "\n\n[Description](" + command.getDescription() + ")" +
                                "\n\n# Paramaters (<> Required, {} Optional)" +
                                "\n[Usage](" + command.getUsage().replace("{p}", (CommandRegistry.getForClient(KekBot.client).getPrefixForGuild(context.getGuild()) != null ? CommandRegistry.getForClient(KekBot.client).getPrefixForGuild(context.getGuild()) : "$")) + ")```"
                        );
                    } else {
                        context.getTextChannel().sendMessage("Command not found.");
                    }
                }
}
            );
}
