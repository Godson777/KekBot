package com.godson.kekbot.profile.item;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Background {

    private String ID;
    private String name;
    private String file;
    private int requiredLevel;
    private Integer price;
    private String description;


    Background(String ID, String name, String file, int requiredLevel, int price) {
        this.ID = ID;
        this.name = name;
        this.file = file;
        this.requiredLevel = requiredLevel;
        this.price = price;
    }

    Background(String ID, String name, String file) {
        this.ID = ID;
        this.name = name;
        this.file = file;
    }

    Background(String ID, String name, String file, int requiredLevel, int price, String description) {
        this.ID = ID;
        this.name = name;
        this.file = file;
        this.requiredLevel = requiredLevel;
        this.price = price;
        this.description = description;
    }

    public BufferedImage drawBackground() throws IOException {
        return ImageIO.read(new File("resources/profile/background/" + file));
    }

    public byte[] drawBackgroundImage() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.flush();
        ImageIO.write(drawBackground(), "png", outputStream);
        byte[] image = outputStream.toByteArray();
        outputStream.close();
        return image;
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

    public String getID() {
        return ID;
    }
}
