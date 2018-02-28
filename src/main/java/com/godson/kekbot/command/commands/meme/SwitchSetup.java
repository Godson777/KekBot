package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.profile.ProfileUtils;

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

public class SwitchSetup extends Command {

    public SwitchSetup() {
        name = "switch";
        description = "Shows how easy it is to setup a switch, with a twist.";
        usage.add("switch <image URL>");
        usage.add("switch <attachment>");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getMessage().getAttachments().size() > 0) {
            if (event.getMessage().getAttachments().get(0).isImage()) {
                try {
                    event.getChannel().sendTyping().queue();
                    event.getChannel().sendFile(generate(ImageIO.read(event.getMessage().getAttachments().get(0).getInputStream())), "swatch.png", null).queue();
                } catch (IOException e) {
                    throwException(e, event, "Image Generation Problem");
                }
            } else event.getChannel().sendMessage("That's not a valid image.").queue();
        } else {
            if (event.getArgs().length > 0) {
                event.getChannel().sendTyping().queue();
                try {
                    URL image = new URL(event.getArgs()[0]);
                    URLConnection connection = image.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    connection.connect();
                    BufferedImage check = ImageIO.read(connection.getInputStream());
                    if (check == null) {
                        event.getChannel().sendMessage("No image found.").queue();
                        return;
                    }

                    event.getChannel().sendFile(generate(check), "swatch.png", null).queue();
                } catch (MalformedURLException | UnknownHostException | IllegalArgumentException | FileNotFoundException e) {
                    event.getChannel().sendMessage("`" + event.getArgs()[0] + "`" + " is not a valid URL.").queue();
                } catch (SSLHandshakeException | SocketException e) {
                    event.getChannel().sendMessage("Unable to connect to URL.").queue();
                } catch (IOException e) {
                    throwException(e, event, "Image Generation Problem");
                }
            } else event.getChannel().sendMessage("No image provided.").queue();
        }
    }

    private byte[] generate(BufferedImage image) throws IOException {
        BufferedImage base = ImageIO.read(new File("resources//memegen/switch_setup.png"));
        BufferedImage blank = new BufferedImage(base.getWidth(), base.getHeight(), base.getType());
        Graphics2D graphics = blank.createGraphics();

        double widthRatio = 174d / image.getWidth();
        double heightRatio = 157d / image.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);

        Dimension dimension = new Dimension((int) (image.getWidth() * ratio), (int) (image.getHeight() * ratio));

        Rectangle2D r2D = new Rectangle(dimension);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int rX = (int) Math.round(r2D.getX());
        int rY = (int) Math.round(r2D.getY());
        int a = (174 / 2) - (rWidth / 2) - rX;
        int b = (157 / 2) - (rHeight / 2) - rY;

        graphics.drawImage(image, 366 + a, 214 + b, dimension.width, dimension.height, null);
        graphics.drawImage(base, 0, 0, null);
        graphics.dispose();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.flush();
        ImageIO.setUseCache(false);
        ImageIO.write(blank, "png", stream);
        byte[] finished = stream.toByteArray();
        stream.close();

        return finished;
    }
}
