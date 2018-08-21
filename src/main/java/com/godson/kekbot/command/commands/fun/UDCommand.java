package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.menu.EmbedPaginator;
import com.godson.kekbot.objects.UDictionary;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;

public class UDCommand extends Command {

    public UDCommand() {
        name = "ud";
        aliases = new String[]{"urban"};
        description = "Gets a definition of something from Urban Dictionary";
        usage.add("ud <term>");
        category = new Category("Fun");
    }

    private final String THUMBUP = "\uD83D\uDC4D";
    private final String THUMBDOWN = "\uD83D\uDC4E";

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length > 0) {
            UDictionary results = GSONUtils.getUDResults(event.combineArgs().replace(" ", "+"));
            //Random random = new Random();
            if (!results.getDefinitions().isEmpty()) {
                EmbedPaginator.Builder pBuilder = new EmbedPaginator.Builder();

                for (UDictionary.Definition definition : results.getDefinitions()) {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.addField("Term:", definition.getWord(), false);
                    builder.addField("Definition:", definition.getDefinition().length() > 1021 ? definition.getDefinition().substring(0, 1021) + "..." : definition.getDefinition(), false);
                    builder.addField("Examples:", definition.getExample().length() > 1021 ? definition.getExample().substring(0, 1021) + "..." : definition.getExample(), false);
                    builder.addField("Link:", definition.getPermalink(), false);
                    builder.addField("", THUMBUP + " " + definition.getThumbsUp(), true);
                    builder.addBlankField(true);
                    builder.addField("", THUMBDOWN + " " + definition.getThumbsDown(), true);
                    pBuilder.addItems(builder.build());
                }
                pBuilder.setEventWaiter(KekBot.waiter);
                pBuilder.setUsers(event.getAuthor());
                pBuilder.setFinalAction(m -> m.clearReactions().queue());
                pBuilder.build().display(event.getChannel());
            } else {
                event.getChannel().sendMessage(event.getString("command.fun.ud.nodefinitions")).queue();
            }
        } else {
            event.getChannel().sendMessage(event.getString("command.fun.ud.noargs")).queue();
        }
    }
}
