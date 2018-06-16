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

public class Erase extends Command {

    public Erase() {
        name = "erase";
        description = "For really big mistakes.";
        usage.add("erase <@user>");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage(event.getString("command.meme.erase.noargs")).queue();
            return;
        }

        if (!isMention(event.combineArgs()) || event.getMentionedUsers().size() < 1) {
            event.getChannel().sendMessage(event.getString("command.meme.erase.nomention")).queue();
            return;
        }

        event.getChannel().sendTyping().queue();
        BufferedImage ava = Utils.getUserAvatarImage(event.getMentionedUsers().get(0));
        try {
            BufferedImage template = ImageIO.read(new File("resources/memegen/mistake_template.png"));
            Graphics2D image = template.createGraphics();
            image.drawImage(ava, 368, 375, 277, 270, null);
            image.dispose();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.flush();
            ImageIO.setUseCache(false);
            ImageIO.write(template, "png", stream);
            event.getChannel().sendFile(stream.toByteArray(), "erase.png", null).queue();
            stream.close();
        } catch (IOException e) {
            throwException(e, event, "Image generation problem.");
        }
    }
}
