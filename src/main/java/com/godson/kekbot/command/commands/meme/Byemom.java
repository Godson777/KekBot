package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Byemom extends Command {

    public Byemom() {
        name = "byemom";
        description = "Creates your own \"OK BYE MOM\" meme.";
        usage.add("byemom <message>");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage("You must supply some text for this command!").queue();
            return;
        }

        String search = event.combineArgs();
        event.getChannel().sendTyping().queue();
        BufferedImage ava = Utils.getUserAvatarImage(event.getAuthor());
        try {
            BufferedImage template = ImageIO.read(new File("resources/memegen/byemom_template.png"));
            Graphics2D image = template.createGraphics();
            image.drawImage(ava, 523, 12, 80, 80, null);
            image.drawImage(ava, 73, 338, 128, 128, null);
            image.rotate(-0.436332);
            image.setColor(Color.black);
            Font font = new Font("Ariel", Font.PLAIN, 20);
            while (font.getStringBounds(search, image.getFontRenderContext()).getWidth() > 372 && font.getSize() > 10) {
                font = new Font("Ariel", Font.PLAIN, font.getSize() - 1);
            }
            image.setFont(font);
            image.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            image.drawString(search, 69, 701);
            image.dispose();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.flush();
            ImageIO.setUseCache(false);
            ImageIO.write(template, "png", stream);
            event.getChannel().sendFile(stream.toByteArray(), "byemom.png", null).queue();
            stream.close();
        } catch (IOException e) {
            throwException(e, event, "Image generation problem.");
        }
    }
}
