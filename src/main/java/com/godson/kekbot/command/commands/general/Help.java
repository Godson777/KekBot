package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.menu.EmbedPaginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class Help extends Command {

    public Help() {
        name = "help";
        description = "Sends you this message. Can also provide with more info on a specified command.";
        usage.add("help");
        usage.add("help {command}");
        category = new Command.Category("General");
        commandState = CommandState.BOTH;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        String[] args = event.getArgs();
        if (args.length == 0) {
            sendHelp(event);
        } else {
            sendCommandHelp(event, args[0]);
        }
    }

    private void sendHelp(CommandEvent event) {
        List<Category> categories = event.getClient().getCommands().stream().map(Command::getCategory).distinct().sorted(Comparator.comparing(Category::getName)).collect(Collectors.toList());
        EmbedPaginator.Builder builder = new EmbedPaginator.Builder();
        builder.addUsers(event.getAuthor());
        builder.setEventWaiter(KekBot.waiter);
        builder.setFinalAction(m -> m.clearReactions().queue());
        builder.waitOnSinglePage(true);
        builder.showPageNumbers(true);
        builder.wrapPageEnds(true);

        categories.forEach(c -> {
            if (c.getName().equalsIgnoreCase("bot owner") && !event.isBotOwner()) return;
            if (c.getName().equalsIgnoreCase("bot admin") && !event.isBotAdmin()) return;
            if (c.getName().equalsIgnoreCase("test")) return;
            if (c.getName().equals("unassigned")) return;
            List<Command> commands = new ArrayList<>(event.getClient().getCommands()).stream().filter(cmd -> cmd.getCategory().equals(c)).sorted(Comparator.comparing(Command::getName)).collect(Collectors.toList());
            for (int i = 0; i < commands.size(); i += 10) {
                List<Command> currentPage = commands.subList(i, (i + 10 < commands.size() ? i + 10 : commands.size()));
                EmbedBuilder eBuilder = new EmbedBuilder();
                eBuilder.setTitle(c.getName());
                eBuilder.setDescription(StringUtils.join(currentPage.stream().map(cmd -> event.getPrefix() + cmd.getName() + " - " + cmd.getDescription()).collect(Collectors.toList()), "\n"));
                eBuilder.setFooter("KekBot v" + KekBot.version, null);
                eBuilder.setAuthor("KekBot, your friendly meme-based bot!", null, event.getSelfUser().getAvatarUrl());
                builder.addItems(eBuilder.build());
            }
        });

        builder.build().display(event.getChannel());
    }

    private void sendCommandHelp(CommandEvent event, String commandName) {
        boolean found;
        Optional<Command> command = event.getClient().getCommands().stream().filter(c -> c.getName().equalsIgnoreCase(commandName)).findAny();
        //Check if the command exists, if it does, check if they have perms to view the command (or the command is hidden).
        if (command.isPresent()) {
            if (command.get().getCategory().getName().equalsIgnoreCase("bot owner")) found = event.isBotOwner();
            else if (command.get().getCategory().getName().equalsIgnoreCase("bot admin")) found = event.isBotAdmin();
            else found = !command.get().getCategory().getName().equalsIgnoreCase("unassigned");
        } else found = false;


        if (found) {
            if (event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS))
                event.getChannel().sendMessageEmbeds(getCommandHelp(event, command.get())).queue();
            else
                event.getChannel().sendMessage(getCommandHelpPlain(event, command.get())).queue();
        } else event.getChannel().sendMessage(event.getString("command.general.help.commandnotfound")).queue();
    }

    private MessageEmbed getCommandHelp(CommandEvent event, Command command) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.addField("Command:", command.getName(), true);
        builder.addField("Category:", command.getCategory().getName(), true);
        if (command.getAliases().length > 0) builder.addField("Aliases:", StringUtils.join(command.getAliases(), ", "), false);
        builder.addField("Description:", command.getDescription(), false);
        if (command.getExtendedDescription() != null && command.getExDescriptionPosition().equals(ExtendedPosition.BEFORE))
            builder.addField("", command.getExtendedDescription().replaceAll("\\{p}", Matcher.quoteReplacement(event.getPrefix())), false);
        builder.addField("Usage (<> Required, {} Optional):", StringUtils.join(command.getUsage().stream().map(usage -> event.getPrefix() + usage).collect(Collectors.toList()), "\n"), false);
        if (command.getExtendedDescription() != null && command.getExDescriptionPosition().equals(ExtendedPosition.AFTER))
            builder.addField("", command.getExtendedDescription().replaceAll("\\{p}", Matcher.quoteReplacement(event.getPrefix())), false);
        builder.setFooter("KekBot v" + KekBot.version, null);
        builder.setAuthor("KekBot, your friendly meme-based bot!", null, event.getSelfUser().getAvatarUrl());
        return builder.build();
    }


    private String getCommandHelpPlain(CommandEvent event, Command command) {
        StringBuilder builder = new StringBuilder();
        builder.append("```\n")
                .append("Command:\n")
                .append(command.getName()).append("\n\n")
                .append("Category:\n")
                .append(command.getCategory().getName()).append("\n\n");
        if (command.getAliases().length > 0) builder.append("Aliases: ").append(StringUtils.join(command.getAliases(), ", ")).append("\n\n");
        builder.append("Description:\n")
                .append(command.getDescription()).append("\n\n");
        if (command.getExtendedDescription() != null && command.getExDescriptionPosition().equals(ExtendedPosition.BEFORE))
            builder.append(command.getExtendedDescription().replaceAll("\\{p}", Matcher.quoteReplacement(event.getPrefix())));
        builder.append("Usage (<> Required, {} Optional):\n")
                .append(StringUtils.join(command.getUsage().stream().map(usage -> event.getPrefix() + usage).collect(Collectors.toList()), "\n"));
        if (command.getExtendedDescription() != null && command.getExDescriptionPosition().equals(ExtendedPosition.AFTER))
            builder.append(command.getExtendedDescription().replaceAll("\\{p}", Matcher.quoteReplacement(event.getPrefix())));
        builder.append("\n\n--\n\n")
                .append("Pro Tip: Enable \"Embed Links\" for me, so I can send cleaner styled messages for commands like this one!\n\n")
                .append("--\n\n")
                .append("KekBot v").append(KekBot.version).append("\n")
                .append("```");
        return builder.toString();
    }
}
