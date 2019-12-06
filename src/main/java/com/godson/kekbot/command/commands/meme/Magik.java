package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.concurrent.ExecutionException;

public class Magik extends Command {

    public Magik() {
        name = "magik";
        description = "Also known as the Content Aware Scale";
        exDescPos = ExtendedPosition.AFTER;
        extendedDescription = "Huh? What do you mean NotSoBot is back? So this tribute was for nothing? wow ok.";
        usage.add("magick <image URL>");
        usage.add("magick <attachment>");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getMessage().getAttachments().size() > 0) {
            if (event.getMessage().getAttachments().get(0).isImage()) {
                event.getChannel().sendMessage(event.getString("command.meme.magik.generating") + CustomEmote.load()).queue(m -> {
                    try {
                        event.getChannel().sendFile(generate(event.getMessage().getAttachments().get(0).retrieveInputStream().get()), "magik.png").queue(h -> m.delete().queue());
                    } catch (IOException | InterruptedException | IM4JavaException | ExecutionException e) {
                        throwException(e, event, "Image Generation Problem");
                    }
                });
            } else event.getChannel().sendMessage(event.getString("command.textimage.imagenotvalid")).queue();
        } else {
            if (event.getArgs().length > 0) {
                try {
                    URL image = new URL(event.getArgs()[0]);
                    URLConnection connection = image.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    connection.connect();
                    InputStream stream = connection.getInputStream();
                    BufferedImage check = ImageIO.read(stream);
                    if (check == null) {
                        event.getChannel().sendMessage(event.getString("command.textimage.noimage")).queue();
                        return;
                    }

                    event.getChannel().sendMessage(event.getString("command.meme.magik.generating") + CustomEmote.load()).queue(m -> {
                        try {
                            event.getChannel().sendFile(generate(check), "magik.png").queue(h -> m.delete().queue());
                        } catch (IOException | InterruptedException | IM4JavaException e) {
                            throwException(e, event, "Image Generation Problem");
                        }
                    });
                } catch (MalformedURLException | UnknownHostException | IllegalArgumentException | FileNotFoundException e) {
                    event.getChannel().sendMessage(event.getString("command.textimage.invalidurl", "`" + event.getArgs()[0] + "`")).queue();
                } catch (SSLHandshakeException | SocketException e) {
                    event.getChannel().sendMessage(event.getString("command.textimage.unabletoconnect")).queue();
                } catch (IOException e) {
                    throwException(e, event, "Image Generation Problem");
                }
            } else {
                event.getChannel().getHistory().retrievePast(50).queue(messages -> {
                    for (Message message : messages) {
                        if (message.getAttachments().size() < 1) {
                            continue;
                        }

                        if (message.getAttachments().get(0).isImage()) {

                                event.getChannel().sendMessage(event.getString("command.meme.magik.generating") + CustomEmote.load()).queue(m -> {
                                    try {
                                    event.getChannel().sendFile(generate(message.getAttachments().get(0).retrieveInputStream().get()), "magik.png").queue(h -> m.delete().queue());
                                    } catch (IOException | InterruptedException | IM4JavaException | ExecutionException e) {
                                        throwException(e, event, "Image Generation Problem");
                                    }
                                });
                                return;
                        }
                    }
                    event.getChannel().sendMessage(event.getString("command.textimage.noimage")).queue();
                });
            }
        }
    }

    protected byte[] generate(InputStream inputStream) throws IOException, IM4JavaException, InterruptedException {
        ConvertCmd cmd = new ConvertCmd();
        IMOperation op = new IMOperation();

        op.addImage("-");
        op.resize(800, 800);
        op.liquidRescale(400, 400);
        op.liquidRescale(1200, 1200);
        op.addImage("png:-");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Pipe pipeIn = new Pipe(inputStream, null);
        Pipe pipeOut = new Pipe(null, stream);
        cmd.setInputProvider(pipeIn);
        cmd.setOutputConsumer(pipeOut);
        if (KekBot.dev) cmd.setSearchPath("D:\\Program Files\\ImageMagick-6.9.9-Q16");

        cmd.run(op);

        stream.flush();
        byte[] finished = stream.toByteArray();
        stream.close();

        return finished;
    }

    protected byte[] generate(BufferedImage image) throws IOException, IM4JavaException, InterruptedException {
        ConvertCmd cmd = new ConvertCmd();
        IMOperation op = new IMOperation();

        op.addImage();
        op.resize(800, 800);
        op.liquidRescale(400, 400);
        op.liquidRescale(1200, 1200);
        op.addImage("png:-");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Pipe pipeOut = new Pipe(null, stream);
        cmd.setOutputConsumer(pipeOut);
        if (KekBot.dev) cmd.setSearchPath("D:\\Program Files\\ImageMagick-6.9.9-Q16");

        cmd.run(op, image);

        stream.flush();
        byte[] finished = stream.toByteArray();
        stream.close();

        return finished;
    }

}
