package com.godson.kekbot.Profile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public enum Background {
    //UNOBTAINABLE BACKGROUNDS
    GRAND_DAD("GRAND DAD", "GRAND.png"),

    //OBTAINABLE BACKGROUNDS
    DRAWN_ML("Drawn Series: Mario & Luigi", "DRAWN_M&L.png", 1, 250),
    DRAWN_WINDWAKER("Drawn Series: Wind Waker", "DRAWN_WINDWAKER", 1, 250, "");

    private String name;
    private String file;
    private int requiredLevel;
    private Integer price;
    private String description;

    Background(String name, String file, int requiredLevel, int price) {
        this.name = name;
        this.file = file;
        this.requiredLevel = requiredLevel;
        this.price = price;
    }

    Background(String name, String file) {
        this.name = name;
        this.file = file;
    }

    Background(String name, String file, int requiredLevel, int price, String description) {
        this.name = name;
        this.file = file;
        this.requiredLevel = requiredLevel;
        this.price = price;
        this.description = description;
    }

    public BufferedImage drawBackground() throws IOException {
        return ImageIO.read(new File("resources/profile/background/" + file));
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
