package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.settings.Settings;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;

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
            if (settings.getQuotes() == null || settings.getQuotes().getList().isEmpty()) {
                channel.sendMessage(event.getString("command.fun.quote.noquotes")).queue();
            } else {
                String quote = settings.getQuotes().quote();
                while (quote.length() > 2000) {
                    settings.getQuotes().getList().remove(quote);
                    settings.save();
                    if (settings.getQuotes().getList().size() > 0) {
                        quote = settings.getQuotes().quote();
                    } else {
                        channel.sendMessage(event.getString("command.fun.quote.noquotes")).queue();
                        return;
                    }
                }
                channel.sendMessage(quote).queue();
            }
        } else {
            switch (event.getArgs()[0]) {
                case "add":
                    if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                        if (event.getArgs().length > 1) {
                            settings.getQuotes().addQuote(event.combineArgs(1));
                            settings.save();
                            channel.sendMessage(event.getString("command.fun.quote.addsuccess")).queue();
                        } else {
                            Questionnaire.newQuestionnaire(event)
                                    .addQuestion(event.getString("command.fun.quote.add"), QuestionType.STRING)
                                    .execute(results -> {
                                        settings.getQuotes().addQuote(results.getAnswer(0).toString());
                                        settings.save();
                                        channel.sendMessage(event.getString("command.fun.quote.addsuccess")).queue();
                                    });
                        }
                    } else channel.sendMessage(KekBot.respond(Action.NOPERM_USER, event.getLocale(), "`Manage Messages`")).queue();
                    break;
                case "remove":
                    if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                        if (event.getArgs().length > 1) {
                            try {
                                int quoteNumber = Integer.parseInt(event.getArgs()[1]);
                                if (settings.getQuotes().getList().size() >= quoteNumber) {
                                    String quote = settings.getQuotes().getQuote(quoteNumber - 1);
                                    settings.getQuotes().removeQuote(quoteNumber - 1);
                                    settings.save();
                                    channel.sendMessage(event.getString("command.fun.quote.removesuccess", "`" + quote + "`")).queue();
                                }
                            } catch (NumberFormatException e) {
                                channel.sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), "`" + event.getArgs()[1] + "`")).queue();
                            }
                        } else channel.sendMessage(event.getString("command.fun.quote.removenoargs")).queue();
                    } else channel.sendMessage(KekBot.respond(Action.NOPERM_USER, event.getLocale(), "`Manage Messages`")).queue();
                    break;
                case "list":
                    int size = (settings.getQuotes() == null ? 0 : settings.getQuotes().getList().size());

                    if (size != 0) {
                        Paginator.Builder builder = new Paginator.Builder();
                        for (int i = 0; i < size; i++) {
                            String quote = settings.getQuotes().getList().get(i);
                            builder.addItems(quote.length() > 200 ? quote.substring(0, 200) + "..." : quote);
                        }

                        builder.setText(event.getString("command.fun.quote.list"))
                                .setEventWaiter(KekBot.waiter)
                                .setColor(event.getGuild().getSelfMember().getColor())
                                .setItemsPerPage(10)
                                .waitOnSinglePage(true)
                                .showPageNumbers(true)
                                .useNumberedItems(true)
                                .wrapPageEnds(true)
                                .setTimeout(5, TimeUnit.MINUTES)
                                .setUsers(event.getAuthor());

                        builder.build().display(event.getChannel());
                    } else {
                        channel.sendMessage(event.getString("command.fun.quote.noquotes")).queue();
                    }
                    break;
                default:
                    int toGet;
                    try {
                        toGet = Integer.parseInt(event.getArgs()[0]) - 1;
                        if (settings.getQuotes().getList().size() > toGet && toGet >= 0) {
                            String quote = settings.getQuotes().getQuote(toGet);
                            while (quote.length() > 2000) {
                                settings.getQuotes().getList().remove(toGet);
                                settings.save();
                                if (settings.getQuotes().getList().size() > 0) {
                                    if (toGet > settings.getQuotes().getList().size()) quote = settings.getQuotes().getQuote(toGet);
                                    else quote = settings.getQuotes().getQuote(settings.getQuotes().getList().size()-1);
                                } else {
                                    channel.sendMessage(event.getString("command.fun.quote.noquotes")).queue();
                                    return;
                                }
                            }
                            channel.sendMessage(quote).queue();
                        } else channel.sendMessage("\"Here, let me just get a quote that doesn't exist... Oh, wait...\" ~You").queue();
                    } catch (NumberFormatException e) {
                        channel.sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), "`" + event.getArgs()[0]) + "`").queue();
                    }

            }
        }
    }
}
