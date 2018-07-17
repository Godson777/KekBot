package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.command.TextImageCommand;
import com.godson.kekbot.util.Utils;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;

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

                StringBuilder text = new StringBuilder();
                String[] words = Utils.removeWhitespaceEdges(message).split(" ");

                //Max width for text we can go:
                int maxTextWidth = 1226;

                //This snippet of code separates the message, going over every space and separating any text into a new line if too long.
                //Because this only treats the separation with spaces, really long words like "Supercalifragilistic" go over the boarder.
                StringBuilder test = new StringBuilder();
                for (String word : words) {
                    if (graphics.getFont().getStringBounds(word, graphics.getFontRenderContext()).getWidth() > maxTextWidth) {
                        event.getChannel().sendMessage(event.getString("command.textimage.texttoolong")).queue();
                        return;
                    }
                    if (graphics.getFont().getStringBounds(test + " " + word, graphics.getFontRenderContext()).getWidth() <= maxTextWidth)
                        test.append(word).append(" ");
                    else {
                        text.append(test).append("\n");
                        test.delete(0, test.length());
                        test.append(word).append(" ");
                    }
                }
                text.append(test);

                //Total height of all the text.
                int totalY = 0;

                //Splits the text into an array with every new line it bumps into.
                String[] split = text.toString().split("\n");

                //For loop to determine total height of all text.
                for (int i = 0; i < split.length; i++) {
                    totalY += graphics.getFontMetrics().getHeight();
                }

                if (totalY > graphics.getFontMetrics().getHeight() * 3) {
                    event.getChannel().sendMessage(event.getString("command.textimage.texttoolong")).queue();
                    return;
                }

                //Some math stuff for centering the image.
                Rectangle2D r2D = graphics.getFont().getStringBounds(split[0], graphics.getFontRenderContext());
                int rWidth = (int) Math.round(r2D.getWidth());
                int rHeight = (int) Math.round(r2D.getHeight());
                int rX = (int) Math.round(r2D.getX());
                int rY = (int) Math.round(r2D.getY());
                int a = (1226 / 2) - (rWidth / 2) - rX;
                int b = (750 / 2) - (rHeight / 2) - rY;

                drawString(graphics, text.toString(), 88 + a, 473 + b - (totalY <= graphics.getFontMetrics().getHeight() * 2 ? totalY : graphics.getFontMetrics().getHeight() * 2));

                graphics.dispose();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                stream.flush();
                ImageIO.setUseCache(false);
                ImageIO.write(image, "png", stream);
                byte[] finished = stream.toByteArray();
                stream.close();

                event.getChannel().sendFile(finished, "dsxsays.png", null).queue();
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
