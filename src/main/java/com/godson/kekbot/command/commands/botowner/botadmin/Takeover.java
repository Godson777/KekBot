package com.godson.kekbot.command.commands.botowner.botadmin;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.menu.PagedSelectionMenu;
import com.godson.kekbot.objects.TakeoverManager;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.responses.Action;
import com.sun.javaws.exceptions.InvalidArgumentException;
import net.dv8tion.jda.core.entities.Message;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Takeover extends Command {

    private final TakeoverManager manager;

    public Takeover(TakeoverManager manager) {
        name = "takeover";
        category = new Category("Bot Owner");
        this.manager = manager;
        commandPermission = CommandPermission.OWNER;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage("Not enough arguments.").queue();
            return;
        }
        switch (event.getArgs()[0]) {
            case "create":
                //We're preparing a new Takeover object for our use.
                TakeoverManager.Takeover takeover = new TakeoverManager.Takeover();
                //Start the questionnaire with the first question.
                setTakeoverName(event, takeover);
                break;
            case "start":
                if (event.getArgs().length < 2) {
                    event.getChannel().sendMessage("No takeover specified.").queue();
                    return;
                }
                try {
                    if (manager.startTakeover(event.combineArgs(1))) event.getChannel().sendMessage("Takeover started.").queue();
                    else event.getChannel().sendMessage("Failed to start takeover.").queue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "reset":
                if (!manager.isTakeoverActive()) {
                    event.getChannel().sendMessage("There is no active takeover.").queue();
                    return;
                }

                manager.deactivateTakeover();
                event.getChannel().sendMessage("Takeover has ended.").queue();
                break;
            case "edit":
                if (event.getArgs().length < 2) {
                    event.getChannel().sendMessage("No takeover specified.").queue();
                    return;
                }

                TakeoverManager.Takeover toEdit = manager.getTakeover(event.combineArgs(1));

                if (toEdit == null) {
                    event.getChannel().sendMessage("No takeover with that name found.").queue();
                    return;
                }

                Questionnaire.newQuestionnaire(event)
                        .addChoiceQuestion("What part of the takeover would you like to edit?", "name", "avatar", "games", "responses")
                        .execute(results -> {
                            switch (results.getAnswerAsType(0, String.class)) {
                                case "name":
                                    Questionnaire.newQuestionnaire(results)
                                            .addQuestion("Choose a name for the takeover.", QuestionType.STRING)
                                            .execute(results1 -> {
                                                String takeoverName = results1.getAnswerAsType(0, String.class);

                                                if (manager.hasTakeover(takeoverName)) {
                                                    event.getChannel().sendMessage("There is already a takeover with this name. Try again.").queue();
                                                    results1.reExecuteWithoutMessage();
                                                    return;
                                                }

                                                new File("takeovers/" + toEdit.getName()).renameTo(new File("takeovers/" + takeoverName));
                                                toEdit.setName(takeoverName);
                                                toEdit.save();
                                                editPrompt(event, results);
                                            });
                                    break;
                                case "avatar":
                                    Questionnaire.newQuestionnaire(results)
                                            .addQuestion("Send image file to use for avatar.", QuestionType.STRING)
                                            .execute(results1 -> {
                                                //Store the URL for now.
                                                String url = results1.getAnswerAsType(0, String.class);

                                                if (setImage(event, toEdit, results1, url)) return;

                                                toEdit.save();
                                                editPrompt(event, results);
                                            });
                                    break;
                                case "games":
                                    Questionnaire.newQuestionnaire(results)
                                            .addChoiceQuestion("Do you want to **add** or **remove** a game?", "add", "remove")
                                            .execute(results1 -> {
                                                switch (results1.getAnswerAsType(0, String.class)) {
                                                    case "add":
                                                        Questionnaire.newQuestionnaire(event)
                                                                .addQuestion("Enter a playing status.", QuestionType.STRING)
                                                                .execute(results2 -> {
                                                                    //Add the "game", then prompt if we want to add another.
                                                                    toEdit.getGames().add(results2.getAnswerAsType(0, String.class));
                                                                    Questionnaire.newQuestionnaire(results2)
                                                                            .addYesNoQuestion("Done. Add another?")
                                                                            .execute(results3 -> {
                                                                                //We want to add another.
                                                                                if (results3.getAnswerAsType(0, boolean.class)) results2.reExecute();
                                                                                else {
                                                                                    toEdit.save();
                                                                                    editPrompt(event, results);
                                                                                }
                                                                            });
                                                                });
                                                        break;
                                                    case "remove":
                                                        event.getClient().registerQuestionnaire(event.getChannel().getId(), event.getAuthor().getId());
                                                        PagedSelectionMenu.Builder builder = new PagedSelectionMenu.Builder();
                                                        builder.setEventWaiter(KekBot.waiter);
                                                        builder.setUsers(event.getAuthor());
                                                        builder.setItemsPerPage(10);
                                                        builder.addChoices(toEdit.getGames().toArray(new String[toEdit.getGames().size()]));
                                                        builder.setSelectionAction((m, i) -> {
                                                            m.clearReactions().queue();
                                                            toEdit.getGames().remove(i-1);
                                                            toEdit.save();
                                                            Questionnaire.newQuestionnaire(results1)
                                                                    .addYesNoQuestion("Done. Remove another?")
                                                                    .execute(results2 -> {
                                                                        if (results2.getAnswerAsType(0, boolean.class)) builder.build().display(event.getChannel());
                                                                        else {
                                                                            event.getClient().unregisterQuestionnaire(event.getChannel().getId(), event.getAuthor().getId());
                                                                            editPrompt(event, results);
                                                                        }
                                                                    });
                                                        });
                                                        builder.setFinalAction(message -> {
                                                            message.clearReactions().queue();
                                                            toEdit.save();
                                                            event.getChannel().sendMessage("Exited.").queue();
                                                            event.getClient().unregisterQuestionnaire(event.getChannel().getId(), event.getAuthor().getId());
                                                        });
                                                        builder.build().display(event.getChannel());
                                                        break;
                                                }
                                            });
                                    break;
                                case "responses":
                                    Questionnaire.newQuestionnaire(results)
                                            .addQuestion("Enter the name of the action you're editing.", QuestionType.STRING)
                                            .addQuestion("Would you like to **add** or **remove** responses?", QuestionType.STRING)
                                            .execute(results1 -> {
                                                Action action = null;
                                                try {
                                                    action = Action.valueOf(results1.getAnswerAsType(0, String.class));
                                                } catch (IllegalArgumentException ignored) {
                                                    event.getChannel().sendMessage("No action found by that name.").queue();
                                                    results1.reExecuteWithoutMessage();
                                                    return;
                                                }

                                                switch (results1.getAnswerAsType(1, String.class)) {
                                                    case "add":
                                                        Action finalAction = action;
                                                        Questionnaire.newQuestionnaire(results1)
                                                                .addQuestion("Enter a response for " + action.name() + ".", QuestionType.STRING)
                                                                .execute(results2 -> {
                                                                    List<String> numberSlots = new ArrayList<String>();
                                                                    String response = results2.getAnswerAsType(0, String.class);
                                                                    for (int i = 0; i < finalAction.getBlanksNeeded(); i++) {
                                                                        numberSlots.add("{" + (i + 1) + "}");
                                                                    }
                                                                    int filled = StringUtils.countMatches(response, "{}");
                                                                    int blanks = finalAction.getBlanksNeeded();
                                                                    if (numberSlots.stream().anyMatch(response::contains) && response.contains("{}")) {
                                                                        event.getChannel().sendMessage("You cannot mix regular blanks with numeric blanks!").queue();
                                                                        results2.reExecuteWithoutMessage();
                                                                        return;
                                                                    } else if (numberSlots.stream().allMatch(response::contains)) {
                                                                        toEdit.addResponse(finalAction, response);
                                                                    } else if (numberSlots.stream().noneMatch(response::contains)) {
                                                                        if (filled == blanks) {
                                                                            toEdit.addResponse(finalAction, response);
                                                                        } else if (filled < blanks) {
                                                                            event.getChannel().sendMessage(String.format("Missing %s blanks.", blanks - filled)).queue();
                                                                            results2.reExecuteWithoutMessage();
                                                                            return;
                                                                        }
                                                                        else if (filled > blanks) {
                                                                            event.getChannel().sendMessage("Too many blanks. (Required: " + blanks + ". Filled: " + filled + ")").queue();
                                                                            results2.reExecuteWithoutMessage();
                                                                            return;
                                                                        }
                                                                    } else {
                                                                        event.getChannel().sendMessage(String.format("Not enough numeric blanks. (This action requires %s blanks to be filled.)", blanks)).queue();
                                                                        results2.reExecuteWithoutMessage();
                                                                        return;
                                                                    }

                                                                    toEdit.save();
                                                                    Questionnaire.newQuestionnaire(results2)
                                                                            .addYesNoQuestion("Done. Want to add another response for " + finalAction + "?")
                                                                            .execute(results3 -> {
                                                                                if (results3.getAnswerAsType(0, boolean.class)) {
                                                                                    event.getChannel().sendMessage("Enter a response for " + finalAction + ".").queue();
                                                                                    results2.reExecuteWithoutMessage();
                                                                                } else {
                                                                                    editPrompt(event, results);
                                                                                }
                                                                            });
                                                                });
                                                        break;
                                                    case "remove":
                                                        PagedSelectionMenu.Builder builder = new PagedSelectionMenu.Builder();
                                                        builder.setEventWaiter(KekBot.waiter);
                                                        builder.setUsers(event.getAuthor());
                                                        builder.setItemsPerPage(10);
                                                        builder.addChoices(toEdit.getResponses().get(action).toArray(new String[toEdit.getResponses().get(action).size()]));
                                                        Action finalAction1 = action;
                                                        builder.setSelectionAction((m, i) -> {
                                                            m.clearReactions().queue();
                                                            toEdit.getResponses().get(finalAction1).remove(i-1);
                                                            toEdit.save();
                                                            Questionnaire.newQuestionnaire(results1)
                                                                    .addYesNoQuestion("Done. Remove another?")
                                                                    .execute(results2 -> {
                                                                        if (results2.getAnswerAsType(0, boolean.class)) builder.build().display(event.getChannel());
                                                                        else {
                                                                            event.getClient().unregisterQuestionnaire(event.getChannel().getId(), event.getAuthor().getId());
                                                                            editPrompt(event, results);
                                                                        }
                                                                    });
                                                        });
                                                        builder.setFinalAction(message -> {
                                                            message.clearReactions().queue();
                                                            toEdit.save();
                                                            event.getChannel().sendMessage("Exited.").queue();
                                                            event.getClient().unregisterQuestionnaire(event.getChannel().getId(), event.getAuthor().getId());
                                                        });
                                                        builder.build().display(event.getChannel());
                                                        break;
                                                }
                                            });
                            }
                        });
        }
    }

    private void editPrompt(CommandEvent event, Questionnaire.Results results) {
        Questionnaire.newQuestionnaire(event)
                .addYesNoQuestion("Done. Would you like to edit something else?")
                .execute(results2 -> {
                    if (results2.getAnswerAsType(0, boolean.class)) results.reExecute();
                    else event.getChannel().sendMessage("Exited.").queue();
                });
    }

    private boolean setImage(CommandEvent event, TakeoverManager.Takeover takeover, Questionnaire.Results results, String url) {
        try {
            //Check if URL is actually an image.
            URL image = new URL(url);
            URLConnection connection = image.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();
            BufferedImage check = ImageIO.read(connection.getInputStream());
            if (check == null) {
                event.getChannel().sendMessage("No image found.").queue();
                results.reExecute();
                return true;
            }

            //Save the image as a file, and set the takeover's file name.
            String avaFile = FilenameUtils.getName(image.getPath());
            ImageIO.write(check, "png", new File("takeovers/" + takeover.getName() + "/" + avaFile));
            if (takeover.getAvaFile() != null) new File("takeovers/" + takeover.getName() + "/" + avaFile).delete();
            takeover.setAvaFile(avaFile);
        } catch (MalformedURLException | UnknownHostException | IllegalArgumentException | FileNotFoundException e) {
            event.getChannel().sendMessage("`" + url + "`" + " is not a valid URL.").queue();
            results.reExecute();
        } catch (SSLHandshakeException | SocketException e) {
            event.getChannel().sendMessage("Unable to connect to URL.").queue();
            results.reExecute();
        } catch (IOException e) {
            e.printStackTrace();
            event.getChannel().sendMessage("Unknown error, try again.").queue();
            results.reExecute();
        }
        return false;
    }

    private void setTakeoverName(CommandEvent event, TakeoverManager.Takeover takeover) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion("Choose a name for the takeover.", QuestionType.STRING)
                .execute(results -> {
                    String takeoverName = results.getAnswerAsType(0, String.class);

                    if (manager.hasTakeover(takeoverName)) {
                        TakeoverManager.Takeover existing = manager.getTakeover(takeoverName);
                        if (!existing.isUsable()) {
                            Questionnaire.newQuestionnaire(event)
                                    .addYesNoQuestion("This takeover already exists, but has not been completed. Would you like to finish it?")
                                    .execute(results1 -> {
                                        if (results1.getAnswerAsType(0, boolean.class)) {
                                            if (existing.getAvaFile() == null) {
                                                setTakeoverImage(event, existing);
                                                return;
                                            }
                                            if (existing.getGames().isEmpty()) {
                                                setTakeoverGames(event, existing);
                                                return;
                                            }
                                            if (existing.getResponses().isEmpty() || existing.isUsable()) {
                                                setTakeoverResponses(event, existing);
                                            }
                                        } else event.getChannel().sendMessage("Exited.").queue();
                                    });
                            return;
                        }
                    }
                    //Set the takeover's name.
                    takeover.setName(takeoverName);
                    takeover.save();
                    manager.addTakeover(takeover);
                    //Create a new folder for the takeover.
                    new File("takeovers/" + takeover.getName()).mkdir();
                    setTakeoverImage(event, takeover);
                });
    }

    private void setTakeoverImage(CommandEvent event, TakeoverManager.Takeover takeover) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion("Send image file to use for avatar.", QuestionType.STRING)
                .execute(results -> {
                    //Store the URL for now.
                    String url = results.getAnswerAsType(0, String.class);

                    if (setImage(event, takeover, results, url)) return;

                    takeover.save();
                    setTakeoverGames(event, takeover);
                });
    }

    private void setTakeoverGames(CommandEvent event, TakeoverManager.Takeover takeover) {
        Questionnaire.newQuestionnaire(event)
                .addQuestion("Enter a playing status.", QuestionType.STRING)
                .execute(results -> {
                    //Add the "game", then prompt if we want to add another.
                    takeover.getGames().add(results.getAnswerAsType(0, String.class));
                    Questionnaire.newQuestionnaire(results)
                            .addYesNoQuestion("Done. Add another?")
                            .execute(results1 -> {
                                //We want to add another.
                                if (results1.getAnswerAsType(0, boolean.class)) results.reExecute();
                                    //We're done, so we're moving on to the final step, responses.
                                else {
                                    takeover.save();
                                    setTakeoverResponses(event, takeover);
                                }
                            });
                });
    }

    private void setTakeoverResponses(CommandEvent event, TakeoverManager.Takeover takeover) {
        //Prepare an iterator for all the actions we'll be adding values for.
        Iterator<Action> iterator = Arrays.asList(Action.values()).iterator();

        //Get the first action.
        final Action[] action = {iterator.next()};

        while (takeover.getResponses().containsKey(action[0]) && iterator.hasNext()) iterator.next();

        Questionnaire.newQuestionnaire(event)
                .addQuestion("Now, for responses. Let's start with a response for " + action[0], QuestionType.STRING)
                .execute(results -> {
                    List<String> numberSlots = new ArrayList<String>();
                    String response = results.getAnswerAsType(0, String.class);
                    for (int i = 0; i < action[0].getBlanksNeeded(); i++) {
                        numberSlots.add("{" + (i + 1) + "}");
                    }
                    int filled = StringUtils.countMatches(response, "{}");
                    int blanks = action[0].getBlanksNeeded();
                    if (numberSlots.stream().anyMatch(response::contains) && response.contains("{}")) {
                        event.getChannel().sendMessage("You cannot mix regular blanks with numeric blanks!").queue();
                        results.reExecuteWithoutMessage();
                        return;
                    } else if (numberSlots.stream().allMatch(response::contains)) {
                        takeover.addResponse(action[0], response);
                    } else if (numberSlots.stream().noneMatch(response::contains)) {
                        if (filled == blanks) {
                            takeover.addResponse(action[0], response);
                        } else if (filled < blanks) {
                            event.getChannel().sendMessage(String.format("Missing %s blanks.", blanks - filled)).queue();
                            results.reExecuteWithoutMessage();
                            return;
                        }
                        else if (filled > blanks) {
                            event.getChannel().sendMessage("Too many blanks. (Required: " + blanks + ". Filled: " + filled + ")").queue();
                            results.reExecuteWithoutMessage();
                            return;
                        }
                    } else {
                        event.getChannel().sendMessage(String.format("Not enough numeric blanks. (This action requires %s blanks to be filled.)", blanks)).queue();
                        results.reExecuteWithoutMessage();
                        return;
                    }


                    takeover.save();
                    Questionnaire.newQuestionnaire(results)
                            .addYesNoQuestion("Done. Want to add another response for " + action[0] + "?")
                            .execute(results1 -> {
                                if (results1.getAnswerAsType(0, boolean.class)) {
                                    event.getChannel().sendMessage("Enter a response for " + action[0] + ".").queue();
                                    results.reExecuteWithoutMessage();
                                }
                                else {
                                    if (iterator.hasNext()) {
                                        action[0] = iterator.next();
                                        event.getChannel().sendMessage("Enter a response for " + action[0] + ".").queue();
                                        results.reExecuteWithoutMessage();
                                    } else {
                                        takeover.save();
                                        event.getChannel().sendMessage("Successfully completed creation of takeover: `" + takeover.getName() + "`.").queue();
                                    }
                                }
                            });
                });
    }
}
