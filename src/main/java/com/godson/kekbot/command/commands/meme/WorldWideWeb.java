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

public class WorldWideWeb extends ImageCommand {

    public WorldWideWeb() {
        name = "www";
        description = "Thanks to the miracle of the world wide web, I can search anything I want!";
        usage.add("www <attachment>");
        usage.add("www <image URL>");
        category = CommandCategories.meme;
        filename = "marvelous";
    }

    @Override
    protected byte[] generate(BufferedImage image) throws IOException {
        BufferedImage base = ImageIO.read(new File("resources/memegen/www.png"));
        BufferedImage blank = new BufferedImage(base.getWidth(), base.getHeight(), base.getType());
        Graphics2D graphics = blank.createGraphics();

        double widthRatio = 165d / image.getWidth();
        double heightRatio = 131d / image.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);

        Dimension dimension = new Dimension((int) (image.getWidth() * ratio), (int) (image.getHeight() * ratio));

        Rectangle2D r2D = new Rectangle(dimension);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int a = (165 / 2) - (rWidth / 2);
        int b = (131 / 2) - (rHeight / 2);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.white);
        graphics.fillRect(132, 466, 165, 131);
        graphics.drawImage(image, 132 + a, 466 + b, dimension.width, dimension.height, null);
        graphics.drawImage(base, 0, 0, null);
        graphics.dispose();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.flush();
        ImageIO.setUseCache(false);
        ImageIO.write(blank, "png", stream);
        byte[] finished = stream.toByteArray();
        stream.close();

        return finished;
    }
}
