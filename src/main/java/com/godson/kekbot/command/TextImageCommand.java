package com.godson.kekbot.command;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;

public abstract class TextImageCommand extends Command {

    protected String filename;

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        if (event.getMessage().getAttachments().size() > 0) {
            if (event.getMessage().getAttachments().get(0).isImage()) {
                try {
                    event.getChannel().sendTyping().queue();
                    event.getChannel().sendFile(generate(ImageIO.read(event.getMessage().getAttachments().get(0).getInputStream())),  filename + ".png", null).queue();
                } catch (IOException e) {
                    throwException(e, event, "Image Generation Problem");
                }
            } else event.getChannel().sendMessage(event.getString("command.textimage.imagenotvalid")).queue();
        } else {
            if (event.getArgs().length > 0) {
                event.getChannel().sendTyping().queue();
                try {
                    URL image = new URL(event.combineArgs());
                    URLConnection connection = image.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    connection.connect();
                    BufferedImage check = ImageIO.read(connection.getInputStream());
                    if (check == null) {
                        try {
                            event.getChannel().sendFile(generate(event.combineArgs()), filename + ".png", null).queue();
                        } catch (IllegalArgumentException e) {
                            event.getChannel().sendMessage(event.getString("command.textimage.texttoolong")).queue();
                        }
                        return;
                    }

                    event.getChannel().sendFile(generate(check), filename + ".png", null).queue();
                } catch (MalformedURLException | UnknownHostException | IllegalArgumentException | FileNotFoundException e) {
                    try {
                        event.getChannel().sendFile(generate(event.combineArgs()), filename + ".png", null).queue();
                    } catch (IllegalArgumentException e1) {
                        event.getChannel().sendMessage(event.getString("command.textimage.texttoolong")).queue();
                    }
                } catch (SSLHandshakeException | SocketException e) {
                    event.getChannel().sendMessage(event.getString("command.textimage.unabletoconnect")).queue();
                } catch (IOException e) {
                    try {
                        event.getChannel().sendFile(generate(event.combineArgs()), filename + ".png", null).queue();
                    } catch (IllegalArgumentException e1) {
                        event.getChannel().sendMessage(event.getString("command.textimage.texttoolong")).queue();
                    }
                }
            } else event.getChannel().sendMessage(event.getString("command.textimage.noargs")).queue();
        }
    }

    protected abstract byte[] generate(BufferedImage image) throws IOException;

    protected abstract byte[] generate(String string) throws IOException;
}
