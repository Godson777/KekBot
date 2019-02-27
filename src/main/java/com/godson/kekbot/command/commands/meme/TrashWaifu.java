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

public class TrashWaifu extends ImageCommand {

    public TrashWaifu() {
        name = "trashwaifu";
        description = "Your waifu is entry level garbage!";
        usage.add("trashwaifu <attachment>");
        usage.add("trashwaifu <image URL>");
        category = CommandCategories.meme;
    }

    @Override
    protected byte[] generate(BufferedImage image) throws IOException {
        BufferedImage base = ImageIO.read(new File("resources/memegen/trash_waifu.png"));
        BufferedImage blank = new BufferedImage(base.getWidth(), base.getHeight(), base.getType());
        Graphics2D graphics = blank.createGraphics();

        double widthRatio = 113d / image.getWidth();
        double heightRatio = 151d / image.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);

        Dimension dimension = new Dimension((int) (image.getWidth() * ratio), (int) (image.getHeight() * ratio));

        Rectangle2D r2D = new Rectangle(dimension);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int a = (113 / 2) - (rWidth / 2);
        int b = (151 / 2) - (rHeight / 2);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.rotate(Math.toRadians(18.89), base.getWidth() / 2, base.getHeight() / 2);
        graphics.setColor(Color.white);
        graphics.fillRect(153, 198, 118, 158);
        graphics.drawImage(image, 153 + a, 198 + b, dimension.width, dimension.height, null);
        graphics.rotate(Math.toRadians(-18.89), base.getWidth() / 2, base.getHeight() / 2);
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
