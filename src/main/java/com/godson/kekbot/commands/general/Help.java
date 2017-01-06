package com.godson.kekbot.commands.general;


import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.GSONUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Help {


    public static Command help = new Command("help")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Sends you this message. Can also provide with more info on a specified command.")
            .withUsage("{p}help {command}")
            .caseSensitive(true)
            .onExecuted(context -> {
                String args[] = context.getArgs();
                if (args.length == 0) {
                    List<String> commands = new ArrayList<String>();
                    List<String> pages = new ArrayList<String>();
                    CommandRegistry registry = CommandRegistry.getForClient(context.getJDA());
                    EnumSet<CommandCategory> categories = EnumSet.allOf(CommandCategory.class);
                    commands.add("# KekBot's default prefix for commands is \"$\". However, the server you're on might have it use a different prefix. If you're unsure, feel free to go a server and say \"@KekBot prefix\"");
                    commands.add("# To add me to your server, send me an invite link!\n");
                    categories.forEach(category -> {
                        if (!category.equals(CommandCategory.BOT_OWNER)) commands.add("# " + category.toString());
                        if (category.equals(CommandCategory.BOT_OWNER) && context.getAuthor().getId().equals(GSONUtils.getConfig().getBotOwner())) commands.add("# " + category.toString());
                        for (Command command : registry.getCommands()) {
                            Set<String> aliases = command.getAliases();
                            if (command.getCategory() != null) {
                                if (command.getCategory().equals(category)) {
                                    if (!category.equals(CommandCategory.BOT_OWNER))
                                        commands.add("[$" + command.getName() +
                                                (aliases.size() != 0 ? " | " + StringUtils.join(aliases, " | ") : "") + "](" + command.getDescription() + ")");
                                    if (category.equals(CommandCategory.BOT_OWNER) && context.getAuthor().getId().equals(GSONUtils.getConfig().getBotOwner()))
                                        commands.add("[$" + command.getName() +
                                                (aliases.size() != 0 ? " | " + StringUtils.join(aliases, " | ") : "") + "](" + command.getDescription() + ")");
                                }
                            }
                        }
                        if (!category.equals(CommandCategory.BOT_OWNER)) commands.add("");
                    });
                    for (int i = 0; i < commands.size(); i += 25) {
                            try {
                                pages.add(StringUtils.join(commands.subList(i, i + 25), "\n"));
                            } catch (IndexOutOfBoundsException e) {
                                pages.add(StringUtils.join(commands.subList(i, commands.size()), "\n"));
                            }
                    }

                    String message = "__**KekBot**__\n*Your helpful meme-based bot!*\n" +
                            "```md\n" + pages.get(0) + "\n\n" + "[Page](1" + "/" + pages.size() + ")\n" +
                            "# Type \"help <number>\" to view that page!" + "```";
                    if (!context.getAuthor().hasPrivateChannel()) context.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
                    else context.getAuthor().getPrivateChannel().sendMessage(message).queue();
                    context.getTextChannel().sendMessage(context.getMessage().getAuthor().getAsMention() + " Alright, check your PMs! :thumbsup:").queue();
                } else {
                    Optional<Command> cmd = CommandRegistry.getForClient(context.getJDA()).getCommandByName(args[0], true);
                    if (cmd.isPresent()) {
                        Command command = cmd.get();
                        Set<String> set = cmd.get().getAliases();
                        if (!command.getCategory().equals(CommandCategory.BOT_OWNER))
                            context.getTextChannel().sendMessage("```md\n[Command](" + command.getName() + ")" +
                                    (command.getAliases().size() != 0 ? "\n\n[Aliases](" + StringUtils.join(set, ", ") + ")" : "") +
                                    "\n\n[Category](" + command.getCategory() + ")" +
                                    "\n\n[Description](" + command.getDescription() + ")" +
                                    "\n\n#Usage ( Paramaters (<> Required, {} Optional) ):" +
                                    "\n" + command.getUsage().replace("{p}", (CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(context.getGuild()) != null ? CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(context.getGuild()) : "$")) + "```").queue();
                    } else {
                        context.getTextChannel().sendMessage("Command not found.").queue();
                    }
                }
}
            );
}
