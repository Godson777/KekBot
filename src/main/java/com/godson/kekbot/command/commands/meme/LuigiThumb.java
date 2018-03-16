package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

public class LuigiThumb extends Command {

    public LuigiThumb() {
        name = "luigithumb";
        description = "Gives an image Luigi's approval.";
        usage.add("luigithumb <image URL>");
        usage.add("luigithumb <attachment>");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getMessage().getAttachments().size() > 0) {
            if (event.getMessage().getAttachments().get(0).isImage()) {
                try {
                    event.getChannel().sendTyping().queue();
                    event.getChannel().sendFile(generate(ImageIO.read(event.getMessage().getAttachments().get(0).getInputStream())), "loogi.png", null).queue();
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

                    event.getChannel().sendFile(generate(check), "loogi.png", null).queue();
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

    private byte[] generate(BufferedImage base) throws IOException {
        Graphics2D graphics = base.createGraphics();
        BufferedImage loogy = ImageIO.read(new File("resources/memegen/luigi_thumb.png"));
        graphics.drawImage(loogy, base.getWidth() - loogy.getWidth(), base.getHeight() - loogy.getHeight(), null);
        graphics.dispose();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.flush();
        ImageIO.setUseCache(false);
        ImageIO.write(base, "png", stream);
        byte[] image = stream.toByteArray();
        stream.close();

        return image;
    }
}
