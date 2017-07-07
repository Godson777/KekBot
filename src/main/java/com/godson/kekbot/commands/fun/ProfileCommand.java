package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Profile.*;
import com.godson.kekbot.Questionaire.QuestionType;
import com.godson.kekbot.Questionaire.Questionnaire;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Utils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ProfileCommand {
    public static Command profile = new Command("profile")
            .withCategory(CommandCategory.FUN)
            .withDescription("Brings you to your profile.")
            .withUsage("{p}profile\n{p}profile {edit} <arguments> (arguments are specified by typing the command without them)\n{p}profile {@user}")
            .onExecuted(context -> {
                if (context.getArgs().length == 0) {
                    try {
                        context.getTextChannel().sendTyping().queue();
                        context.getTextChannel().sendFile(Profile.getProfile(context.getAuthor()).drawCard(context.getJDA()), "profile.png", null).queue();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (context.getArgs().length >= 1) {
                    if (context.getArgs()[0].equalsIgnoreCase("edit")) {
                        Profile profile = Profile.getProfile(context.getAuthor());
                        if (context.getArgs().length < 2) {
                            context.getTextChannel().sendMessage("Here are the available values you can edit:\n" +
                                    "\n**Title:** " + profile.getSubtitle() +
                                    "\n**Bio:** " + (profile.getBio() != null ? profile.getBio() : "No Bio Set.") +
                                    "\n**Tokens:** " + (profile.getTokens().size() > 0 ? profile.getTokens().size() + " " + (profile.getTokens().size() > 1 ? "Tokens" : "Token")  : "¯\\_(ツ)_/¯") +
                                    "\n**Backgrounds:** " + (profile.getBackgrounds().size() > 0 ? profile.getBackgrounds().size() + " " + (profile.getBackgrounds().size() > 1 ? "Backgrounds" : "Background") : "¯\\_(ツ)_/¯") +
                                    "\n**Playlists:** " + KekBot.replacePrefix(context.getGuild(), "Use {p}myplaylist to view your playlists.")).queue();
                        } else {
                            switch (context.getArgs()[1].toLowerCase()) {
                                case "title":
                                    if (context.getArgs().length >= 3) {
                                        String title = "";
                                        for (int i = 2; i < context.getArgs().length; i++) {
                                            title += context.getArgs()[i];
                                            if (i < context.getArgs().length - 1) title += " ";
                                        }
                                        if (title.length() <= 20) {
                                            String finalTitle = title;
                                            new Questionnaire(context)
                                                    .addYesNoQuestion("Are you sure you want to use the title: `" + title + "`?")
                                                    .execute(results -> {
                                                        if (results.getAnswer(0).toString().equalsIgnoreCase("Yes") || results.getAnswer(0).toString().equalsIgnoreCase("Y")) {
                                                            profile.setSubtitle(finalTitle);
                                                            profile.save();
                                                            context.getTextChannel().sendMessage("Title set!").queue();
                                                        } else {
                                                            context.getTextChannel().sendMessage("Cancelled.").queue();
                                                        }
                                                    });
                                        } else {
                                            context.getTextChannel().sendMessage("Your title is too long. Titles can only be 20 characters or fewer. Try something shorter.").queue();
                                        }
                                    } else {
                                        new Questionnaire(context)
                                                .addQuestion("Type the title you would like to use below. Or type `cancel` to exit.", QuestionType.STRING)
                                                .execute(results -> {
                                                    if (results.getAnswer(0).toString().length() <= 20) {
                                                        new Questionnaire(results)
                                                                .addYesNoQuestion("Are you sure you want to use the title: `" + results.getAnswer(0) + "`?")
                                                                .execute(results2 -> {
                                                                    if (results2.getAnswer(0).toString().equalsIgnoreCase("Yes") || results2.getAnswer(0).toString().equalsIgnoreCase("Y")) {
                                                                        profile.setSubtitle((String) results.getAnswer(0));
                                                                        profile.save();
                                                                        context.getTextChannel().sendMessage("Title set!").queue();
                                                                    } else {
                                                                        context.getTextChannel().sendMessage("Cancelled.").queue();
                                                                    }
                                                                });
                                                    } else {
                                                        context.getTextChannel().sendMessage("Your title is too long. Titles can only be 20 characters or fewer. Try something shorter, or type `cancel` to exit.").queue();
                                                        results.reExecuteWithoutMessage();
                                                    }
                                                });

                                    }
                                    break;
                                case "bio":
                                    if (context.getArgs().length >= 3) {
                                        String bio = "";
                                        for (int i = 2; i < context.getArgs().length; i++) {
                                            bio += context.getArgs()[i];
                                            if (i < context.getArgs().length - 1) bio += " ";
                                        }
                                        if (ProfileUtils.testBio(bio)) {
                                            String finalBio = bio;
                                            new Questionnaire(context)
                                                    .addYesNoQuestion("For your bio, you wrote: `" + bio + "` Is this correct?")
                                                    .execute(results -> {
                                                        if (results.getAnswer(0).toString().equalsIgnoreCase("Yes") || results.getAnswer(0).toString().equalsIgnoreCase("Y")) {
                                                            profile.setBio(finalBio);
                                                            profile.save();
                                                            context.getTextChannel().sendMessage("Bio set!").queue();
                                                        } else {
                                                            context.getTextChannel().sendMessage("Cancelled.").queue();
                                                        }
                                                    });
                                        } else {
                                            context.getTextChannel().sendMessage("That bio is too long, and will not fit in your profile card. Please try something shorter.").queue();
                                        }
                                    } else {
                                        new Questionnaire(context)
                                                .addQuestion("Type the title you would like to use below. Or type `cancel` to exit.", QuestionType.STRING)
                                                .execute(results -> {
                                                    if (ProfileUtils.testBio(results.getAnswer(0).toString())) {
                                                        new Questionnaire(results)
                                                                .addYesNoQuestion("For your bio, you wrote: `" + results.getAnswer(0) + "` Is this correct?")
                                                                .execute(results2 -> {
                                                                    if (results2.getAnswer(0).toString().equalsIgnoreCase("Yes") || results2.getAnswer(0).toString().equalsIgnoreCase("Y")) {
                                                                        profile.setBio((String) results.getAnswer(0));
                                                                        profile.save();
                                                                        context.getTextChannel().sendMessage("Bio set!").queue();
                                                                    } else {
                                                                        context.getTextChannel().sendMessage("Cancelled.").queue();
                                                                    }
                                                                });
                                                    } else {
                                                        context.getTextChannel().sendMessage("That bio is too long, and will not fit in your profile card. Please try something shorter, or type `cancel` to exit.").queue();
                                                        results.reExecuteWithoutMessage();
                                                    }
                                                });
                                    }
                                    break;
                                case "token":
                                case "tokens":
                                    List<Token> tokens = profile.getTokens();
                                    if (context.getArgs().length < 3) {
                                        context.getTextChannel().sendMessage("**Number of Tokens: ** " + tokens.size() +
                                                //"\n**Token Display: **" + KekBot.replacePrefix(context.getGuild(), "Use `{p}profile edit token display` to enter the token display editor.") +
                                                "\n**Equipped Token: **" + (profile.hasTokenEquipped() ? profile.getToken().getName() : "¯\\_(ツ)_/¯" +
                                                "\n**Available Options:** List, Equip")).queue();
                                    } else {
                                        switch (context.getArgs()[2]) {
                                            case "list":
                                                if (tokens.size() > 0) {
                                                    List<String> tokensList = new ArrayList<>();
                                                    for (int i = 0; i < tokens.size(); i++) {
                                                        Token token = tokens.get(i);
                                                        tokensList.add((i + 1) + ". " + token.getName());
                                                    }
                                                    int pageNumber;
                                                    if (context.getArgs().length < 4) pageNumber = 0;
                                                    else {
                                                        try {
                                                            pageNumber = Integer.valueOf(context.getArgs()[3]) - 1;
                                                        } catch (NumberFormatException e) {
                                                            context.getTextChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + context.getArgs()[3] + "`")).queue();
                                                            break;
                                                        }
                                                    }
                                                    try {
                                                        if ((pageNumber * 10) > tokensList.size() || (pageNumber * 10) < 0) {
                                                            context.getTextChannel().sendMessage("That page doesn't exist!").queue();
                                                        } else {
                                                            context.getTextChannel().sendMessage(StringUtils.join(tokensList.subList((pageNumber * 10), ((pageNumber + 1) * 10)), "\n") +
                                                                    (tokensList.size() > 10 ? "\n\nPage " + (pageNumber + 1) + "/" + (tokensList.size() / 10 + 1) +
                                                                            (pageNumber == 0 ? KekBot.replacePrefix(context.getGuild(), "\n\nDo {p}profile edit tokens list <number> to view that page.") : "") : "")).queue();
                                                        }
                                                    } catch (IndexOutOfBoundsException e) {
                                                        context.getTextChannel().sendMessage(StringUtils.join(tokensList.subList((pageNumber * 15), tokensList.size()), "\n") +
                                                                (tokensList.size() > 9 ? "\n\nPage " + (pageNumber + 1) + "/" + (tokensList.size() / 10 + 1) : "")).queue();
                                                    }
                                                } else {
                                                    context.getTextChannel().sendMessage("You have no tokens to list!").queue();
                                                }
                                                break;
                                            case "equip":
                                                if (context.getArgs().length < 4) {
                                                    context.getTextChannel().sendMessage("You haven't specified the token you wanted to equip.").queue();
                                                } else {
                                                    if (tokens.size() > 0) {
                                                            try {
                                                                Token token = tokens.get(Integer.valueOf(context.getArgs()[3]) - 1);
                                                                profile.equipToken(token);
                                                                profile.save();
                                                                context.getTextChannel().sendMessage("Equipped " + token.getName() + ".").queue();
                                                            } catch (NumberFormatException e) {
                                                                context.getTextChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + context.getArgs()[3] + "`")).queue();
                                                                break;
                                                            } catch (IndexOutOfBoundsException e) {
                                                                context.getTextChannel().sendMessage("Invalid token ID.").queue();
                                                            }
                                                    } else {
                                                        context.getTextChannel().sendMessage("You have no tokens to equip!").queue();
                                                    }
                                                }
                                                break;
                                        }
                                    }
                                    break;
                                case "background":
                                case "backgrounds":
                                    List<String> backgroundIDs = profile.getBackgrounds();
                                    List<Background> backgrounds = new ArrayList<>();
                                    for (String backgroundID : backgroundIDs) {
                                        if (KekBot.backgroundManager.doesBackgroundExist(backgroundID)) {
                                            backgrounds.add(KekBot.backgroundManager.get(backgroundID));
                                        } else profile.removeBackgroundByID(backgroundID);
                                    }
                                    if (context.getArgs().length < 3) {
                                        context.getTextChannel().sendMessage("**Number of Backgrounds: ** " + backgrounds.size() +
                                                "\n**Current Background: **" + (profile.hasBackgroundEquipped() ? profile.getCurrentBackground().getName() : "¯\\_(ツ)_/¯" +
                                                "\n**Available Options:** List, Set")).queue();
                                    } else {
                                        switch (context.getArgs()[2]) {
                                            case "list":
                                                if (backgrounds.size() > 0) {
                                                    List<String> backgroundsList = new ArrayList<>();
                                                    for (int i = 0; i < backgrounds.size(); i++) {
                                                        Background background = backgrounds.get(i);
                                                        backgroundsList.add((i + 1) + ". " + background.getName());
                                                    }
                                                    int pageNumber;
                                                    if (context.getArgs().length < 4) pageNumber = 0;
                                                    else {
                                                        try {
                                                            pageNumber = Integer.valueOf(context.getArgs()[3]) - 1;
                                                        } catch (NumberFormatException e) {
                                                            context.getTextChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + context.getArgs()[3] + "`")).queue();
                                                            break;
                                                        }
                                                    }
                                                    try {
                                                        if ((pageNumber * 10) > backgroundsList.size() || (pageNumber * 10) < 0) {
                                                            context.getTextChannel().sendMessage("That page doesn't exist!").queue();
                                                        } else {
                                                            context.getTextChannel().sendMessage(StringUtils.join(backgroundsList.subList((pageNumber * 10), ((pageNumber + 1) * 10)), "\n") +
                                                                    (backgroundsList.size() > 10 ? "\n\nPage " + (pageNumber + 1) + "/" + (backgroundsList.size() / 10 + 1) +
                                                                            (pageNumber == 0 ? KekBot.replacePrefix(context.getGuild(), "\n\nDo {p}profile edit backgrounds list <number> to view that page.") : "") : "")).queue();
                                                        }
                                                    } catch (IndexOutOfBoundsException e) {
                                                        context.getTextChannel().sendMessage(StringUtils.join(backgroundsList.subList((pageNumber * 15), backgroundsList.size()), "\n") +
                                                                (backgroundsList.size() > 9 ? "\n\nPage " + (pageNumber + 1) + "/" + (backgroundsList.size() / 10 + 1) : "")).queue();
                                                    }
                                                } else {
                                                    context.getTextChannel().sendMessage("You have no backgrounds to list!").queue();
                                                }
                                                break;
                                            case "set":
                                                if (context.getArgs().length < 4) {
                                                    context.getTextChannel().sendMessage("You haven't specified the background you wanted to set as your current.").queue();
                                                } else {
                                                    if (backgrounds.size() > 0) {
                                                        try {
                                                            Background background = backgrounds.get(Integer.valueOf(context.getArgs()[3]) - 1);
                                                            profile.setCurrentBackground(background);
                                                            profile.save();
                                                            context.getTextChannel().sendMessage("Set " + background.getName() + " as your current background.").queue();
                                                        } catch (NumberFormatException e) {
                                                            context.getTextChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + context.getArgs()[3] + "`")).queue();
                                                            break;
                                                        } catch (IndexOutOfBoundsException e) {
                                                            context.getTextChannel().sendMessage("Invalid background ID.").queue();
                                                        }
                                                    } else {
                                                        context.getTextChannel().sendMessage("You have no backgrounds to equip!").queue();
                                                    }
                                                }
                                                break;
                                        }
                                    }
                                    break;
                            }
                        }
                    } else if (context.getArgs()[0].equalsIgnoreCase("admin") && context.getAuthor().getId().equals(GSONUtils.getConfig().getBotOwner())) {
                        if (context.getArgs().length >= 2) {
                            switch (context.getArgs()[1]) {
                                case "view":
                                    if (context.getArgs().length >= 3) {
                                        try {
                                            context.getTextChannel().sendTyping().queue();
                                            User user = Utils.findUser(context.getArgs()[2]);
                                            context.getTextChannel().sendFile(Profile.getProfile(user).drawCard(context.getJDA()), "profile.png", new MessageBuilder().append("Here is " + user.getName() + "#" + user.getDiscriminator() + "'s profile card.").build()).queue();
                                        } catch (NullPointerException e) {
                                            context.getTextChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else context.getTextChannel().sendMessage("No user ID specified.").queue();
                                    break;
                                case "give":
                                    if (context.getArgs().length >= 3) {
                                        switch (context.getArgs()[2]) {
                                            case "topkeks":
                                                if (context.getArgs().length >= 4) {
                                                    try {
                                                        int toGive = Integer.valueOf(context.getArgs()[3]);
                                                        if (context.getArgs().length >= 5) {
                                                            try {
                                                                User user = Utils.findUser(context.getArgs()[4]);
                                                                Profile profile = Profile.getProfile(user);
                                                                profile.addTopKeks(toGive);
                                                                profile.save();
                                                                context.getTextChannel().sendMessage("Gave " + user.getName() + "#" + user.getDiscriminator() + " " + toGive + " " + CustomEmote.TOPKEK + ".").queue();
                                                            } catch (NullPointerException e) {
                                                                context.getTextChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                            }
                                                        }
                                                    } catch (NumberFormatException e) {
                                                        context.getTextChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, context.getArgs()[3])).queue();
                                                    }
                                                }
                                                break;
                                            case "background":
                                                if (context.getArgs().length >= 4) {
                                                    try {
                                                        Background background = KekBot.backgroundManager.get(context.getArgs()[3]);
                                                        if (context.getArgs().length >= 5) {
                                                            try {
                                                                User user = Utils.findUser(context.getArgs()[4]);
                                                                Profile profile = Profile.getProfile(user);
                                                                if (profile.hasBackground(background)) {
                                                                    context.getTextChannel().sendMessage(user.getName() + "#" + user.getDiscriminator() + " already owns this background.").queue();
                                                                } else {
                                                                    profile.addBackground(background);
                                                                    profile.save();
                                                                    context.getTextChannel().sendMessage("Gave " + user.getName() + "#" + user.getDiscriminator() + " the `" + background.getName() + "` background.").queue();
                                                                }
                                                            } catch (NullPointerException e) {
                                                                context.getTextChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                            }
                                                        }
                                                    } catch (NullPointerException e) {
                                                        context.getTextChannel().sendMessage("No background exists with that ID.").queue();
                                                    }
                                                }
                                                break;
                                            case "token":
                                                if (context.getArgs().length >= 4) {
                                                    try {
                                                        Token token = Token.valueOf(context.getArgs()[3]);
                                                        if (context.getArgs().length >= 5) {
                                                            try {
                                                                User user = Utils.findUser(context.getArgs()[4]);
                                                                Profile profile = Profile.getProfile(user);
                                                                if (profile.hasToken(token)) {
                                                                    context.getTextChannel().sendMessage(user.getName() + "#" + user.getDiscriminator() + " already owns this token.").queue();
                                                                } else {
                                                                    profile.addToken(token);
                                                                    profile.save();
                                                                    context.getTextChannel().sendMessage("Gave " + user.getName() + "#" + user.getDiscriminator() + " the `" + token.getName() + "` token.").queue();
                                                                }
                                                            } catch (NullPointerException e) {
                                                                context.getTextChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                            }
                                                        }
                                                    } catch (IllegalArgumentException e) {
                                                        context.getTextChannel().sendMessage("No token exists with that ID.").queue();
                                                    }
                                                }
                                                break;
                                        }
                                    }
                                    break;
                                case "take":
                                    if (context.getArgs().length >= 3) {
                                        switch (context.getArgs()[2]) {
                                            case "topkeks":
                                                if (context.getArgs().length >= 4) {
                                                    try {
                                                        int toTake = Integer.valueOf(context.getArgs()[3]);
                                                        if (context.getArgs().length >= 5) {
                                                            try {
                                                                User user = Utils.findUser(context.getArgs()[4]);
                                                                Profile profile = Profile.getProfile(user);
                                                                if (toTake > profile.getTopkeks()) toTake = profile.getTopkeks();
                                                                profile.spendTopKeks(toTake);
                                                                profile.save();
                                                                context.getTextChannel().sendMessage("Took away " + toTake + CustomEmote.TOPKEK + " from " + user.getName() + "#" + user.getDiscriminator() + ".").queue();
                                                            } catch (NullPointerException e) {
                                                                context.getTextChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                            }
                                                        }
                                                    } catch (NumberFormatException e) {
                                                        context.getTextChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, context.getArgs()[3])).queue();
                                                    }
                                                }
                                                break;
                                            case "background":
                                                if (context.getArgs().length >= 4) {
                                                    try {
                                                        Background background = KekBot.backgroundManager.get(context.getArgs()[3]);
                                                        if (context.getArgs().length >= 5) {
                                                            try {
                                                                User user = Utils.findUser(context.getArgs()[4]);
                                                                Profile profile = Profile.getProfile(user);
                                                                if (profile.hasBackground(background)) {
                                                                    if (profile.getCurrentBackground().equals(background)) profile.setCurrentBackground(null);
                                                                    profile.removeBackgroundByID(background.getID());
                                                                    profile.save();
                                                                    context.getTextChannel().sendMessage("Took the `" + background.getName() + "` background away from " + user.getName() + "#" + user.getDiscriminator() + ".").queue();
                                                                } else {
                                                                    context.getTextChannel().sendMessage(user.getName() + "#" + user.getDiscriminator() + " doesn't own this background.").queue();
                                                                }
                                                            } catch (NullPointerException e) {
                                                                context.getTextChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                            }
                                                        }
                                                    } catch (NullPointerException e) {
                                                        context.getTextChannel().sendMessage("No background exists with that ID.").queue();
                                                    }
                                                }
                                                break;
                                            case "token":
                                                if (context.getArgs().length >= 4) {
                                                    try {
                                                        Token token = Token.valueOf(context.getArgs()[3]);
                                                        if (context.getArgs().length >= 5) {
                                                            try {
                                                                User user = Utils.findUser(context.getArgs()[4]);
                                                                Profile profile = Profile.getProfile(user);
                                                                if (profile.hasToken(token)) {
                                                                    if (profile.getToken().equals(token)) profile.unequipToken();
                                                                    profile.removeToken(token);
                                                                    profile.save();
                                                                    context.getTextChannel().sendMessage("Took the `" + token.getName() + "` token from " + user.getName() + "#" + user.getDiscriminator() + ".").queue();
                                                                } else {
                                                                    context.getTextChannel().sendMessage(user.getName() + "#" + user.getDiscriminator() + " doesn't own this token.").queue();
                                                                }
                                                            } catch (NullPointerException e) {
                                                                context.getTextChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                            }
                                                        }
                                                    } catch (IllegalArgumentException e) {
                                                        context.getTextChannel().sendMessage("No token exists with that ID.").queue();
                                                    }
                                                }
                                                break;
                                        }
                                    }
                                    break;
                                case "set":
                                    if (context.getArgs().length >= 3) {
                                        switch (context.getArgs()[2]) {
                                            case "topkeks":
                                                if (context.getArgs().length >= 4) {
                                                    try {
                                                        int toSet = Integer.valueOf(context.getArgs()[3]);
                                                        if (context.getArgs().length >= 5) {
                                                            try {
                                                                User user = Utils.findUser(context.getArgs()[4]);
                                                                Profile profile = Profile.getProfile(user);
                                                                profile.setTopKeks(toSet);
                                                                profile.save();
                                                                context.getTextChannel().sendMessage("Set " + user.getName() + "#" + user.getDiscriminator() + "'s " + CustomEmote.TOPKEK + "to " + + toSet + ".").queue();
                                                            } catch (NullPointerException e) {
                                                                context.getTextChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                            }
                                                        }
                                                    } catch (NumberFormatException e) {
                                                        context.getTextChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, context.getArgs()[3])).queue();
                                                    }
                                                }
                                                break;
                                            case "background":
                                                if (context.getArgs().length >= 4) {
                                                    try {
                                                        Background background;
                                                        if (context.getArgs()[3].equalsIgnoreCase("none")) background = null;
                                                        else background = KekBot.backgroundManager.get(context.getArgs()[3]);
                                                        if (context.getArgs().length >= 5) {
                                                            try {
                                                                User user = Utils.findUser(context.getArgs()[4]);
                                                                Profile profile = Profile.getProfile(user);
                                                                if (background == null || profile.hasBackground(background)) {
                                                                    profile.setCurrentBackground(background);
                                                                    profile.save();
                                                                    if (background != null) context.getTextChannel().sendMessage("Set " + user.getName() + "#" + user.getDiscriminator() + "'s background to `" + background.getName() + "`").queue();
                                                                    else context.getTextChannel().sendMessage("Reset " + user.getName() + "#" + user.getDiscriminator() + "'s background.").queue();
                                                                } else {
                                                                    context.getTextChannel().sendMessage(user.getName() + "#" + user.getDiscriminator() + " doesn't own this background.").queue();
                                                                }
                                                            } catch (NullPointerException e) {
                                                                e.printStackTrace();
                                                                context.getTextChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                            }
                                                        }
                                                    } catch (NullPointerException e) {
                                                        context.getTextChannel().sendMessage("No background exists with that ID.").queue();
                                                    }
                                                }
                                                break;
                                            case "token":
                                                if (context.getArgs().length >= 4) {
                                                    try {
                                                        Token token;
                                                        if (context.getArgs()[3].equalsIgnoreCase("none")) token = null;
                                                        else token = Token.valueOf(context.getArgs()[3]);
                                                        if (context.getArgs().length >= 5) {
                                                            try {
                                                                User user = Utils.findUser(context.getArgs()[4]);
                                                                Profile profile = Profile.getProfile(user);
                                                                if (profile.hasToken(token) || token == null) {
                                                                    profile.equipToken(token);
                                                                    profile.save();
                                                                    context.getTextChannel().sendMessage("Set " + user.getName() + "#" + user.getDiscriminator() + "'s token to `" + token.getName() + "`.").queue();
                                                                } else {
                                                                    context.getTextChannel().sendMessage(user.getName() + "#" + user.getDiscriminator() + " doesn't own this token.").queue();
                                                                }
                                                            } catch (NullPointerException e) {
                                                                context.getTextChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                            }
                                                        }
                                                    } catch (IllegalArgumentException e) {
                                                        context.getTextChannel().sendMessage("No token exists with that ID.").queue();
                                                    }
                                                }
                                                break;
                                            case "badge":
                                                if (context.getArgs().length >= 4) {
                                                    try {
                                                        Badge badge;
                                                        if (context.getArgs()[3].equalsIgnoreCase("none")) badge = null;
                                                        else badge = Badge.valueOf(context.getArgs()[3]);
                                                        if (context.getArgs().length >= 5) {
                                                            try {
                                                                User user = Utils.findUser(context.getArgs()[4]);
                                                                Profile profile = Profile.getProfile(user);
                                                                profile.setBadge(badge);
                                                                profile.save();
                                                                context.getTextChannel().sendMessage("Set " + user.getName() + "#" + user.getDiscriminator() + "'s badge to `" + badge.getName() + "`.").queue();
                                                            } catch (NullPointerException e) {
                                                                context.getTextChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                            }
                                                        }
                                                    } catch (IllegalArgumentException e) {
                                                        context.getTextChannel().sendMessage("No badge exists with that ID.").queue();
                                                    }
                                                }
                                                break;
                                        }
                                    } else context.getTextChannel().sendMessage("No arguments specified.").queue();
                            }
                        } else context.getTextChannel().sendMessage("No arguments specified.").queue();
                    } else if (context.getMessage().getMentionedUsers().size() == 1) {
                        try {
                            context.getTextChannel().sendTyping().queue();
                            User user = context.getMessage().getMentionedUsers().get(0);
                            Profile profile = Profile.getProfile(user);
                            context.getTextChannel().sendFile(profile.drawCard(context.getJDA()), "profile.png", new MessageBuilder().append("Here is ").append(user.getName()).append("'s profile card:").build()).queue();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
}
