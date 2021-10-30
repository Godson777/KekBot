package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.TextImageCommand;
import com.godson.kekbot.util.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Doggo extends TextImageCommand {

    public Doggo() {
        name = "doggo";
        description = "Doggo displays your text/image on his board!";
        usage.add("doggo <attachment>");
        usage.add("doggo <image URL>");
        usage.add("doggo <text>");
        filename = "doggo";
        category = CommandCategories.meme;
    }

    @Override
    protected byte[] generate(BufferedImage image) throws IOException {
        BufferedImage base = ImageIO.read(new File("resources/memegen/doggo.jpg"));
        Graphics2D graphics = base.createGraphics();

        double widthRatio = 376d / image.getWidth();
        double heightRatio = 299d / image.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);

        Dimension dimension = new Dimension((int) (image.getWidth() * ratio), (int) (image.getHeight() * ratio));

        Rectangle2D r2D = new Rectangle(dimension);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int a = (376 / 2) - (rWidth / 2);
        int b = (299 / 2) - (rHeight / 2);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.white);
        graphics.drawImage(image, 135+a, 57+b, dimension.width, dimension.height, null);
        graphics.dispose();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.flush();
        ImageIO.setUseCache(false);
        ImageIO.write(base, "png", stream);
        byte[] finished = stream.toByteArray();
        stream.close();

        return finished;
    }

    @Override
    protected byte[] generate(String string) throws IOException {
        BufferedImage template = ImageIO.read(new File("resources/memegen/doggo.jpg"));
        Graphics2D graphics = template.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setFont(new Font("Calibri", Font.BOLD, 40));
        graphics.setColor(Color.black);

        if (!ImageUtils.drawCenteredString(graphics, string, 135, 57, 376, 299)) throw new IllegalArgumentException("String too long pls");

        graphics.dispose();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.flush();
        ImageIO.setUseCache(false);
        ImageIO.write(template, "png", stream);
        byte[] finished = stream.toByteArray();
        stream.close();

        return finished;
    }
}
