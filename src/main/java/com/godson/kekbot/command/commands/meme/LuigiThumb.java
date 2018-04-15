package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.ImageCommand;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class LuigiThumb extends ImageCommand {

    public LuigiThumb() {
        name = "luigithumb";
        description = "Gives an image Luigi's approval.";
        usage.add("luigithumb <image URL>");
        usage.add("luigithumb <attachment>");
        category = CommandCategories.meme;
        filename = "loogi";
    }

    @Override
    protected byte[] generate(BufferedImage base) throws IOException {
        Graphics2D graphics = base.createGraphics();
        BufferedImage loogy = ImageIO.read(new File("resources/memegen/luigi_thumb.png"));
        graphics.drawImage(loogy, base.getWidth() - loogy.getWidth(), base.getHeight() - loogy.getHeight(), null);
        graphics.dispose();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.flush();
        ImageIO.setUseCache(false);
        ImageIO.write(base, "png", stream);
        byte[] image = stream.toByteArray();
        stream.close();

        return image;
    }
}
