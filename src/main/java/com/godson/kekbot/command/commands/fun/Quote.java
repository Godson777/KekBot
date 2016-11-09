package com.godson.kekbot.command.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.Settings.Quotes;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Quote {
    public static Command quote = new Command("quote")
            .withAliases("q")
            .withCategory(CommandCategory.FUN)
            .withDescription("Grabs a random command from a list of quotes made in a server.")
            .withUsage("{p}quote {add/remove/list} (Note: Adding and removing quotes requires the \"Manage Messages\" permission.)")
            .botRequiredPermissions(Permission.MESSAGE_WRITE)
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                Guild guild = context.getGuild();
                String rawSplit[] = context.getMessage().getContent().split(" ", 3);
                Quotes quotes = GSONUtils.getQuotes(guild);
                String prefix = CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(context.getGuild()) == null
                        ? CommandRegistry.getForClient(context.getJDA()).getPrefix()
                        : CommandRegistry.getForClient(context.getJDA()).getPrefixForGuild(context.getGuild());
                if (rawSplit.length == 1) {
                    List<String> quotesList = quotes.getQuotes();
                    if (quotesList.isEmpty()) {
                        channel.sendMessageAsync("You have no quotes!", null);
                    } else {
                        channel.sendMessageAsync(quotes.getQuote(), null);
                    }
                } else {
                    switch (rawSplit[1]) {
                        case "add":
                            if (PermissionUtil.checkPermission(guild, context.getAuthor(), Permission.MESSAGE_MANAGE)) {
                                if (rawSplit.length == 3) {
                                    List<String> quotesList = quotes.getQuotes();
                                    quotesList.add(rawSplit[2]);
                                    quotes.save(guild);
                                    channel.sendMessageAsync("Successfully added quote! :thumbsup:", null);
                                } else {
                                    channel.sendMessageAsync("```md\n[Subcommand](quote add)" +
                                            "\n\n[Description](Adds a quote.)" +
                                            "\n\n# Paramaters (<> Required, {} Optional)" +
                                            "\n[Usage](" + prefix + "quote add <quote>)```", null);
                                }
                            } else {
                                channel.sendMessageAsync(context.getAuthor().getAsMention() + ", you do not have the `Manage Messages` permission!", null);
                            }
                            break;
                        case "remove":
                            if (PermissionUtil.checkPermission(guild, context.getAuthor(), Permission.MESSAGE_MANAGE)) {
                                if (rawSplit.length == 3) {
                                    try {
                                        int quoteNumber = Integer.valueOf(rawSplit[2]);
                                        if (quotes.getQuotes().size() >= quoteNumber) {
                                            String quote = quotes.getQuote(quoteNumber-1);
                                            quotes.removeQuote(quoteNumber-1);
                                            channel.sendMessageAsync("Successfully removed quote: **" + quote + "**.", null);
                                        }
                                    } catch (NumberFormatException e) {
                                        channel.sendMessageAsync("\"" + rawSplit[2] + "\" is not a number!", null);
                                    }
                                } else {
                                    channel.sendMessageAsync("```md\n[Subcommand](quote remove)" +
                                            "\n\n[Description](Removes a specific quote. Use " + prefix + "quote list to get the quote's number.)" +
                                            "\n\n# Paramaters (<> Required, {} Optional)" +
                                            "\n[Usage](" + prefix + "quote remove <quote number>)```", null);
                                }
                            } else {
                                channel.sendMessageAsync(context.getAuthor().getAsMention() + ", you do not have the `Manage Messages` permission!", null);
                            }
                            break;
                        case "list":
                            int size = quotes.getQuotes().size();
                            List<String> quotesList = new ArrayList<>();
                            List<String> pages = new ArrayList<>();
                            String pageNumber = (rawSplit.length == 3 ? rawSplit[2] : null);

                            if (size != 0) {
                                for (int i = 0; i < size; i++) {
                                    String quote = quotes.getQuotes().get(i);
                                    quotesList.add(String.valueOf(i+1) + ". " + quote);
                                }
                                try {
                                    if (pageNumber == null || Integer.valueOf(pageNumber) == 1) {
                                        if (quotesList.size() <= 10) {
                                            channel.sendMessageAsync("```md\n" + StringUtils.join(quotesList, "\n") + "```", null);
                                        } else {
                                            for (int i = 0; i < quotesList.size(); i += 10) {
                                                try {
                                                    pages.add(StringUtils.join(quotesList.subList(i, i + 10), "\n"));
                                                } catch (IndexOutOfBoundsException e) {
                                                    pages.add(StringUtils.join(quotesList.subList(i, quotesList.size()), "\n"));
                                                }
                                            }
                                            channel.sendMessageAsync("```md\n" + pages.get(0) + "\n\n[Page](1" + "/" + pages.size() + ")" + "```", null);
                                        }
                                    } else {
                                        if (quotesList.size() <= 10) {
                                            channel.sendMessageAsync("There are no other pages!", null);
                                        } else {
                                            for (int i = 0; i < quotesList.size(); i += 10) {
                                                try {
                                                    pages.add(StringUtils.join(quotesList.subList(i, i + 10), "\n"));
                                                } catch (IndexOutOfBoundsException e) {
                                                    pages.add(StringUtils.join(quotesList.subList(i, quotesList.size()), "\n"));
                                                }
                                            }
                                            if (Integer.valueOf(pageNumber) > pages.size()) {
                                                channel.sendMessageAsync("Specified page does not exist!", null);
                                            } else {
                                                channel.sendMessageAsync("```md\n" + pages.get(Integer.valueOf(pageNumber) - 1) + "\n\n[Page](" + pageNumber + "/" + pages.size() + ")" + "```", null);
                                            }
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    channel.sendMessageAsync("\"" + pageNumber + "\" is not a number!", null);
                                }
                            } else {
                                channel.sendMessageAsync("There are no quotes to list!", null);
                            }
                            break;
                    }
                }
            });
}
