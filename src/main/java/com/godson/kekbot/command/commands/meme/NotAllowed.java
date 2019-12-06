package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.command.ImageCommand;
import com.godson.kekbot.util.Utils;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class NotAllowed extends Command {

    public NotAllowed() {
        name = "notallowed";
        description = "Huh. I wonder who that's for.";
        usage.add("notallowed <@user>");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) {
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

        try {
            BufferedImage base = ImageIO.read(new File("resources/memegen/notallowed.png"));
            BufferedImage blank = new BufferedImage(base.getWidth(), base.getHeight(), base.getType());
            Graphics2D graphics = blank.createGraphics();

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.fillRect(485, 83, 340, 340);
            graphics.drawImage(target, 485, 83, 340, 340, null);
            graphics.drawImage(base, 0, 0, null);
            graphics.fillRect(46, 63, 390, 390);
            graphics.drawImage(target, 46, 63, 390, 390, null);
            graphics.dispose();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.flush();
            ImageIO.setUseCache(false);
            ImageIO.write(blank, "png", stream);
            event.getChannel().sendFile(stream.toByteArray(), "huh.png").queue();
            stream.close();
        } catch (IOException e) {
            throwException(e, event, "Image generation problem.");
        }
    }
}
