package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.util.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class LongLive extends Command {

    public LongLive() {
        name = "longlive";
        description = "LONG LIVE THE KING!";
        usage.add("longlive <@user>");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage(event.getString("command.meme.noargs")).queue();
            return;
        }

        if (!isMention(event.combineArgs()) || event.getMentionedUsers().size() < 1) {
            event.getChannel().sendMessage(event.getString("command.meme.nomention")).queue();
            return;
        }

        event.getChannel().sendTyping().queue();
        User user = event.getMentionedUsers().get(0);
        BufferedImage target = Utils.getUserAvatarImage(user);
        BufferedImage ava = Utils.getUserAvatarImage(event.getAuthor());
        try {
            BufferedImage template = ImageIO.read(new File("resources/memegen/longlivetheking_template.png"));
            Graphics2D image = template.createGraphics();
            image.drawImage(ava, 1026, 42, 479, 479, null);
            image.drawImage(target, 503, 558, 442, 442, null);
            image.dispose();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.flush();
            ImageIO.setUseCache(false);
            ImageIO.write(template, "png", stream);
            event.getChannel().sendFile(stream.toByteArray(), "theking.png", null).queue();
            stream.close();
        } catch (IOException e) {
            throwException(e, event, "Image generation problem.");
        }
    }
}
