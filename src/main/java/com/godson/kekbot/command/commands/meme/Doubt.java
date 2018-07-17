package com.godson.kekbot.command.commands.meme;


import com.godson.kekbot.util.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Doubt extends Command {

    public Doubt() {
        name = "doubt";
        description = "Displays your doubt.";
        usage.add("doubt");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        event.getChannel().sendTyping().queue();
        BufferedImage ava = Utils.getUserAvatarImage(event.getAuthor());

        try {
            BufferedImage template = ImageIO.read(new File("resources/memegen/doubt.png"));
            Graphics2D image = template.createGraphics();
            image.drawImage(ava, 0, 0, 709, 709, null);

            image.dispose();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.flush();
            ImageIO.setUseCache(false);
            ImageIO.write(template, "png", stream);
            event.getChannel().sendFile(stream.toByteArray(), "doubt.png", null).queue();
            stream.close();
        } catch (IOException e) {
            throwException(e, event, "Image Generation Problem");
        }
    }
}
