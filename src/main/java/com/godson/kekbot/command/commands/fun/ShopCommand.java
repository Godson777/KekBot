package com.godson.kekbot.command.commands.fun;

import com.godson.discoin4j.Discoin4J;
import com.godson.discoin4j.exceptions.DiscoinErrorException;
import com.godson.discoin4j.exceptions.RejectedException;
import com.godson.discoin4j.exceptions.UnauthorizedException;
import com.godson.discoin4j.exceptions.UnknownErrorException;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.profile.Background;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.profile.Token;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.settings.Config;
import net.dv8tion.jda.core.MessageBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ShopCommand extends Command {

    public ShopCommand() {
        name = "shop";
        description = "Opens up the profile shop.";
        usage.add("shop <category>");
        usage.add("shop <category> <page>");
        usage.add("shop buy <category> <itemID>");
        usage.add("shop info <category> <itemID>");
        if (Config.getConfig().getDcoinToken() != null) usage.add("shop convert");
        extendedDescription = "\nAvailable Categories:\nTokens\nBackgrounds\n\n#Notes:\nArrows signify other pages in a shop.\nCrossed out items require you to be a higher level. You can find out what level is required by using {p}shop info <category> <itemID>.";
        exDescPos = ExtendedPosition.AFTER;
        category = new Category("Fun");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        String missingArgs = "Missing arguments, check `" + event.getPrefix() + "help shop` to get more info.";
        if (event.getArgs().length == 0) {
            event.getChannel().sendMessage(missingArgs).queue();
        } else {
            switch (event.getArgs()[0].toLowerCase()) {
                case "token":
                case "tokens":
                    List<Token> tokenShop = KekBot.tokenShop.getInventory();
                    int tokenShopPage;
                    if (event.getArgs().length < 2) tokenShopPage = 0;
                    else {
                        try {
                            tokenShopPage = Integer.valueOf(event.getArgs()[1]) - 1;
                        } catch (NumberFormatException e) {
                            event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + event.getArgs()[1] + "`")).queue();
                            break;
                        }
                    }
                    try {
                        if ((tokenShopPage * 9) >= tokenShop.size() || (tokenShopPage * 9) < 0) {
                            event.getChannel().sendMessage("That page doesn't exist!").queue();
                        } else {
                            event.getChannel().sendTyping().queue();
                            event.getChannel().sendFile(drawTokenShop(Profile.getProfile(event.getAuthor()), tokenShop.subList(tokenShopPage * 9, ((tokenShopPage + 1) * 9 <= tokenShop.size() ? (tokenShopPage + 1) * 9 : tokenShop.size())), tokenShopPage > 0, (tokenShopPage + 1) * 9 < tokenShop.size(), tokenShopPage), "tokenshop.png", null).queue();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "background":
                case "backgrounds":
                    List<Background> backgroundShop = KekBot.backgroundShop.getInventory();
                    int backgroundShopPage;
                    if (event.getArgs().length < 2) backgroundShopPage = 0;
                    else {
                        try {
                            backgroundShopPage = Integer.valueOf(event.getArgs()[1]) - 1;
                        } catch (NumberFormatException e) {
                            event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + event.getArgs()[1] + "`")).queue();
                            break;
                        }
                    }
                    try {
                        if ((backgroundShopPage * 6) >= backgroundShop.size() || (backgroundShopPage * 6) < 0) {
                            event.getChannel().sendMessage("That page doesn't exist!").queue();
                        } else {
                            event.getChannel().sendTyping().queue();
                            event.getChannel().sendFile(drawBackgroundShop(Profile.getProfile(event.getAuthor()), backgroundShop.subList(backgroundShopPage * 6, ((backgroundShopPage + 1) * 6 <= backgroundShop.size() ? (backgroundShopPage + 1) * 6 : backgroundShop.size())), backgroundShopPage > 0, (backgroundShopPage + 1) * 6 < backgroundShop.size(), backgroundShopPage), "backgroundshop.png", null).queue();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "buy":
                    if (event.getArgs().length < 2)
                        event.getChannel().sendMessage(missingArgs).queue();
                    else {
                        switch (event.getArgs()[1]) {
                            case "token":
                            case "tokens":
                                if (event.getArgs().length < 3)
                                    event.getChannel().sendMessage(missingArgs).queue();
                                else {
                                    try {
                                        event.getChannel().sendMessage(KekBot.tokenShop.buy(KekBot.tokenShop.getInventory().get(Integer.valueOf(event.getArgs()[2]) - 1), event.getAuthor())).queue();
                                    } catch (NumberFormatException e) {
                                        event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + event.getArgs()[2] + "`")).queue();
                                    }
                                }
                                break;
                            case "background":
                            case "backgrounds":
                                if (event.getArgs().length < 3)
                                    event.getChannel().sendMessage(missingArgs).queue();
                                else {
                                    try {
                                        event.getChannel().sendMessage(KekBot.backgroundShop.buy(KekBot.backgroundShop.getInventory().get(Integer.valueOf(event.getArgs()[2]) - 1), event.getAuthor())).queue();
                                    } catch (NumberFormatException e) {
                                        event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + event.getArgs()[2] + "`")).queue();
                                    }
                                }
                                break;
                            default:
                                event.getChannel().sendMessage(missingArgs).queue();
                        }
                    }
                    break;
                case "info":
                    if (event.getArgs().length < 2)
                        event.getChannel().sendMessage(missingArgs).queue();
                    else {
                        switch (event.getArgs()[1].toLowerCase()) {
                            case "token":
                            case "tokens":
                                if (event.getArgs().length < 3)
                                    event.getChannel().sendMessage(missingArgs).queue();
                                else {
                                    try {
                                        Token selectedToken = KekBot.tokenShop.getInventory().get(Integer.valueOf(event.getArgs()[2]) - 1);
                                        String tokenInfo = "***Name:*** " + selectedToken.getName() +
                                                "\n***Requires Level " + selectedToken.getRequiredLevel() + ".***" +
                                                "\n***Description:*** " + selectedToken.getDescription() +
                                                "\n***Preview:***";
                                        event.getChannel().sendTyping().queue();
                                        event.getChannel().sendFile(selectedToken.drawTokenImage(), "preview.png", new MessageBuilder().append(tokenInfo).build()).queue();
                                    } catch (NumberFormatException e) {
                                        event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + event.getArgs()[2] + "`")).queue();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (IndexOutOfBoundsException e) {
                                        event.getChannel().sendMessage("There is no item in this shop with that ID.").queue();
                                    }
                                }
                                break;
                            case "background":
                            case "backgrounds":
                                if (event.getArgs().length < 3)
                                    event.getChannel().sendMessage(missingArgs).queue();
                                else {
                                    try {
                                        Background selectedBackground = KekBot.backgroundShop.getInventory().get(Integer.valueOf(event.getArgs()[2]) - 1);
                                        String backgroundInfo = "***Name:*** " + selectedBackground.getName() +
                                                "\n***Requires Level " + selectedBackground.getRequiredLevel() + ".***" +
                                                "\n***Description:*** " + selectedBackground.getDescription() +
                                                "\n***Preview:***";
                                        event.getChannel().sendTyping().queue();
                                        event.getChannel().sendFile(selectedBackground.drawBackgroundImage(), "preview.png", new MessageBuilder().append(backgroundInfo).build()).queue();
                                    } catch (NumberFormatException e) {
                                        event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + event.getArgs()[2] + "`")).queue();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (IndexOutOfBoundsException e) {
                                        event.getChannel().sendMessage("There is no item in this shop with that ID.").queue();
                                    }
                                }
                                break;
                            default:
                                event.getChannel().sendMessage(missingArgs).queue();
                        }
                    }
                    break;
                case "convert":
                    String url = "https://discoin.sidetrip.xyz/rates";
                    String unauthorized = "An error has occurred. This likely is because the bot owner screwed up somewhere...\n\nTranaction Canceled.";
                    if (Config.getConfig().getDcoinToken() != null) {
                        new Questionnaire(event)
                                .addQuestion("Welcome to the Discoin Association's currency converter! You can convert all of your topkeks to currencies from other bots here!\n\nType the currency you want to convert to. (For the list of currencies, and their conversion rates, use the following link: " + url + ")\nYou can say `cancel` at any time to back out.", QuestionType.STRING)
                                .execute(results -> {
                                    String to = results.getAnswer(0).toString();
                                    if (to.length() == 3) {
                                        new Questionnaire(results)
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
        }
    }

    private byte[] drawTokenShop(Profile profile, List<Token> tokens, boolean prev, boolean next, int offset) throws IOException {
        BufferedImage shop3Shelf = ImageIO.read(new File("resources/shop/3shelf.png"));
        BufferedImage prevImg = ImageIO.read(new File("resources/shop/prev.png"));
        BufferedImage nextImg = ImageIO.read(new File(("resources/shop/next.png")));
        BufferedImage topkek = ImageIO.read(new File("resources/shop/topkek.png"));
        BufferedImage locked = ImageIO.read(new File("resources/shop/lockedToken.png"));
        Graphics2D graphics = shop3Shelf.createGraphics();
        if (prev) graphics.drawImage(prevImg, 247, 639, null);
        if (next) graphics.drawImage(nextImg, 339, 639, null);
        graphics.setColor(Color.white);
        graphics.setFont(new Font("Calibri", Font.BOLD, 16));
        for (int y = 0; y < Math.ceil((double) tokens.size() / 3d); y++) {
            for (int x = 0; x < (tokens.size() / (y + 1) < 3 ? tokens.size() - (y * 3) : 3); x++) {
                graphics.drawImage(tokens.get(x + (y * 3)).drawToken(), 70 + (125 * x), 226 + (130 * y), 80, 80, null);
                if (tokens.get(x + (y * 3)).getRequiredLevel() > profile.getLevel()) graphics.drawImage(locked, 70 + (125 * x), 226 + (130 * y),null);
                graphics.drawImage(topkek, 40 + (125 * x), 196 + (130 * y), null);
                graphics.drawString(String.valueOf(tokens.get(x + (y * 3)).getPrice()), 85 + (125 * x),215 + (130 * y));
                graphics.drawString(String.valueOf(((x + (y * 3)) + 1) + (offset * 9)), 53 + (125 * x), 257 + (130 * y));
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(shop3Shelf, "png", outputStream);
        byte[] image = outputStream.toByteArray();
        outputStream.flush();
        outputStream.close();
        return image;
    }

    private byte[] drawBackgroundShop(Profile profile, List<Background> backgrounds, boolean prev, boolean next, int offset) throws IOException {
        BufferedImage shop3Shelf = ImageIO.read(new File("resources/shop/3shelf.png"));
        BufferedImage prevImg = ImageIO.read(new File("resources/shop/prev.png"));
        BufferedImage nextImg = ImageIO.read(new File(("resources/shop/next.png")));
        BufferedImage topkek = ImageIO.read(new File("resources/shop/topkek.png"));
        BufferedImage locked = ImageIO.read(new File("resources/shop/lockedBackground.png"));
        Graphics2D graphics = shop3Shelf.createGraphics();
        if (prev) graphics.drawImage(prevImg, 247, 639, null);
        if (next) graphics.drawImage(nextImg, 339, 639, null);
        graphics.setColor(Color.white);
        graphics.setFont(new Font("Calibri", Font.BOLD, 16));
        for (int y = 0; y < Math.ceil((double) backgrounds.size() / 2d); y++) {
            for (int x = 0; x < (backgrounds.size() / (y + 1) < 2 ? backgrounds.size() - (y * 2) : 2); x++) {
                graphics.drawImage(backgrounds.get(x + (y * 2)).drawBackground(), 75 + (166 * x), 205 + (130 * y), 156, 94, null);
                if (backgrounds.get(x + (y * 2)).getRequiredLevel() > profile.getLevel()) graphics.drawImage(locked, 75 + (166 * x), 205 + (130 * y),null);
                graphics.drawImage(topkek, 4 + (393 * x), 199 + (130 * y), null);
                graphics.drawString(String.valueOf(backgrounds.get(x + (y * 2)).getPrice()), 40 + (393 * x),220 + (130 * y));
                graphics.drawString(String.valueOf(((x + (y * 2)) + 1) + (offset * 6) ), 32 + (393 * x), 252 + (130 * y));
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(shop3Shelf, "png", outputStream);
        byte[] image = outputStream.toByteArray();
        outputStream.flush();
        outputStream.close();
        return image;
    }
}
