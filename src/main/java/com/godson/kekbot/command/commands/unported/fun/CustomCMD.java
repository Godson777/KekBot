package com.godson.kekbot.command.commands.unported.fun;

import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.settings.CustomCommand;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class CustomCMD {
    /*public static Command customCMD = new Command("customcmd")
            .withCategory(CommandCategory.FUN)
            .withAliases("ccmd")
            .withDescription("Brings the user to the Custom Command Wizard.")
            .withUsage("{p}customcmd")
            .userRequiredPermissions(Permission.ADMINISTRATOR)
            .onExecuted(context -> {
                new Questionnaire(context)
                        .addChoiceQuestion("Welcome to the custom commands wizard! Would you like to `add` or `remove` a command? Or would you like to `list` this server's commands? (You can also say `cancel` at any time to exit the wizard.)", "add", "remove", "list")
                        .execute(results -> {
                            TextChannel channel = context.getTextChannel();
                            switch ((String) results.getAnswer(0)) {
                                case "add":
                                    CustomCommand command = new CustomCommand();
                                    new Questionnaire(results)
                                            .addChoiceQuestion("Great! Let's start by choosing your type of command. Is your command a `text` command, an `image` command, " +
                                                    "or a `mention` command? (You can also ask for `help`.)", "text", "image", "mention", "help")
                                            .execute(results1 -> {
                                                switch ((String) results1.getAnswer(0)) {
                                                    case "help":
                                                        channel.sendMessage("Command Types:\n" +
                                                                "`text`: A command that simply sends a piece of text that you specify.\n" +
                                                                "`image`: A command that sends a image that you specify.\n" +
                                                                "`mention`: A command that sends a specified piece of text, but is targetted at a mentioned user.").queue();
                                                        new Questionnaire(results1)
                                                                .addChoiceQuestion("Once you're ready to continue, type `continue`, or if you want to stop entirely, type `cancel`.", "continue")
                                                                .execute(results2 -> results1.reExecute());
                                                        break;
                                                    default:
                                                        new Questionnaire(results1)
                                                                .addQuestion("Alright, now let's get the name of your command. I'll let you know if it conflicts with an existing command.", QuestionType.STRING)
                                                                .execute(results2 -> {
                                                                    String commandName = (String) results2.getAnswer(0);
                                                                    if (commandName.contains(" ")) {
                                                                        channel.sendMessage("There can't be any spaces in your command name. Try again.").queue();
                                                                        results2.reExecuteWithoutMessage();
                                                                    } else if (!commandName.matches("(\\w+)")) {
                                                                        channel.sendMessage("Your command name can only consist of letters, numbers, and underscores. Try again.").queue();
                                                                        results2.reExecuteWithoutMessage();
                                                                    } else if (commandName.equalsIgnoreCase("list")) {
                                                                        channel.sendMessage("Sorry, I'm gonna have to ask you to *not* make your command using that name. Could you pick another one, please?").queue();
                                                                        results2.reExecuteWithoutMessage();
                                                                    } else if (commandName.length() > 30) {
                                                                        channel.sendMessage("That command name's too long, could you pick a *shorter* name?").queue();
                                                                        results2.reExecuteWithoutMessage();
                                                                    } else if (context.getRegistry().getCommands().stream().anyMatch(cmd -> cmd.getName().equalsIgnoreCase(commandName) || cmd.getAliases().contains(commandName.toLowerCase()))) {
                                                                        channel.sendMessage("That name conflicts with an existing command. Try again.").queue();
                                                                        results2.reExecuteWithoutMessage();
                                                                    } else if (context.getRegistry().getCustomCommands(context.getGuild()).size() >= 1 && context.getRegistry().getCustomCommands(context.getGuild()).stream().anyMatch(cmd -> cmd.getName().equalsIgnoreCase(commandName))) {
                                                                        channel.sendMessage("That name conflicts with another custom command. Try again.").queue();
                                                                        results2.reExecuteWithoutMessage();
                                                                    } else {
                                                                        command.setName(commandName);
                                                                        switch ((String) results1.getAnswer(0)) {
                                                                            case "text":
                                                                                command.setType(CustomCommand.TEXT);
                                                                                new Questionnaire(results2)
                                                                                        .addQuestion("Sweet, now all we need is the value of the command. " +
                                                                                                "For a `text` command, all you have to do is send the text this command will use when called.", QuestionType.STRING)
                                                                                        .execute(results3 -> {
                                                                                            command.setValue(results3.getAnswer(0).toString());
                                                                                            command.saveAndRegister(context);
                                                                                            channel.sendMessage("Alright! Your custom command is all set! You can use your new command by calling `" + KekBot.replacePrefix(context.getGuild(), "{p}") + commandName + "`!").queue();
                                                                                        });
                                                                                break;
                                                                            case "image":
                                                                                command.setType(CustomCommand.IMAGE);
                                                                                new Questionnaire(results2)
                                                                                        .addQuestion("Sweet, now all we need is the value of the command. " +
                                                                                                "For a `image` command, all you have to do is send a direct URL to the image this command will use when called.", QuestionType.STRING)
                                                                                        .execute(results3 -> {
                                                                                            try {
                                                                                                URL image = new URL(results3.getAnswer(0).toString());
                                                                                                URLConnection connection = image.openConnection();
                                                                                                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                                                                                                connection.connect();
                                                                                                BufferedImage check = ImageIO.read(connection.getInputStream());
                                                                                                if (check == null) {
                                                                                                    channel.sendMessage("Hm, I can't seem to find any image from this link. (It could be because I can't read the site correctly.) Try a different link.").queue();
                                                                                                    results3.reExecuteWithoutMessage();
                                                                                                } else {
                                                                                                    channel.sendMessage("Alright! Your custom command is all set! You can use your new command by calling `" + KekBot.replacePrefix(context.getGuild(), "{p}") + commandName + "`!").queue();
                                                                                                    command.setValue(results3.getAnswer(0).toString());
                                                                                                    command.saveAndRegister(context);
                                                                                                }
                                                                                            } catch (MalformedURLException | UnknownHostException | IllegalArgumentException | FileNotFoundException e) {
                                                                                                channel.sendMessage("That doesn't appear to be a valid URL, try a different one.").queue();
                                                                                                results3.reExecuteWithoutMessage();
                                                                                            } catch (SSLHandshakeException | SocketException e) {
                                                                                                channel.sendMessage("Hm, I can't seem to connect to this URL, try a different one.").queue();
                                                                                                results3.reExecuteWithoutMessage();
                                                                                            } catch (IOException e) {
                                                                                                e.printStackTrace();
                                                                                            }
                                                                                        });
                                                                                break;
                                                                            case "mention":
                                                                                command.setType(CustomCommand.MENTION);
                                                                                new Questionnaire(results2)
                                                                                        .addQuestion("Sweet, now all we need is the value of the command. " +
                                                                                                "For a `mention` command, you need to send the text this command will use when called, as well as a blank `{}` somewhere in the text. This is where the @mention will go.", QuestionType.STRING)
                                                                                        .execute(results3 -> {
                                                                                            String text = (String) results3.getAnswer(0);
                                                                                            int blanks = StringUtils.countMatches(text, "{}");
                                                                                            if (blanks == 1) {
                                                                                                channel.sendMessage("Alright! Your custom command is all set! You can use your new command by calling `" + KekBot.replacePrefix(context.getGuild(), "{p}") + commandName + "`!").queue();
                                                                                                command.setValue(text);
                                                                                                command.saveAndRegister(context);
                                                                                            } else {
                                                                                                if (blanks == 0) {
                                                                                                    channel.sendMessage("Since this is a `mention` command, you'll need to have a blank `{}` in your message. ").queue();
                                                                                                    results3.reExecute();
                                                                                                } else {
                                                                                                    channel.sendMessage("Woah, don't you think " + blanks + " blanks is a little overkill? I only asked you for one...").queue();
                                                                                                    results3.reExecute();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                        }
                                                                    }
                                                                });
                                                }
                                            });
                                    break;
                                case "remove":
                                    List<String> names = new ArrayList<>();
                                    List<Command> ccmds = context.getRegistry().getCustomCommands(context.getGuild());
                                    names.add("list");
                                    for (Command cmd : ccmds) {
                                        if (!(context.getGuild().getId().equals("221910104495095808") && (cmd.getName().equals("suggest") || cmd.getName().equals("addresponse") || cmd.getName().equals("suggestions") || cmd.getName().equals("test"))))
                                            names.add(cmd.getName());
                                    }
                                    if (names.size() > 0 && !(names.size() == 1 && names.get(0).equals("list"))) {
                                        new Questionnaire(results)
                                                .withoutRepeats()
                                                .withCustomErrorMessage("Hm, I can't seem to find that command. Try another one.")
                                                .addChoiceQuestion("Alright. Type in the name of the command you want to remove. If you don't know which command you want to remove, you can also `list` them.", names.toArray(new String[names.size()]))
                                                .execute(results1 -> {
                                                    names.remove("list");
                                                    String commandName = results1.getAnswer(0).toString();
                                                    if (commandName.equals("list")) {
                                                        String commands;
                                                        final int[] page = {0};
                                                        try {
                                                            commands = StringUtils.join(names.subList((page[0] * 15), ((page[0] + 1) * 15)), "\n");
                                                        } catch (IndexOutOfBoundsException e) {
                                                            commands = StringUtils.join(names.subList(0, names.size()), "\n");
                                                        }
                                                        new Questionnaire(results1)
                                                                .withoutRepeats()
                                                                .addChoiceQuestion("List of currently registered custom commands:" +
                                                                        "\n```" + commands + "```" +
                                                                        (names.size() > 15 ? "\nTo view the next page, type `next`, or to go back to a previous page, type `back`. (Page " + page[0] + 1 + "/" + names.size() / 15 + 1 + ")" : "") +
                                                                        "\nOnce you're ready to continue, type `continue`, or if you want to stop entirely, type `cancel`.", "continue", "next", "back")
                                                                .execute(results2 -> {
                                                                    switch (results2.getAnswer(0).toString()) {
                                                                        case "continue":
                                                                            results1.reExecute();
                                                                            break;
                                                                        case "next":
                                                                            if (((page[0] + 1) * 15) > names.size()) {
                                                                                channel.sendMessage("There is no next page!").queue();
                                                                                results2.reExecuteWithoutMessage();
                                                                            } else {
                                                                                ++page[0];
                                                                                results2.reExecute();
                                                                            }
                                                                            break;
                                                                        case "back":
                                                                            if (page[0] - 1 == -1) {
                                                                                channel.sendMessage("You're already at the beginning!").queue();
                                                                                results2.reExecuteWithoutMessage();
                                                                            } else {
                                                                                --page[0];
                                                                                results2.reExecute();
                                                                            }
                                                                            break;
                                                                    }
                                                                });
                                                    } else {
                                                        GSONUtils.getCCommand(commandName, context.getGuild()).remove(context);
                                                        channel.sendMessage("Done, the command has been removed.").queue();
                                                    }
                                                });
                                    } else {
                                        channel.sendMessage("There are no commands to remove. Your only options are to `add` a command, or `cancel`.").queue();
                                        results.reExecuteWithoutMessage();
                                    }
                                    break;
                                case "list":
                                    List<String> names1 = new ArrayList<>();
                                    List<Command> ccmds1 = context.getRegistry().getCustomCommands(context.getGuild());
                                    for (Command cmd : ccmds1) {
                                        if (!(context.getGuild().getId().equals("221910104495095808") && (cmd.getName().equals("suggest") || cmd.getName().equals("addresponse") || cmd.getName().equals("suggestions") || cmd.getName().equals("test"))))
                                            names1.add(cmd.getName());
                                    }
                                    if (names1.size() > 0) {
                                        String commands;
                                        final int[] page = {0};
                                        try {
                                            commands = StringUtils.join(names1.subList((page[0] * 15), ((page[0] + 1) * 15)), "\n");
                                        } catch (IndexOutOfBoundsException e) {
                                            commands = StringUtils.join(names1.subList(0, names1.size()), "\n");
                                        }
                                        new Questionnaire(results)
                                                .withoutRepeats()
                                                .addChoiceQuestion("List of currently registered custom commands:" +
                                                        "\n```" + commands + "```" +
                                                        (names1.size() > 15 ? "\nTo view the next page, type `next`, or to go back to a previous page, type `back`. (Page " + page[0] + 1 + "/" + names1.size() / 15 + 1 + ")" : "") +
                                                        "\nOnce you're ready to exit, type `exit`, or `cancel`.", "exit", "next", "back")
                                                .execute(results1 -> {
                                                    switch (results1.getAnswer(0).toString()) {
                                                        case "exit":
                                                            channel.sendMessage("Exited.").queue();
                                                            break;
                                                        case "next":
                                                            if (((page[0] + 1) * 15) > names1.size()) {
                                                                channel.sendMessage("There is no next page!").queue();
                                                                results1.reExecuteWithoutMessage();
                                                            } else {
                                                                ++page[0];
                                                                results1.reExecute();
                                                            }
                                                            break;
                                                        case "back":
                                                            if (page[0] - 1 == -1) {
                                                                channel.sendMessage("You're already at the beginning!").queue();
                                                                results1.reExecuteWithoutMessage();
                                                            } else {
                                                                --page[0];
                                                                results1.reExecute();
                                                            }
                                                            break;
                                                    }
                                                });
                                    } else {
                                        channel.sendMessage("There are no commands to list. Your only options are to `add` a command, or `cancel`.").queue();
                                        results.reExecuteWithoutMessage();
                                    }
                            }
                        });
            })
            .onFailure((context, reason) -> {
                if (reason.equals(FailureReason.AUTHOR_MISSING_PERMISSIONS)) {
                    context.getTextChannel().sendMessage(KekBot.respond(Action.NOPERM_USER, "`Administrator`")).queue();
                }
            });*/
}
