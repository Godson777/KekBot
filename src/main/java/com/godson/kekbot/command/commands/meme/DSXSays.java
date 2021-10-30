package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.util.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

//Requested by a few friends I met in the sm4sh modding scene
public class DSXSays extends Command {

    public DSXSays() {
        name = "dsxsays";
        description = "Makes DSX say a thing. Who's DSX? Who knows?";
        usage.add("dsxsays <text>");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length > 0) {
            event.getChannel().sendTyping().queue();

            //Start generating DSX Says image:
            try {
                BufferedImage image = ImageIO.read(new File("resources/memegen/dsxsays.png"));
                String message = event.combineArgs();
                Graphics2D graphics = image.createGraphics();
                graphics.setFont(new Font("Calibri", Font.BOLD, 144));
                graphics.setColor(Color.BLACK);

                if (!ImageUtils.drawCenteredString(graphics, message, 54, 397, 1237, 3025)) {
                    event.getChannel().sendMessage(event.getString("command.textimage.texttoolong")).queue();
                    return;
                }

                graphics.dispose();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                stream.flush();
                ImageIO.setUseCache(false);
                ImageIO.write(image, "png", stream);
                byte[] finished = stream.toByteArray();
                stream.close();

                event.getChannel().sendFile(finished, "dsxsays.png").queue();
            } catch (IOException e) {
                throwException(e, event, "Image Generation Problem");
            }



        } else event.getChannel().sendMessage(event.getString("command.noargs", event.getPrefix() + "help")).queue();
    }

    private void drawString(Graphics g, String text, int x, int y) {
        for (String line : text.split("\n"))
            g.drawString(line, x, y += g.getFontMetrics().getHeight());
    }
}
