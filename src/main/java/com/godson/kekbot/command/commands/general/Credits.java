package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.settings.Config;
import com.jagrosh.jdautilities.menu.Paginator;

public class Credits extends Command {

    public Credits() {
        name = "credits";
        description = "Shows everyone who helped in the making of KekBot, as well as KekBot's Patreon supporters.";
        usage.add("credits");
        usage.add("credits patreon");
        category = new Category("General");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length == 0) event.getChannel().sendMessage("```md" +
                "\nCredits:\n\n# Coded By: #\n" + KekBot.jda.getUserById("99405418077364224").getName() +
                "\n\n# Memes Supplied By: #\n" + KekBot.jda.getUserById("159671787683184640").getName() +
                "\n" + KekBot.jda.getUserById("194197898584391680").getName() +
                "\n" + KekBot.jda.getUserById("174713102628028416").getName() +
                "\n" + KekBot.jda.getUserById("181569245253992448").getName() +
                "\n\n# Special thanks to: #" +
                "\n" + KekBot.jda.getUserById("240128376105336832").getName() + " for making the TopKek drawing, and a few other assets for profiles." +
                "\n" + KekBot.jda.getUserById("255472639261409281").getName() + " for making some assets for shops." +
                "\nJDA Team for making JDA in the first place." +
                "\nEveryone in the Discord4J and JDA servers for helping me with my stupid problems and putting up with me." +
                "\nTo view KekBot's patreon supporters, call `" + event.getPrefix() + "credits patreon`." + "```").queue();
        else if (event.getArgs()[0].equalsIgnoreCase("patreon")) {
                Paginator.Builder builder = new Paginator.Builder();
                Config config = Config.getConfig();
                builder.addItems(config.getPatrons().toArray(new String[config.getPatrons().size()]));
                builder.showPageNumbers(true);
                builder.setEventWaiter(KekBot.waiter);
                builder.addUsers(event.getAuthor());
                builder.waitOnSinglePage(true);
                builder.setText("Shoutouts to all these people. (Shoutouts to Simpleflips too.)");
                builder.build().display(event.getChannel());
            }
    }
}
