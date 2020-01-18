package com.godson.kekbot.command.commands.fun;

import com.godson.discoin4j.Discoin4J;
import com.godson.discoin4j.exceptions.GenericErrorException;
import com.godson.discoin4j.exceptions.UnauthorizedException;
import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.menu.PagedSelectionMenu;
import com.godson.kekbot.menu.ShopMenu;
import com.godson.kekbot.profile.item.Background;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.profile.item.Token;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.settings.Config;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.math3.util.Precision;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    public void onExecuted(CommandEvent event) throws Throwable {
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
                    tokenShop.wrapPageEnds(true);

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
                                if (profile.getLevel() >= selectedToken.getRequiredLevel()) tokenView.addChoice("✅");

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
                    backgroundShop.wrapPageEnds(true);

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
                                if (profile.getLevel() >= selectedBackground.getRequiredLevel()) backgroundView.addChoice("✅");

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
                    String url = "https://dash.discoin.zws.im/#/";
                    String unauthorized = "An error has occurred. This likely is because the bot owner screwed up somewhere...\n\nTranaction Canceled.";
                    if (Config.getConfig().getDcoinToken() != null) {
                        try {
                            PagedSelectionMenu.Builder discoinBuilder = new PagedSelectionMenu.Builder();
                            discoinBuilder.setEventWaiter(KekBot.waiter);
                            List<Discoin4J.Currency> currencies = KekBot.discoin.getCurrencies();
                            double kekValue = currencies.stream().filter(c -> c.getId().equals("KEK")).findFirst().get().getValue();
                            discoinBuilder.addChoices(currencies.stream().filter(c -> !c.getId().equals("KEK")).sorted(Comparator.comparing(Discoin4J.Currency::getId)).map(currency -> currency.getId() + " - " + currency.getName() + " - 1 " + CustomEmote.printTopKek() + " = " + Precision.round(kekValue / currency.getValue(), 2)).toArray(String[]::new));
                            discoinBuilder.wrapPageEnds(true);
                            discoinBuilder.setItemsPerPage(10);
                            discoinBuilder.setText("Welcome to the Discoin Association's currency converter! You can convert your topkeks to another bot's currency, and vice versa!\n\nSelect the currency you wish to convert to.");
                            discoinBuilder.addUsers(event.getAuthor());
                            discoinBuilder.setSelectionAction((me, currency) -> {
                                Questionnaire.newQuestionnaire(event)
                                        .addQuestion("How many topkeks do you want to convert?", QuestionType.DOUBLE)
                                        .includeCancel(true)
                                        .execute(results -> {
                                            double amount = results.getAnswerAsType(0, double.class);
                                            Profile profile = Profile.getProfile(event.getAuthor());
                                            if (!profile.canSpend(amount)) {
                                                event.getChannel().sendMessage("You don't have that many topkeks.\n\nTransaction Canceled.").queue();
                                                return;
                                            }
                                            try {
                                                Discoin4J.Transaction transaction = KekBot.discoin.makeTransaction(event.getAuthor().getId(), amount, currencies.stream().filter(c -> !c.getId().equals("KEK")).sorted(Comparator.comparing(Discoin4J.Currency::getId)).collect(Collectors.toList()).get(currency - 1).getId());
                                                profile.spendTopKeks(amount);
                                                profile.save();
                                                EmbedBuilder embedBuilder = new EmbedBuilder();
                                                embedBuilder.setDescription("Done! You should be receiving `" + transaction.getId() + "` in the currency you selected shortly." +
                                                        "\n[You can check your receipt by clicking on me!](" + url + "transactions/" + transaction.getId() + ")");
                                                event.getChannel().sendMessage(embedBuilder.build()).queue();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (UnauthorizedException e) {
                                                event.getChannel().sendMessage(unauthorized).queue();
                                            } catch (GenericErrorException e) {
                                                event.getChannel().sendMessage("Yikes! I've found an error that shouldn't exist! Report this to the bot owner with the `ticket` command right away! `" + e.getMessage() + "`").queue();
                                                throwException(e, event);
                                            }
                                        });
                            });
                            discoinBuilder.build().display(event.getChannel());
                        } catch (IOException e) {
                            throwException(e, event);
                        } catch (UnauthorizedException e) {
                            event.getChannel().sendMessage(unauthorized).queue();
                        } catch (GenericErrorException e) {
                            event.getChannel().sendMessage("Yikes! I've found an error that shouldn't exist! Report this to the bot owner with the `ticket` command right away! `" + e.getMessage() + "`").queue();
                        }
                    }
                    break;
            }
        });
        builder.build().display(event.getChannel());
    }
}
