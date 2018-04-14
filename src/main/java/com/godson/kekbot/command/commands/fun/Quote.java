package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.settings.Settings;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.concurrent.TimeUnit;

public class Quote extends Command {

    public Quote() {
        name = "quote";
        aliases = new String[]{"q"};
        description = "Grabs a random command from a list of quotes made in a server.";
        usage.add("quote");
        usage.add("quote <quote number>");
        usage.add("quote add <quote>");
        usage.add("quote remove <quote number>");
        usage.add("quote list");
        category = new Category("Fun");
        extendedDescription = "Note: Adding and removing quotes requires the \"Manage Messages\" permission.";
        exDescPos = ExtendedPosition.AFTER;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        MessageChannel channel = event.getChannel();
        Guild guild = event.getGuild();
        Settings settings = Settings.getSettings(guild);
        if (event.getArgs().length == 0) {
            if (settings.getQuotes().getList().isEmpty()) {
                channel.sendMessage("You have no quotes!").queue();
            } else channel.sendMessage(settings.getQuotes().quote()).queue();
        } else {
            switch (event.getArgs()[0]) {
                case "add":
                    if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                        if (event.getArgs().length > 1) {
                            settings.getQuotes().addQuote(event.combineArgs(1));
                            settings.save();
                            channel.sendMessage("Successfully added quote! :thumbsup:").queue();
                        } else {
                            Questionnaire.newQuestionnaire(event)
                                    .addQuestion("Enter your quote here:", QuestionType.STRING)
                                    .execute(results -> {
                                        settings.getQuotes().addQuote(results.getAnswer(0).toString());
                                        settings.save();
                                        channel.sendMessage("Successfully added quote! :thumbsup:").queue();
                                    });
                        }
                    } else channel.sendMessage(KekBot.respond(Action.NOPERM_USER, "`Manage Messages`")).queue();
                    break;
                case "remove":
                    if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                        if (event.getArgs().length > 1) {
                            try {
                                int quoteNumber = Integer.valueOf(event.getArgs()[1]);
                                if (settings.getQuotes().getList().size() >= quoteNumber) {
                                    String quote = settings.getQuotes().getQuote(quoteNumber - 1);
                                    settings.getQuotes().removeQuote(quoteNumber - 1);
                                    settings.save();
                                    channel.sendMessage("Successfully removed quote: **" + quote + "**.").queue();
                                }
                            } catch (NumberFormatException e) {
                                channel.sendMessage("\"" + event.getArgs()[1] + "\" is not a number!").queue();
                            }
                        } else channel.sendMessage("No quote specified.").queue();
                    } else channel.sendMessage(KekBot.respond(Action.NOPERM_USER, "`Manage Messages`")).queue();
                    break;
                case "list":
                    int size = settings.getQuotes().getList().size();

                    if (size != 0) {
                        Paginator.Builder builder = new Paginator.Builder();
                        for (int i = 0; i < size; i++) {
                            String quote = settings.getQuotes().getList().get(i);
                            builder.addItems(quote.length() > 200 ? quote.substring(0, 200) + "..." : quote);
                        }

                        builder.setText("Here are your quotes:")
                                .setEventWaiter(KekBot.waiter)
                                .setColor(event.getGuild().getSelfMember().getColor())
                                .setItemsPerPage(10)
                                .waitOnSinglePage(true)
                                .showPageNumbers(true)
                                .useNumberedItems(true)
                                .setTimeout(5, TimeUnit.MINUTES)
                                .setUsers(event.getAuthor());

                        builder.build().display(event.getChannel());
                    } else {
                        channel.sendMessage("There are no quotes to list!").queue();
                    }
                    break;
                default:
                    int toGet;
                    try {
                        toGet = Integer.valueOf(event.getArgs()[0]) - 1;
                        if (settings.getQuotes().getList().size() > toGet && toGet >= 0) {
                            channel.sendMessage(settings.getQuotes().getQuote(toGet)).queue();
                        } else channel.sendMessage("\"Here, let me just get a quote that doesn't exist... Oh, wait...\" ~You").queue();
                    } catch (NumberFormatException e) {
                        channel.sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + event.getArgs()[0]) + "`").queue();
                    }

            }
        }
    }
}
