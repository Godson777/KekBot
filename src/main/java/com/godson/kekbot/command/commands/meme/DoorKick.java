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

public class DoorKick extends ImageCommand {

    public DoorKick() {
        name = "doorkick";
        description = "";
        usage.add("doorkick <attachment>");
        usage.add("doorkick <image URL>");
        category = CommandCategories.meme;
    }

    @Override
    protected byte[] generate(BufferedImage image) throws IOException {
        BufferedImage base = ImageIO.read(new File("resources/memegen/door.png"));
        BufferedImage blank = new BufferedImage(base.getWidth(), base.getHeight(), base.getType());
        Graphics2D graphics = blank.createGraphics();

        double widthRatio = 338d / image.getWidth();
        double heightRatio = 466d / image.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);

        Dimension dimension = new Dimension((int) (image.getWidth() * ratio), (int) (image.getHeight() * ratio));

        Rectangle2D r2D = new Rectangle(dimension);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int a = (338 / 2) - (rWidth / 2);
        int b = (466 / 2) - (rHeight / 2);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(image, 326 + a, 7 + b, dimension.width, dimension.height, null);
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
