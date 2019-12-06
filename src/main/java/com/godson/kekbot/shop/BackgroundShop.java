package com.godson.kekbot.shop;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.profile.item.Background;
import com.godson.kekbot.profile.Profile;
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

public class BackgroundShop extends Shop<Background> {

    public BackgroundShop() throws IOException {
        super(ShelfType.SHELF_3,94,156, 6);
        for (Background background : KekBot.backgroundManager.getBackgrounds()) {
            if (background.hasPrice()) addToInventory(background);
        }
        getInventory().sort(Comparator.comparing(Background::getRequiredLevel).thenComparing(Background::getPrice));
    }

    public String buy(Background background, User user) {
        Profile profile = Profile.getProfile(user);
        if (background.getRequiredLevel() <= profile.getLevel()) {
            if (!profile.hasBackground(background)) {
                if (profile.canSpend(background.getPrice())) {
                    profile.spendTopKeks(background.getPrice());
                    profile.addBackground(background);
                    profile.save();
                    return "Purchase complete. \uD83D\uDCB0";
                } else
                    return "You can't afford this background! This costs **" + CustomEmote.printPrice(background.getPrice()) + "**, you have **" + CustomEmote.printPrice(profile.getTopkeks()) + "**.";
            } else return "You already have this background!";
        } else return "This background requires you to be at least Level " + background.getRequiredLevel() + ". You are Level " + profile.getLevel() + ".";
    }

    @Override
    public List<byte[]> draw(Profile profile) throws IOException {
        BufferedImage locked = ImageIO.read(new File("resources/shop/lockedBackground.png"));

        int maxPages = getNumberOfPages();

        BufferedImage images[] = new BufferedImage[maxPages];

        List<byte[]> entireShop = new ArrayList<>();

        //Set all images in the array to be empty shelves, we'll fill them in later.
        for (int i = 0; i < maxPages; i++) {
            images[i] = ImageIO.read(new File(shelfType.getFile()));
        }

        for (int page = 0; page < maxPages; page++) {
            //Get only the items we want to work for this page.
            List<Background> backgrounds = inventory.subList(itemsPerPage * page, (itemsPerPage * (page + 1) > inventory.size() ? inventory.size() : itemsPerPage * (page + 1)));
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
            int shelves = backgrounds.size() % itemsPerPage;
            for (int y = 0; y < (shelves == 0 ? shelfType.getShelves() : shelves); y++) {
                //Loop for each item on this shelf.
                for (int x = 0; x < (backgrounds.size() / (y + 1) < itemsPerShelf ? backgrounds.size() - (y * itemsPerShelf) : itemsPerShelf); x++) {
                    //Draw the item in the appropriate X and Y cords.
                    graphics.drawImage(backgrounds.get(x + (y * itemsPerShelf)).drawBackground(), 75 + (166 * x), 205 + (130 * y), width, height, null);
                    //If the user can't buy this item, draw the locked icon over it.
                    if (backgrounds.get(x + (y * itemsPerShelf)).getRequiredLevel() > profile.getLevel()) graphics.drawImage(locked, 75 + (166 * x), 205 + (130 * y),null);
                    //Draw the price near the item.
                    graphics.drawImage(topkek, 4 + (393 * x), 199 + (130 * y), null);
                    graphics.drawString(String.valueOf(backgrounds.get(x + (y * itemsPerShelf)).getPrice()), 40 + (393 * x),220 + (130 * y));
                    //Draw the item's ID.
                    graphics.drawString(String.valueOf(((x + (y * itemsPerShelf)) + 1)), 32 + (393 * x), 252 + (130 * y));
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
