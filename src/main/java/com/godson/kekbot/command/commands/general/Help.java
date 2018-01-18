package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.core.entities.ChannelType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            sendHelp(event, 0);
        } else {
            if (event.isFromType(ChannelType.PRIVATE)) {
                try {
                    int page = Integer.valueOf(args[0]);
                    sendHelp(event, page -1);
                } catch (NumberFormatException e) {
                    sendCommandHelp(event, args[0]);
                }
            } else {
                sendCommandHelp(event, args[0]);
            }
        }
    }

    private void sendHelp(CommandEvent event, int page) {
        event.getAuthor().openPrivateChannel().queue(c -> c.sendMessage(getHelp(event, page)).queue(m -> {
            if (!event.isFromType(ChannelType.PRIVATE)) event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Alright, check your DMs! :thumbsup:").queue();
        }, f -> event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Huh, I can't slide into your DMs if you're blocking DMs.").queue()),
                f -> event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Huh, I can't open DMs with you. I won't be able to send you the help you need.").queue());
    }

    private String getHelp(CommandEvent event, int page) {
        final int max = 25;
        List<String> help = new ArrayList<>();
        help.add("# KekBot's default prefix for commands is \"$\". However, the server you're on might have it use a different prefix. If you're unsure, feel free to go a server and say \"@KekBot prefix\"");
        help.add("# To add me to your server, send me an invite link!\n");

        List<Category> categories = event.getClient().getCommands().stream().map(Command::getCategory).distinct().sorted(Comparator.comparing(Category::getName)).filter(Objects::nonNull).collect(Collectors.toList());
        categories.forEach(c -> {
            if (c.getName().equalsIgnoreCase("bot owner") && !event.isBotOwner()) return;
            if (c.getName().equals("unassigned")) return;
            help.add("#" + c.getName());
            event.getClient().getCommands().stream()
                    .filter(command -> command.getCategory().equals(c))
                    .sorted(Comparator.comparing(Command::getName))
                    .forEachOrdered(command -> help.add("[$" + command.getName() + (aliases.length != 0 ? " | " + StringUtils.join(aliases, " | ") : "") + "](" + command.getDescription() + ")"));
            help.add("");
        });

        try {
            if ((page * max) > help.size() || (page * max) < 0) return "Specified page doesn't exist!";
            else return "```md\n" + StringUtils.join(help.subList((page * max), ((page + 1) * max)), "\n") +
                    (help.size() > max ? "\n\nPage " + (page + 1) + "/" + (help.size() / max + 1) +
                            (page == 0 ? "\n# Type \"help <number>\" to view that page!" : "") : "") + "```";
        } catch (IndexOutOfBoundsException e) {
            return "```md\n" + StringUtils.join(help.subList((page * max), help.size()), "\n") +
                    (help.size() > max ? "\n\nPage " + (page + 1) + "/" + (help.size() / max + 1) : "") + "```";
        }
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
            event.getChannel().sendMessage(getCommandHelp(event, command.get())).queue();
        } else event.getChannel().sendMessage("Command not found.").queue();
    }

    private String getCommandHelp(CommandEvent event, Command command) {
        final String[] help = {("```md\n[Command](" + command.getName() + ")" +
                (command.getAliases().length != 0 ? "\n\n[Aliases](" + StringUtils.join(aliases, ", ") + ")" : "") +
                "\n\n[Category](" + command.getCategory().getName() + ")" +
                "\n\n[Description](" + command.getDescription() + ")" +
                (command.getExtendedDescription() != null && command.getExDescriptionPosition().equals(ExtendedPosition.BEFORE) ?
                        "\n" + command.getExtendedDescription().replaceAll("\\{p}", Matcher.quoteReplacement(event.getPrefix())) : "") +
                "\n\n#Usage ( Paramaters (<> Required, {} Optional) ):" +
                "\n")};
        command.getUsage().forEach(s -> help[0] += event.getClient().getPrefix(event.getGuild().getId()) + s + "\n");
        help[0] += (command.getExtendedDescription() != null && command.getExDescriptionPosition().equals(ExtendedPosition.AFTER) ?
                command.getExtendedDescription().replaceAll("\\{p}", Matcher.quoteReplacement(event.getPrefix())) :  "");
        help[0] += "```";
        return help[0];
    }
}
