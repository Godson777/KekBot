package com.godson.kekbot.command.commands.fun;

import com.godson.discoin4j.Discoin4J;
import com.godson.discoin4j.exceptions.DiscoinErrorException;
import com.godson.discoin4j.exceptions.RejectedException;
import com.godson.discoin4j.exceptions.UnauthorizedException;
import com.godson.discoin4j.exceptions.UnknownErrorException;
import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.menu.ShopMenu;
import com.godson.kekbot.profile.item.Background;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.profile.item.Token;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.settings.Config;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.jagrosh.jdautilities.menu.OrderedMenu;

import java.io.IOException;

public class ShopCommand extends Command {

    public ShopCommand() {
        name = "shop";
        description = "Opens up the profile shop.";
        usage.add("shop");
        extendedDescription = "\nAvailable Categories:" +
                "\nTokens" +
                "\nBackgrounds" +
                "\n\n#Notes:" +
                "\nArrows signify other pages in a shop." +
                "\nCrossed out items require you to be a higher level.";
        exDescPos = ExtendedPosition.AFTER;
        category = new Category("Fun");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        OrderedMenu.Builder builder = new OrderedMenu.Builder();
        builder.addChoices("Tokens","Backgrounds");
        if (Config.getConfig().getDcoinToken() != null) builder.addChoices("Convert Topkeks");
        builder.setDescription(event.getString("command.fun.shop.intro") + "\n");

        //Make sure there's a cancel button.
        builder.useCancelButton(true);

        //Make sure only the user calling the command can mess with this menu.
        builder.addUsers(event.getAuthor());

        builder.setEventWaiter(KekBot.waiter);

        //Don't allow text input.
        builder.allowTextInput(false);

        //Handler for all the selections.
        builder.setSelection((m, sel) -> {
            switch (sel) {
                case 1:
                    //Token Shop
                    ShopMenu.Builder tokenShop = new ShopMenu.Builder();

                    tokenShop.setEventWaiter(KekBot.waiter);

                    //Make sure only the user calling the command can mess with this menu.
                    tokenShop.setUsers(event.getAuthor());

                    //Draw the shops images for this menu.
                    try {
                        tokenShop.addImages(KekBot.tokenShop.draw(Profile.getProfile(event.getAuthor())));
                    } catch (IOException e) {
                        throwException(e, event, "Shop Generation Error.");
                    }

                    //Number of items in the shop.
                    tokenShop.setNumberOfItems(KekBot.tokenShop.getInventory().size());

                    //Handler for shop items.
                    tokenShop.setSelectionAction((msg, selection) -> {
                        //Item we selected from the shop.
                        Token selectedToken = KekBot.tokenShop.getInventory().get(selection - 1);

                        //User's profile.
                        Profile profile = Profile.getProfile(event.getAuthor());

                        //Item's info.
                        String info = "***" + event.getString("command.fun.shop.name") + "*** " + selectedToken.getName() +
                                "\n***" + event.getString("command.fun.shop.requireslevel", selectedToken.getRequiredLevel()) + "***" +
                                "\n****" + event.getString("command.fun.shop.currentlevel", profile.getLevel()) + "***" +
                                "\n***" + event.getString("command.fun.shop.price", CustomEmote.printPrice(selectedToken.getPrice())) + "***" +
                                "\n***" + event.getString("command.fun.shop.description") + "*** " + selectedToken.getDescription() +
                                "\n***" + event.getString("command.fun.shop.preview") + "***";

                        event.getChannel().sendTyping().queue();
                        try {
                            //Send the image first.
                            event.getChannel().sendFile(selectedToken.drawTokenImage(), "preview.png").queue(message -> {
                                //Set up the menu to purchase the item.
                                ButtonMenu.Builder tokenView = new ButtonMenu.Builder();

                                tokenView.setEventWaiter(KekBot.waiter);
                                tokenView.setText(info);

                                //Make sure only the user calling the command can mess with this menu.
                                tokenView.setUsers(event.getAuthor());

                                //If the user's level matches the required level, allow them to buy it.
                                if (profile.getLevel() == selectedToken.getRequiredLevel()) tokenView.addChoice("✅");

                                tokenView.addChoice("❌");

                                //Handler for choosing yes/no.
                                tokenView.setAction(emote -> {
                                    if (emote.getName().equals("✅")) {
                                        event.getChannel().sendMessage(KekBot.tokenShop.buy(KekBot.tokenShop.getInventory().get(selection - 1), event.getAuthor())).queue();
                                    }
                                    message.delete().queue();
                                });
                                tokenView.build().display(message);
                            });
                        } catch (IOException e) {
                            throwException(e, event);
                        }
                    });
                    //Number of items in this shop.
                    tokenShop.setItemsPerPage(KekBot.tokenShop.getItemsPerPage());

                    //And finally, display the shop.
                    tokenShop.build().display(event.getChannel());
                    break;
                case 2:
                    //Background Shop
                    ShopMenu.Builder backgroundShop = new ShopMenu.Builder();
                    backgroundShop.setEventWaiter(KekBot.waiter);

                    //Make sure only the user calling the command can mess with this menu.
                    backgroundShop.setUsers(event.getAuthor());

                    //Draw the shops images for this menu.
                    try {
                        backgroundShop.addImages(KekBot.backgroundShop.draw(Profile.getProfile(event.getAuthor())));
                    } catch (IOException e) {
                        throwException(e, event, "Shop Generation Error.");
                    }

                    //Number of items in the shop.
                    backgroundShop.setNumberOfItems(KekBot.backgroundShop.getInventory().size());

                    //Handler for shop items.
                    backgroundShop.setSelectionAction((msg, selection) -> {
                        //Item we selected from the shop.
                        Background selectedBackground = KekBot.backgroundShop.getInventory().get(selection - 1);

                        //User's profile.
                        Profile profile = Profile.getProfile(event.getAuthor());

                        //Item's info.
                        String info = "***" + event.getString("command.fun.shop.name") + "*** " + selectedBackground.getName() +
                                "\n***" + event.getString("command.fun.shop.requireslevel", selectedBackground.getRequiredLevel()) + "***" +
                                "\n****" + event.getString("command.fun.shop.currentlevel", profile.getLevel()) + "***" +
                                "\n***" + event.getString("command.fun.shop.price", CustomEmote.printPrice(selectedBackground.getPrice())) + "***" +
                                "\n***" + event.getString("command.fun.shop.description") + "*** " + selectedBackground.getDescription() +
                                "\n***" + event.getString("command.fun.shop.preview") + "***";

                        event.getChannel().sendTyping().queue();
                        try {
                            //Send the image first.
                            event.getChannel().sendFile(selectedBackground.drawBackgroundImage(), "preview.png").queue(message -> {
                                //Set up the menu to purchase the item.
                                ButtonMenu.Builder backgroundView = new ButtonMenu.Builder();

                                backgroundView.setEventWaiter(KekBot.waiter);
                                backgroundView.setText(info);

                                //Make sure only the user calling the command can mess with this menu.
                                backgroundView.setUsers(event.getAuthor());

                                //If the user's level matches the required level, allow them to buy it.
                                if (profile.getLevel() == selectedBackground.getRequiredLevel()) backgroundView.addChoice("✅");

                                backgroundView.addChoice("❌");

                                //Handler for choosing yes/no.
                                backgroundView.setAction(emote -> {
                                    if (emote.getName().equals("✅")) {
                                        event.getChannel().sendMessage(KekBot.backgroundShop.buy(KekBot.backgroundShop.getInventory().get(selection - 1), event.getAuthor())).queue();
                                    }
                                    message.delete().queue();
                                });
                                backgroundView.build().display(message);
                            });
                        } catch (IOException e) {
                            throwException(e, event);
                        }
                    });
                    backgroundShop.setItemsPerPage(KekBot.backgroundShop.getItemsPerPage());

                    //And finally, display the shop.
                    backgroundShop.build().display(event.getChannel());
                    break;
                case 3:
                    String url = "https://discoin.sidetrip.xyz";
                    String unauthorized = "An error has occurred. This likely is because the bot owner screwed up somewhere...\n\nTranaction Canceled.";
                    if (Config.getConfig().getDcoinToken() != null) {
                        Questionnaire.newQuestionnaire(event)
                                .addQuestion("Welcome to the Discoin Association's currency converter! You can convert all of your topkeks to currencies from other bots here!\n\nType the currency you want to convert to. (For the list of currencies, and their conversion rates, use the following link: " + url + "/rates" + ")\nYou can say `cancel` at any time to back out.", QuestionType.STRING)
                                .execute(results -> {
                                    String to = results.getAnswer(0).toString();
                                    if (to.length() == 3) {
                                        Questionnaire.newQuestionnaire(results)
                                                .addQuestion("How many topkeks do you want to convert?", QuestionType.INT)
                                                .execute(results1 -> {
                                                    int amount = (int) results1.getAnswer(0);
                                                    Profile profile = Profile.getProfile(event.getAuthor());
                                                    if (!profile.canSpend(amount)) {
                                                        event.getChannel().sendMessage("You don't have that many topkeks.\n\nTransaction Canceled.").queue();
                                                        return;
                                                    }
                                                    try {
                                                        Discoin4J.Confirmation confirmation = KekBot.discoin.makeTransaction(event.getAuthor().getId(), amount, to);
                                                        profile.spendTopKeks(amount);
                                                        profile.save();
                                                        event.getChannel().sendMessage("Done! You should be receiving `" + confirmation.getResultAmount() + "` in the currency you selected shortly." +
                                                                "\nYour reciept ID is: `" + confirmation.getReceiptCode() + "`." +
                                                                "\nToday's remaining Discoin limit for currency `" + to + "`: " + confirmation.getLimitNow()).queue();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    } catch (RejectedException e) {
                                                        switch (e.getStatus().getReason()) {
                                                            case "verify required":
                                                                event.getChannel().sendMessage("Hm, you're not verified on Discoin. You'll need to verify yourself before you can convert topkeks. You can do so here: " + url + "/verify\n\nTranaction Canceled.").queue();
                                                                break;
                                                            case "per-user limit exceeded":
                                                                event.getChannel().sendMessage("You've already converted the maximum amount of coins for today! Try again tomorrow.\n\nTranaction Canceled.").queue();
                                                                break;
                                                            case "total limit exceeded":
                                                                event.getChannel().sendMessage("Woah, this feature's been used so much, I've already transferred " + e.getStatus().getLimit() + " Discoins! I can't transfer anymore today! Check back tomorrow.\n\nTranaction Canceled.").queue();
                                                                break;
                                                            default:
                                                                e.printStackTrace();
                                                                break;
                                                        }
                                                    } catch (DiscoinErrorException e) {
                                                        event.getChannel().sendMessage("Hm, that doesn't seem like a valid currency.\n\nTranaction Canceled.").queue();
                                                    } catch (UnauthorizedException e) {
                                                        event.getChannel().sendMessage(unauthorized).queue();
                                                    } catch (UnknownErrorException e) {
                                                        event.getChannel().sendMessage("Yikes! I've found an error that shouldn't exist! Report this to the bot owner with the `ticket` command right away! `" + e.getMessage() + "`").queue();
                                                    }
                                                });
                                    } else {
                                        event.getChannel().sendMessage("That's too " + (to.length() < 3 ? "short" : "long") + ", currency IDs are 3 characters long. Try again.").queue();
                                        results.reExecuteWithoutMessage();
                                    }
                                });
                    }
                    break;
            }
        });
        builder.build().display(event.getChannel());
    }
}
