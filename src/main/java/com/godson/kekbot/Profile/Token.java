package com.godson.kekbot.Profile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public enum Token {
    //UNOBTAINABLE TOKENS
    GRAND_DAD("GRAND DAD", "granddad.png"),

    //OBTAINABLE TOKENS
    SNEK("Snek", "snek.png", 1, 250),
    KAPPA("Kappa", "kappa.png", 1, 500),
    DOGECOIN("Doge Coin", "dogecoin.png", 1, 500, "From the depths of the internet, Dogecoins were invented in an attempt to be more popular than Bitcoin. Whether or not if it succeeded is still a mystery. But at least you can use one as a badge.");

    private String name;
    private String file;
    private int requiredLevel;
    private Integer price;
    private String description;

    Token(String name, String file, int requiredLevel, int price) {
        this.name = name;
        this.file = file;
        this.requiredLevel = requiredLevel;
        this.price = price;
    }

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

    public int getPrice() {
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
