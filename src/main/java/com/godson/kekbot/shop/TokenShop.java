package com.godson.kekbot.shop;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.profile.item.Token;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TokenShop extends Shop<Token> {

    public TokenShop() throws IOException {
        super(ShelfType.SHELF_3, 80, 80, 9);
        for (Token token : Token.values()) {
            if (token.hasPrice()) addToInventory(token);
        }
        inventory.sort(Comparator.comparing(Token::getRequiredLevel).thenComparing(Token::getPrice));
    }

    public String buy(Token token, User user) {
        Profile profile = Profile.getProfile(user);
        if (token.getRequiredLevel() <= profile.getLevel()) {
            if (!profile.hasToken(token)) {
                if (profile.canSpend(token.getPrice())) {
                    profile.spendTopKeks(token.getPrice());
                    profile.addToken(token);
                    profile.save();
                    return "Purchase complete. \uD83D\uDCB0";
                } else return "You can't afford this token! This costs **" + CustomEmote.printPrice(token.getPrice()) + "**, you have **" + CustomEmote.printPrice(profile.getTopkeks()) + "**.";
            } else return "You already have this token!";
        } else return "This token requires you to be at least Level " + token.getRequiredLevel() + ". You are Level " + profile.getLevel() + ".";
    }

    @Override
    public List<byte[]> draw(Profile profile) throws IOException {
        BufferedImage locked = ImageIO.read(new File("resources/shop/lockedToken.png"));

        int maxPages = getNumberOfPages();

        BufferedImage images[] = new BufferedImage[maxPages];

        List<byte[]> entireShop = new ArrayList<>();

        //Set all images in the array to be empty shelves, we'll fill them in later.
        for (int i = 0; i < maxPages; i++) {
            images[i] = ImageIO.read(new File(shelfType.getFile()));
        }

        for (int page = 0; page < maxPages; page++) {
            //Get only the items we want to work for this page.
            List<Token> tokens = inventory.subList(itemsPerPage * page, (itemsPerPage * (page + 1) > inventory.size() ? inventory.size() : itemsPerPage * (page + 1)));
            //Can we go back a page?
            boolean prev = (page > 0);
            //Can we go to the next page?
            boolean next = (page < maxPages - 1);

            //Create graphics object responsible for editing the image.
            Graphics2D graphics = images[page].createGraphics();

            //Draw prev/next arrows if we can go back/forwards a page.
            if (prev) graphics.drawImage(prevImg, 247, 639, null);
            if (next) graphics.drawImage(nextImg, 339, 639, null);

            //The number of items we can fit in a single shelf.
            int itemsPerShelf = itemsPerPage / shelfType.getShelves();


            graphics.setColor(Color.white);
            graphics.setFont(new Font("Calibri", Font.BOLD, 16));
            //Loop for each shelf in this shop.
            int shelves = tokens.size() % itemsPerPage;
            for (int y = 0; y < (shelves == 0 ? shelfType.getShelves() : shelves) ; y++) {
                //Loop for each item on this shelf.
                for (int x = 0; x < (tokens.size() / (y + 1) < itemsPerShelf ? tokens.size() - (y * itemsPerShelf) : itemsPerShelf); x++) {
                    //Draw the item in the appropriate X and Y cords.
                    graphics.drawImage(tokens.get(x + (y * itemsPerShelf)).drawToken(), 70 + (125 * x), 226 + (130 * y), width, height, null);
                    //If the user can't buy this item, draw the locked icon over it.
                    if (tokens.get(x + (y * itemsPerShelf)).getRequiredLevel() > profile.getLevel()) graphics.drawImage(locked, 70 + (125 * x), 226 + (130 * y),null);
                    //Draw the price near the item.
                    graphics.drawImage(topkek, 40 + (125 * x), 196 + (130 * y), null);
                    graphics.drawString(String.valueOf(tokens.get(x + (y * itemsPerShelf)).getPrice()), 85 + (125 * x),215 + (130 * y));
                    //Draw the item's ID.
                    graphics.drawString(String.valueOf(((x + (y * itemsPerShelf)) + 1)), 53 + (125 * x), 257 + (130 * y));
                }
            }
            //Finish everything and convert to byte array.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(images[page], "png", outputStream);
            byte[] image = outputStream.toByteArray();
            outputStream.flush();
            outputStream.close();
            graphics.dispose();

            entireShop.add(image);
        }

        return entireShop;
    }
}
