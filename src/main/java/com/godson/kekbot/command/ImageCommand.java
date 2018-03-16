package com.godson.kekbot.command;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;

public abstract class ImageCommand extends Command {

    protected String filename;

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getMessage().getAttachments().size() > 0) {
            if (event.getMessage().getAttachments().get(0).isImage()) {
                try {
                    event.getChannel().sendTyping().queue();
                    event.getChannel().sendFile(generate(ImageIO.read(event.getMessage().getAttachments().get(0).getInputStream())),  filename + ".png", null).queue();
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

                    event.getChannel().sendFile(generate(check), filename + ".png", null).queue();
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

    protected abstract byte[] generate(BufferedImage base) throws IOException;
}
