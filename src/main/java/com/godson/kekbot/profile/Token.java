package com.godson.kekbot.profile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public enum Token {
    //UNOBTAINABLE TOKENS
    GRAND_DAD("GRAND DAD", "granddad.png"),

    //OBTAINABLE TOKENS
    SNEK("Snek", "snek.png", 1, 250, "Aw, how did this cute little snek get here? ...What? You want it? I mean, I guess we could sell it to you..."),
    KAPPA("Kappa", "kappa.png", 1, 500, "Originating from Twitch, this emote has been used to convey sarcasm and trollish behavior. Now, it can also be used as your token!"),
    DOGECOIN("Doge Coin", "dogecoin.png", 1, 500, "From the depths of the internet, Dogecoins were invented in an attempt to be more popular than Bitcoin. Whether or not if it succeeded is still a mystery. But at least you can use one as a badge."),
    DERP("Derp", "derp.png", 1, 400, "Have you ever had a moment where you just completely deadpan with a derpy look? Well, that's this token in a nutshell."),
    MUSHROOM_1UP("1-UP Mushroom", "1up.png", 2, 350, "A 1-UP Mushroom, based on its appearance from the SNES game \"Super Mario World\". (Note: This token does not give an extra life.)"),
    MUSHROOM_1UP_PAPER("Paper 1-UP Mushroom", "paper_1up.png", 2, 200, "A 1-UP Mushroom, based on its appearance from the \"Paper Mario\" series. Although since we can easily print these out, we don't see a reason why they can't be affordable."),
    EYES("Eyes Emote", "eyes.png", 3, 600, "An emote commonly used on both Discord and Twitter. Except now it's also a token. If that's your kinda thing, anyway..."),
    NICK_CAGE("Nicholas Cage", "cage.png", 3, 1000, "We loved Nicholas Cage so much, we made a coin with his face on it, and then dipped it in gold... That counts as a valid token, right?"),
    POOSY("Poosy Destroyer", "poosy.png", 1, 200, "Based off a quick meme from Joel, and a command that's used quite often. You can now destroy poosies all over with this token. (NOTE: Does not actually help you destroy any poosies.)"),
    MARIO_COIN("Coin (Mario Bros)", "mario_coin.png", 3, 600, "It's a coin, based on the world of \"Super Mario\"! It's worthless in *this* world, but we think it'll have some use for you..."),
    MARIO_COIN_8BIT("Coin (8-Bit Mario)", "mario_coin_8bit.png", 3, 450, "It's a coin from the \"Super Mario\" world alright, except it kinda just... turned 8-Bit. I know, right? Weird. I don't think it'll have much use for us here."),
    GOLDEN_MUSHROOM("Golden Mushroom", "golden_mushroom.png", 10, 1000, "It's a golden mushroom! These things are from \"New Super Mario Bros. 2\", and since they're hard to come by, we're only letting the best of the best get their hands on these...for a price."),
    STAR_COIN("Star Coin", "star_coin.png", 5, 500, "It's a star coin. These are usually hidden all over \"New Super Mario Bros\" games. Because they're hard to find, we only allow super stars get their hands on these."),
    STARMAN("Starman", "starman.png", 5, 500, "It's a starman. The use for these things vary from powerups to collectables withn the \"Super Mario\" universe. Looks like we found a use for it here, too..."),
    BUP("BUP", "BUP.png", 1, 250, "BUP."),
    WOODMAN("mm2wood", "WOODMAN.png", 3, 300, "nice."),
    TJYOSHI("TJ \"Henry\" Yoshi", "TJYOSHI.png", 1, 350, "An A press is an A press, you can't say it's only half...");

    private String name;
    private String file;
    private int requiredLevel;
    private Integer price;
    private String description;

    Token(String name, String file) {
        this.name = name;
        this.file = file;
    }

    Token(String name, String file, int requiredLevel, int price, String description) {
        this.name = name;
        this.file = file;
        this.requiredLevel = requiredLevel;
        this.price = price;
        this.description = description;
    }

    public BufferedImage drawToken() throws IOException {
        return ImageIO.read(new File("resources/profile/token/" + file));
    }

    public byte[] drawTokenImage() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(drawToken(), "png", outputStream);
        return outputStream.toByteArray();
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasPrice() {
        return price != null;
    }
}
