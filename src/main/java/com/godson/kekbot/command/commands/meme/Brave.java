package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.ImageCommand;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Brave extends ImageCommand {

    public Brave() {
        name = "brave";
        description = "I'm a brave boy!";
        usage.add("brave <attachment>");
        usage.add("brave <image URL>");
        filename = "NOT_BRAVE_ENOUGH";
        category = CommandCategories.meme;
    }

    @Override
    protected byte[] generate(BufferedImage image) throws IOException {
        BufferedImage base = ImageIO.read(new File("resources/memegen/brave.jpg"));
        Graphics2D graphics = base.createGraphics();

        double widthRatio = 892d / image.getWidth();
        double heightRatio = 1108d / image.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);

        Dimension dimension = new Dimension((int) (image.getWidth() * ratio), (int) (image.getHeight() * ratio));

        Rectangle2D r2D = new Rectangle(dimension);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int a = (892 / 2) - (rWidth / 2);
        int b = (1108 / 2) - (rHeight / 2);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(image, a, 1112+b, dimension.width, dimension.height, null);
        graphics.dispose();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.flush();
        ImageIO.setUseCache(false);
        ImageIO.write(base, "png", stream);
        byte[] finished = stream.toByteArray();
        stream.close();

        return finished;
    }
}
