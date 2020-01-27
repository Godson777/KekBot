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

public class GayBabyJail extends Command {

    public GayBabyJail() {
        name = "gaybabyjail";
        aliases = new String[]{"gbj"};
        description = "Sends the target of your choosing to gay baby jail.";
        category = CommandCategories.meme;
        usage.add("gaybabyjail <@user>");
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

        for(int y = 0; y < target.getHeight(); y++){
            for(int x = 0; x < target.getWidth(); x++){
                int p = target.getRGB(x,y);

                int a = (p>>24)&0xff;
                int r = (p>>16)&0xff;
                int g = (p>>8)&0xff;
                int b = p&0xff;

                //calculate average
                int avg = (r+g+b)/3;

                //replace RGB value with avg
                p = (a<<24) | (avg<<16) | (avg<<8) | avg;

                target.setRGB(x, y, p);
            }
        }
        try {
            BufferedImage template = new BufferedImage(640, 600, BufferedImage.TYPE_INT_ARGB);
            Graphics2D image = template.createGraphics();
            image.drawImage(target, 109, 169, 430, 430, null);
            image.drawImage(ImageIO.read(new File("resources/memegen/gbj.png")), 0, 0, null);
            image.dispose();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.flush();
            ImageIO.setUseCache(false);
            ImageIO.write(template, "png", stream);
            event.getChannel().sendFile(stream.toByteArray(), "gbj.png").queue();
            stream.close();
        } catch (IOException e) {
            throwException(e, event, "Image generation problem.");
        }
    }
}
