package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.core.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Johnny extends Command {

    public Johnny() {
        name = "johnny";
        description = "HEREEEE'S JOHNNY!";
        usage.add("johnny <@user>");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage("Who are you targeting?").queue();
            return;
        }

        if (!isMention(event.combineArgs()) || event.getMessage().getMentionedUsers().size() < 1) {
            event.getChannel().sendMessage("The user you want to target must be in the form of a mention!").queue();
            return;
        }

        event.getChannel().sendTyping().queue();
        User user = event.getMessage().getMentionedUsers().get(0);
        BufferedImage target = Utils.getUserAvatarImage(user);
        BufferedImage ava = Utils.getUserAvatarImage(event.getAuthor());
        try {
            BufferedImage template = ImageIO.read(Utils.getResource("memegen/johnny_template.png"));
            BufferedImage bg = new BufferedImage(template.getWidth(), template.getHeight(), template.getType());
            Graphics2D image = bg.createGraphics();
            image.drawImage(ava, 111, 218, 283, 282, null);
            image.drawImage(template, 0, 0, null);
            image.drawImage(target, 250, -8, 81, 81, null);
            image.dispose();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.flush();
            ImageIO.setUseCache(false);
            ImageIO.write(bg, "png", stream);
            event.getChannel().sendFile(stream.toByteArray(), "jahnny.png", null).queue();
            stream.close();
        } catch (IOException e) {
            throwException(e, event, "Image generation problem.");
        }
    }
}
