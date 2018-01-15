package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.profile.*;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.settings.Config;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileCommand extends Command {

    public ProfileCommand() {
        name = "profile";
        description = "Lets you view and edit your profile, and also viewing other peoples profiles.";
        usage.add("profile");
        usage.add("profile edit <args>");
        usage.add("profile <@user>");
        category = new Category("Fun");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length == 0) {
            try {
                event.getChannel().sendTyping().queue();
                event.getChannel().sendFile(Profile.getProfile(event.getAuthor()).drawCard(), "profile.png", null).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (event.getArgs().length >= 1) {
            if (event.getArgs()[0].equalsIgnoreCase("edit")) {
                Profile profile = Profile.getProfile(event.getAuthor());
                if (event.getArgs().length < 2) {
                    event.getChannel().sendMessage("Here are the available values you can edit:\n" +
                            "\n**Title:** " + profile.getSubtitle() +
                            "\n**Bio:** " + (profile.getBio() != null ? profile.getBio() : "No Bio Set.") +
                            "\n**Tokens:** " + (profile.getTokens().size() > 0 ? profile.getTokens().size() + " " + (profile.getTokens().size() > 1 ? "Tokens" : "Token")  : "¯\\_(ツ)_/¯") +
                            "\n**Backgrounds:** " + (profile.getBackgrounds().size() > 0 ? profile.getBackgrounds().size() + " " + (profile.getBackgrounds().size() > 1 ? "Backgrounds" : "Background") : "¯\\_(ツ)_/¯") +
                            "\n**Playlists:** " + "Use " + event.getClient().getPrefix(event.getGuild().getId()) + "myplaylist to view your playlists.").queue();
                } else {
                    switch (event.getArgs()[1].toLowerCase()) {
                        case "title":
                            if (event.getArgs().length >= 3) {
                                setTitle(event, profile, event.combineArgs(2));
                            } else {
                                new Questionnaire(event)
                                        .addQuestion("Type the title you would like to use below. Or type `cancel` to exit.", QuestionType.STRING)
                                        .execute(results -> setTitle(event, profile, results.getAnswer(0).toString()));
                            }
                            break;
                        case "bio":
                            if (event.getArgs().length >= 3) {
                                setBio(event, profile, event.combineArgs(2));
                            } else {
                                new Questionnaire(event)
                                        .addQuestion("Type the title you would like to use below. Or type `cancel` to exit.", QuestionType.STRING)
                                        .execute(results -> {
                                            if (ProfileUtils.testBio(results.getAnswer(0).toString())) {
                                                setBio(event, profile, results.getAnswer(0).toString());
                                            } else {
                                                event.getChannel().sendMessage("That bio is too long, and will not fit in your profile card. Please try something shorter, or type `cancel` to exit.").queue();
                                                results.reExecuteWithoutMessage();
                                            }
                                        });
                            }
                            break;
                        case "token":
                        case "tokens":
                            List<Token> tokens = profile.getTokens();
                            if (event.getArgs().length < 3) {
                                event.getChannel().sendMessage("**Number of Tokens: ** " + tokens.size() +
                                        //"\n**Token Display: **" + KekBot.replacePrefix(event.getGuild(), "Use `{p}profile edit token display` to enter the token display editor.") +
                                        "\n**Equipped Token: **" + (profile.hasTokenEquipped() ? profile.getToken().getName() : "¯\\_(ツ)_/¯") +
                                        "\n**Available Options:** List, Equip").queue();
                            } else {
                                switch (event.getArgs()[2]) {
                                    case "list":
                                        if (tokens.size() > 0) {
                                            List<String> tokensList = new ArrayList<>();
                                            for (int i = 0; i < tokens.size(); i++) {
                                                Token token = tokens.get(i);
                                                tokensList.add((i + 1) + ". " + token.getName());
                                            }
                                            int pageNumber;
                                            int maxItems = 10;
                                            if (event.getArgs().length < 4) pageNumber = 0;
                                            else {
                                                try {
                                                    pageNumber = Integer.valueOf(event.getArgs()[3]) - 1;
                                                } catch (NumberFormatException e) {
                                                    event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + event.getArgs()[3] + "`")).queue();
                                                    break;
                                                }
                                            }
                                            try {
                                                if ((pageNumber * maxItems) > tokensList.size() || (pageNumber * maxItems) < 0) {
                                                    event.getChannel().sendMessage("That page doesn't exist!").queue();
                                                } else {
                                                    event.getChannel().sendMessage(StringUtils.join(tokensList.subList((pageNumber * maxItems), ((pageNumber + 1) * maxItems)), "\n") +
                                                            (tokensList.size() > maxItems ? "\n\nPage " + (pageNumber + 1) + "/" + (tokensList.size() / maxItems + 1) +
                                                                    (pageNumber == 0 ? "\n\nDo " + event.getClient().getPrefix(event.getGuild().getId()) + "profile edit tokens list <number> to view that page." : "") : "")).queue();
                                                }
                                            } catch (IndexOutOfBoundsException e) {
                                                event.getChannel().sendMessage(StringUtils.join(tokensList.subList((pageNumber * maxItems), tokensList.size()), "\n") +
                                                        (tokensList.size() > maxItems ? "\n\nPage " + (pageNumber + 1) + "/" + (tokensList.size() / maxItems + 1) : "")).queue();
                                            }
                                        } else {
                                            event.getChannel().sendMessage("You have no tokens to list!").queue();
                                        }
                                        break;
                                    case "equip":
                                        if (event.getArgs().length < 4) {
                                            event.getChannel().sendMessage("You haven't specified the token you wanted to equip.").queue();
                                        } else {
                                            if (tokens.size() > 0) {
                                                try {
                                                    Token token = tokens.get(Integer.valueOf(event.getArgs()[3]) - 1);
                                                    profile.equipToken(token);
                                                    profile.save();
                                                    event.getChannel().sendMessage("Equipped " + token.getName() + ".").queue();
                                                } catch (NumberFormatException e) {
                                                    event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + event.getArgs()[3] + "`")).queue();
                                                    break;
                                                } catch (IndexOutOfBoundsException e) {
                                                    event.getChannel().sendMessage("Invalid token ID.").queue();
                                                }
                                            } else {
                                                event.getChannel().sendMessage("You have no tokens to equip!").queue();
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
                            if (event.getArgs().length < 3) {
                                event.getChannel().sendMessage("**Number of Backgrounds: ** " + backgrounds.size() +
                                        "\n**Current Background: **" + (profile.hasBackgroundEquipped() ? profile.getCurrentBackground().getName() : "¯\\_(ツ)_/¯") +
                                        "\n**Available Options:** List, Set").queue();
                            } else {
                                switch (event.getArgs()[2]) {
                                    case "list":
                                        if (backgrounds.size() > 0) {
                                            List<String> backgroundsList = new ArrayList<>();
                                            for (int i = 0; i < backgrounds.size(); i++) {
                                                Background background = backgrounds.get(i);
                                                backgroundsList.add((i + 1) + ". " + background.getName());
                                            }
                                            int pageNumber;
                                            int maxItems = 10;
                                            if (event.getArgs().length < 4) pageNumber = 0;
                                            else {
                                                try {
                                                    pageNumber = Integer.valueOf(event.getArgs()[3]) - 1;
                                                } catch (NumberFormatException e) {
                                                    event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + event.getArgs()[3] + "`")).queue();
                                                    break;
                                                }
                                            }
                                            try {
                                                if ((pageNumber * maxItems) > backgroundsList.size() || (pageNumber * maxItems) < 0) {
                                                    event.getChannel().sendMessage("That page doesn't exist!").queue();
                                                } else {
                                                    event.getChannel().sendMessage(StringUtils.join(backgroundsList.subList((pageNumber * maxItems), ((pageNumber + 1) * maxItems)), "\n") +
                                                            (backgroundsList.size() > maxItems ? "\n\nPage " + (pageNumber + 1) + "/" + (backgroundsList.size() / maxItems + 1) +
                                                                    (pageNumber == 0 ? "\n\nDo " + event.getClient().getPrefix(event.getGuild().getId()) + "profile edit backgrounds list <number> to view that page." : "") : "")).queue();
                                                }
                                            } catch (IndexOutOfBoundsException e) {
                                                event.getChannel().sendMessage(StringUtils.join(backgroundsList.subList((pageNumber * maxItems), backgroundsList.size()), "\n") +
                                                        (backgroundsList.size() > maxItems ? "\n\nPage " + (pageNumber + 1) + "/" + (backgroundsList.size() / maxItems + 1) : "")).queue();
                                            }
                                        } else {
                                            event.getChannel().sendMessage("You have no backgrounds to list!").queue();
                                        }
                                        break;
                                    case "set":
                                        if (event.getArgs().length < 4) {
                                            event.getChannel().sendMessage("You haven't specified the background you wanted to set as your current.").queue();
                                        } else {
                                            if (backgrounds.size() > 0) {
                                                try {
                                                    Background background = backgrounds.get(Integer.valueOf(event.getArgs()[3]) - 1);
                                                    profile.setCurrentBackground(background);
                                                    profile.save();
                                                    event.getChannel().sendMessage("Set " + background.getName() + " as your current background.").queue();
                                                } catch (NumberFormatException e) {
                                                    event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + event.getArgs()[3] + "`")).queue();
                                                    break;
                                                } catch (IndexOutOfBoundsException e) {
                                                    event.getChannel().sendMessage("Invalid background ID.").queue();
                                                }
                                            } else {
                                                event.getChannel().sendMessage("You have no backgrounds to equip!").queue();
                                            }
                                        }
                                        break;
                                }
                            }
                            break;
                    }
                }
            } else if (event.getArgs()[0].equalsIgnoreCase("admin") && event.isBotOwner()) {
                if (event.getArgs().length >= 2) {
                    switch (event.getArgs()[1]) {
                        case "view":
                            if (event.getArgs().length >= 3) {
                                try {
                                    event.getChannel().sendTyping().queue();
                                    User user = KekBot.jda.getUserById(event.getArgs()[2]);
                                    event.getChannel().sendFile(Profile.getProfile(user).drawCard(), "profile.png", new MessageBuilder().append("Here is " + user.getName() + "#" + user.getDiscriminator() + "'s profile card.").build()).queue();
                                } catch (NullPointerException e) {
                                    event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else event.getChannel().sendMessage("No user ID specified.").queue();
                            break;
                        case "give":
                            if (event.getArgs().length >= 3) {
                                switch (event.getArgs()[2]) {
                                    case "topkeks":
                                        if (event.getArgs().length >= 4) {
                                            try {
                                                int toGive = Integer.valueOf(event.getArgs()[3]);
                                                if (event.getArgs().length >= 5) {
                                                    try {
                                                        User user = KekBot.jda.getUserById(event.getArgs()[4]);
                                                        Profile profile = Profile.getProfile(user);
                                                        profile.addTopKeks(toGive);
                                                        profile.save();
                                                        event.getChannel().sendMessage("Gave " + user.getName() + "#" + user.getDiscriminator() + " " + CustomEmote.printPrice(toGive) + ".").queue();
                                                    } catch (NullPointerException e) {
                                                        event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                    }
                                                }
                                            } catch (NumberFormatException e) {
                                                event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getArgs()[3])).queue();
                                            }
                                        }
                                        break;
                                    case "XP":
                                    case "KXP":
                                        if (event.getArgs().length >= 4) {
                                            try {
                                                int toGive = Integer.valueOf(event.getArgs()[3]);
                                                if (event.getArgs().length >= 5) {
                                                    try {
                                                        User user = KekBot.jda.getUserById(event.getArgs()[4]);
                                                        Profile profile = Profile.getProfile(user);
                                                        profile.addKXP(toGive);
                                                        profile.save();
                                                        event.getChannel().sendMessage("Gave " + user.getName() + "#" + user.getDiscriminator() + toGive + " KXP.").queue();
                                                    } catch (NullPointerException e) {
                                                        event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                    }
                                                }
                                            } catch (NumberFormatException e) {
                                                event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getArgs()[3])).queue();
                                            }
                                        }
                                        break;
                                    case "background":
                                        if (event.getArgs().length >= 4) {
                                            try {
                                                Background background = KekBot.backgroundManager.get(event.getArgs()[3]);
                                                if (event.getArgs().length >= 5) {
                                                    try {
                                                        User user = KekBot.jda.getUserById(event.getArgs()[4]);
                                                        Profile profile = Profile.getProfile(user);
                                                        if (profile.hasBackground(background)) {
                                                            event.getChannel().sendMessage(user.getName() + "#" + user.getDiscriminator() + " already owns this background.").queue();
                                                        } else {
                                                            profile.addBackground(background);
                                                            profile.save();
                                                            event.getChannel().sendMessage("Gave " + user.getName() + "#" + user.getDiscriminator() + " the `" + background.getName() + "` background.").queue();
                                                        }
                                                    } catch (NullPointerException e) {
                                                        event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                    }
                                                }
                                            } catch (NullPointerException e) {
                                                event.getChannel().sendMessage("No background exists with that ID.").queue();
                                            }
                                        }
                                        break;
                                    case "token":
                                        if (event.getArgs().length >= 4) {
                                            try {
                                                Token token = Token.valueOf(event.getArgs()[3]);
                                                if (event.getArgs().length >= 5) {
                                                    try {
                                                        User user = KekBot.jda.getUserById(event.getArgs()[4]);
                                                        Profile profile = Profile.getProfile(user);
                                                        if (profile.hasToken(token)) {
                                                            event.getChannel().sendMessage(user.getName() + "#" + user.getDiscriminator() + " already owns this token.").queue();
                                                        } else {
                                                            profile.addToken(token);
                                                            profile.save();
                                                            event.getChannel().sendMessage("Gave " + user.getName() + "#" + user.getDiscriminator() + " the `" + token.getName() + "` token.").queue();
                                                        }
                                                    } catch (NullPointerException e) {
                                                        event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                    }
                                                }
                                            } catch (IllegalArgumentException e) {
                                                event.getChannel().sendMessage("No token exists with that ID.").queue();
                                            }
                                        }
                                        break;
                                }
                            }
                            break;
                        case "take":
                            if (event.getArgs().length >= 3) {
                                switch (event.getArgs()[2]) {
                                    case "topkeks":
                                        if (event.getArgs().length >= 4) {
                                            try {
                                                double toTake = Integer.valueOf(event.getArgs()[3]);
                                                if (event.getArgs().length >= 5) {
                                                    try {
                                                        User user = KekBot.jda.getUserById(event.getArgs()[4]);
                                                        Profile profile = Profile.getProfile(user);
                                                        if (toTake > profile.getTopkeks()) toTake = profile.getTopkeks();
                                                        profile.spendTopKeks(toTake);
                                                        profile.save();
                                                        event.getChannel().sendMessage("Took away " + CustomEmote.printPrice(toTake) + " from " + user.getName() + "#" + user.getDiscriminator() + ".").queue();
                                                    } catch (NullPointerException e) {
                                                        event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                    }
                                                }
                                            } catch (NumberFormatException e) {
                                                event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getArgs()[3])).queue();
                                            }
                                        }
                                        break;
                                    case "XP":
                                    case "KXP":
                                        if (event.getArgs().length >= 4) {
                                            try {
                                                int toTake = Integer.valueOf(event.getArgs()[3]);
                                                if (event.getArgs().length >= 5) {
                                                    try {
                                                        User user = KekBot.jda.getUserById(event.getArgs()[4]);
                                                        Profile profile = Profile.getProfile(user);
                                                        profile.takeKXP(toTake);
                                                        profile.save();
                                                        event.getChannel().sendMessage("Took away " + toTake + " KXP from " + user.getName() + "#" + user.getDiscriminator() + ".").queue();
                                                    } catch (NullPointerException e) {
                                                        event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                    }
                                                }
                                            } catch (NumberFormatException e) {
                                                event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getArgs()[3])).queue();
                                            }
                                        }
                                        break;
                                    case "background":
                                        if (event.getArgs().length >= 4) {
                                            try {
                                                Background background = KekBot.backgroundManager.get(event.getArgs()[3]);
                                                if (event.getArgs().length >= 5) {
                                                    try {
                                                        User user = KekBot.jda.getUserById(event.getArgs()[4]);
                                                        Profile profile = Profile.getProfile(user);
                                                        if (profile.hasBackground(background)) {
                                                            if (profile.getCurrentBackground().equals(background)) profile.setCurrentBackground(null);
                                                            profile.removeBackgroundByID(background.getID());
                                                            profile.save();
                                                            event.getChannel().sendMessage("Took the `" + background.getName() + "` background away from " + user.getName() + "#" + user.getDiscriminator() + ".").queue();
                                                        } else {
                                                            event.getChannel().sendMessage(user.getName() + "#" + user.getDiscriminator() + " doesn't own this background.").queue();
                                                        }
                                                    } catch (NullPointerException e) {
                                                        event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                    }
                                                }
                                            } catch (NullPointerException e) {
                                                event.getChannel().sendMessage("No background exists with that ID.").queue();
                                            }
                                        }
                                        break;
                                    case "token":
                                        if (event.getArgs().length >= 4) {
                                            try {
                                                Token token = Token.valueOf(event.getArgs()[3]);
                                                if (event.getArgs().length >= 5) {
                                                    try {
                                                        User user = KekBot.jda.getUserById(event.getArgs()[4]);
                                                        Profile profile = Profile.getProfile(user);
                                                        if (profile.hasToken(token)) {
                                                            if (profile.getToken().equals(token)) profile.unequipToken();
                                                            profile.removeToken(token);
                                                            profile.save();
                                                            event.getChannel().sendMessage("Took the `" + token.getName() + "` token from " + user.getName() + "#" + user.getDiscriminator() + ".").queue();
                                                        } else {
                                                            event.getChannel().sendMessage(user.getName() + "#" + user.getDiscriminator() + " doesn't own this token.").queue();
                                                        }
                                                    } catch (NullPointerException e) {
                                                        event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                    }
                                                }
                                            } catch (IllegalArgumentException e) {
                                                event.getChannel().sendMessage("No token exists with that ID.").queue();
                                            }
                                        }
                                        break;
                                }
                            }
                            break;
                        case "set":
                            if (event.getArgs().length >= 3) {
                                switch (event.getArgs()[2]) {
                                    case "topkeks":
                                        if (event.getArgs().length >= 4) {
                                            try {
                                                int toSet = Integer.valueOf(event.getArgs()[3]);
                                                if (event.getArgs().length >= 5) {
                                                    try {
                                                        User user = KekBot.jda.getUserById(event.getArgs()[4]);
                                                        Profile profile = Profile.getProfile(user);
                                                        profile.setTopKeks(toSet);
                                                        profile.save();
                                                        event.getChannel().sendMessage("Set " + user.getName() + "#" + user.getDiscriminator() + "'s " + CustomEmote.printTopKek() + "to " + + toSet + ".").queue();
                                                    } catch (NullPointerException e) {
                                                        event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                    }
                                                }
                                            } catch (NumberFormatException e) {
                                                event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getArgs()[3])).queue();
                                            }
                                        }
                                        break;
                                    case "XP":
                                    case "KXP":
                                        if (event.getArgs().length >= 4) {
                                            try {
                                                int toSet = Integer.valueOf(event.getArgs()[3]);
                                                if (event.getArgs().length >= 5) {
                                                    try {
                                                        User user = KekBot.jda.getUserById(event.getArgs()[4]);
                                                        Profile profile = Profile.getProfile(user);
                                                        profile.setKXP(toSet);
                                                        profile.save();
                                                        event.getChannel().sendMessage("Set " + user.getName() + "#" + user.getDiscriminator() + "'s KXP to " + + toSet + ".").queue();
                                                    } catch (NullPointerException e) {
                                                        event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                    }
                                                }
                                            } catch (NumberFormatException e) {
                                                event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getArgs()[3])).queue();
                                            }
                                        }
                                        break;
                                    case "background":
                                        if (event.getArgs().length >= 4) {
                                            try {
                                                Background background;
                                                if (event.getArgs()[3].equalsIgnoreCase("none")) background = null;
                                                else background = KekBot.backgroundManager.get(event.getArgs()[3]);
                                                if (event.getArgs().length >= 5) {
                                                    try {
                                                        User user = KekBot.jda.getUserById(event.getArgs()[4]);
                                                        Profile profile = Profile.getProfile(user);
                                                        if (background == null || profile.hasBackground(background)) {
                                                            profile.setCurrentBackground(background);
                                                            profile.save();
                                                            if (background != null) event.getChannel().sendMessage("Set " + user.getName() + "#" + user.getDiscriminator() + "'s background to `" + background.getName() + "`").queue();
                                                            else event.getChannel().sendMessage("Reset " + user.getName() + "#" + user.getDiscriminator() + "'s background.").queue();
                                                        } else {
                                                            event.getChannel().sendMessage(user.getName() + "#" + user.getDiscriminator() + " doesn't own this background.").queue();
                                                        }
                                                    } catch (NullPointerException e) {
                                                        e.printStackTrace();
                                                        event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                    }
                                                }
                                            } catch (NullPointerException e) {
                                                event.getChannel().sendMessage("No background exists with that ID.").queue();
                                            }
                                        }
                                        break;
                                    case "token":
                                        if (event.getArgs().length >= 4) {
                                            try {
                                                Token token;
                                                if (event.getArgs()[3].equalsIgnoreCase("none")) token = null;
                                                else token = Token.valueOf(event.getArgs()[3]);
                                                if (event.getArgs().length >= 5) {
                                                    try {
                                                        User user = KekBot.jda.getUserById(event.getArgs()[4]);
                                                        Profile profile = Profile.getProfile(user);
                                                        if (profile.hasToken(token) || token == null) {
                                                            profile.equipToken(token);
                                                            profile.save();
                                                            if (token != null) event.getChannel().sendMessage("Set " + user.getName() + "#" + user.getDiscriminator() + "'s token to `" + token.getName() + "`.").queue();
                                                            else event.getChannel().sendMessage("Reset " + user.getName() + "#" + user.getDiscriminator() + "'s token.").queue();
                                                        } else {
                                                            event.getChannel().sendMessage(user.getName() + "#" + user.getDiscriminator() + " doesn't own this token.").queue();
                                                        }
                                                    } catch (NullPointerException e) {
                                                        event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                    }
                                                }
                                            } catch (IllegalArgumentException e) {
                                                event.getChannel().sendMessage("No token exists with that ID.").queue();
                                            }
                                        }
                                        break;
                                    case "badge":
                                        if (event.getArgs().length >= 4) {
                                            try {
                                                Badge badge;
                                                if (event.getArgs()[3].equalsIgnoreCase("none")) badge = null;
                                                else badge = Badge.valueOf(event.getArgs()[3]);
                                                if (event.getArgs().length >= 5) {
                                                    try {
                                                        User user = KekBot.jda.getUserById(event.getArgs()[4]);
                                                        Profile profile = Profile.getProfile(user);
                                                        profile.setBadge(badge);
                                                        profile.save();
                                                        if (badge != null) event.getChannel().sendMessage("Set " + user.getName() + "#" + user.getDiscriminator() + "'s badge to `" + badge.getName() + "`.").queue();
                                                        else event.getChannel().sendMessage("Reset " + user.getName() + "#" + user.getDiscriminator() + "'s badge.").queue();
                                                    } catch (NullPointerException e) {
                                                        event.getChannel().sendMessage("User with that ID not found, or the ID specified is invalid.").queue();
                                                    }
                                                }
                                            } catch (IllegalArgumentException e) {
                                                event.getChannel().sendMessage("No badge exists with that ID.").queue();
                                            }
                                        }
                                        break;
                                }
                            } else event.getChannel().sendMessage("No arguments specified.").queue();
                    }
                } else event.getChannel().sendMessage("No arguments specified.").queue();
            } else if (event.getEvent().getMessage().getMentionedUsers().size() == 1) {
                try {
                    event.getChannel().sendTyping().queue();
                    User user = event.getEvent().getMessage().getMentionedUsers().get(0);
                    Profile profile = Profile.getProfile(user);
                    event.getChannel().sendFile(profile.drawCard(), "profile.png", new MessageBuilder().append("Here is ").append(user.getName()).append("'s profile card:").build()).queue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setTitle(CommandEvent event, Profile profile, String title) {
        if (title.length() <= 20) {
            new Questionnaire(event)
                    .addYesNoQuestion("Are you sure you want to use the title: `" + title + "`?")
                    .execute(results -> {
                        if (results.getAnswerAsType(0, boolean.class)) {
                            profile.setSubtitle(title);
                            profile.save();
                            event.getChannel().sendMessage("Title set!").queue();
                        } else {
                            event.getChannel().sendMessage("Cancelled.").queue();
                        }
                    });
        } else {
            event.getChannel().sendMessage("Your title is too long. Titles can only be 20 characters or fewer. Try something shorter.").queue();
        }
    }

    private void setBio(CommandEvent event, Profile profile, String bio) {
        if (ProfileUtils.testBio(bio)) {
            new Questionnaire(event)
                    .addYesNoQuestion("For your bio, you wrote: `" + bio + "` Is this correct?")
                    .execute(results -> {
                        if (results.getAnswerAsType(0, boolean.class)) {
                            profile.setBio(bio);
                            profile.save();
                            event.getChannel().sendMessage("Bio set!").queue();
                        } else {
                            event.getChannel().sendMessage("Cancelled.").queue();
                        }
                    });
        } else {
            event.getChannel().sendMessage("That bio is too long, and will not fit in your profile card. Please try something shorter.").queue();
        }
    }
}
