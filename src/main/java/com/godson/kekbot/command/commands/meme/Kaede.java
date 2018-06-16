package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.Utils;
import com.godson.kekbot.command.*;

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
import java.util.Arrays;


public class Kaede extends Command {

    public Kaede() {
        name = "kaede";
        description = "Kaede";
        usage.add("kaede <text>");
        usage.add("kaede <attachment>");
        usage.add("kaede <image URL>");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        String filename = "kaededab";
        boolean reboot = false;

        if (event.combineArgs().contains("--reboot")) reboot = true;

        String[] args = event.getArgs();

        args = Arrays.stream(args).filter(s -> !s.equalsIgnoreCase("--reboot")).toArray(String[]::new);


        if (event.getMessage().getAttachments().size() > 0) {
            if (event.getMessage().getAttachments().get(0).isImage()) {
                try {
                    event.getChannel().sendTyping().queue();
                    event.getChannel().sendFile(generate(ImageIO.read(event.getMessage().getAttachments().get(0).getInputStream()), reboot),  filename + ".png", null).queue();
                } catch (IOException e) {
                    throwException(e, event, "Image Generation Problem");
                }
            } else event.getChannel().sendMessage(event.getString("command.textimage.imagenotvalid")).queue();
        } else {
            if (args.length > 0) {
                event.getChannel().sendTyping().queue();
                try {
                    URL image = new URL(event.combineArgs());
                    URLConnection connection = image.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    connection.connect();
                    BufferedImage check = ImageIO.read(connection.getInputStream());
                    if (check == null) {
                        try {
                            event.getChannel().sendFile(generate(event.combineArgs().replace("--reboot", ""), reboot), filename + ".png", null).queue();
                        } catch (IllegalArgumentException e) {
                            event.getChannel().sendMessage(event.getString("command.textimage.texttoolong")).queue();
                        }
                        return;
                    }

                    event.getChannel().sendFile(generate(check, reboot), filename + ".png", null).queue();
                } catch (MalformedURLException | UnknownHostException | IllegalArgumentException | FileNotFoundException e) {
                    try {
                        event.getChannel().sendFile(generate(event.combineArgs().replace("--reboot", ""), reboot), filename + ".png", null).queue();
                    } catch (IllegalArgumentException e1) {
                        event.getChannel().sendMessage(event.getString("command.textimage.texttoolong")).queue();
                    }
                } catch (SSLHandshakeException | SocketException e) {
                    event.getChannel().sendMessage(event.getString("command.textimage.unabletoconnect")).queue();
                } catch (IOException e) {
                    try {
                        event.getChannel().sendFile(generate(event.combineArgs().replace("--reboot", ""), reboot), filename + ".png", null).queue();
                    } catch (IllegalArgumentException e1) {
                        event.getChannel().sendMessage(event.getString("command.textimage.texttoolong")).queue();
                    }
                }
            } else event.getChannel().sendMessage(event.getString("command.textimage.noargs")).queue();
        }
    }

    protected byte[] generate(BufferedImage image, boolean reboot) throws IOException {
        BufferedImage template = ImageIO.read(new File(reboot ? "resources/memegen/kaededab-reboot.jpg" : "resources/memegen/kaededab.jpg"));
        Graphics2D graphics = template.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.rotate(Math.toRadians(-6.92));

        //Some math stuff for maintaining aspect ratio.
        double widthRatio = 610d / image.getWidth();
        double heightRatio = 379d / image.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);

        Dimension dimension = new Dimension((int) (image.getWidth() * ratio), (int) (image.getHeight() * ratio));

        //Some math stuff for centering the image.
        Rectangle2D r2D = new Rectangle(dimension);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int rX = (int) Math.round(r2D.getX());
        int rY = (int) Math.round(r2D.getY());
        int a = (610 / 2) - (rWidth / 2) - rX;
        int b = (379 / 2) - (rHeight / 2) - rY;

        graphics.drawImage(image, 60 + a, 717 + b, dimension.width, dimension.height, null);

        graphics.dispose();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.flush();
        ImageIO.setUseCache(false);
        ImageIO.write(template, "png", stream);
        byte[] finished = stream.toByteArray();
        stream.close();

        return finished;
    }

    protected byte[] generate(String string, boolean reboot) throws IOException {
        BufferedImage template = ImageIO.read(new File(reboot ? "resources/memegen/kaededab-reboot.jpg" : "resources/memegen/kaededab.jpg"));
        Graphics2D graphics = template.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setFont(new Font("Calibri", Font.BOLD, 70));
        graphics.setColor(Color.black);

        graphics.rotate(Math.toRadians(-6.92));

        StringBuilder text = new StringBuilder();
        String[] words = Utils.removeWhitespaceEdges(string).split(" ");

        //This snippet of code separates the message, going over every space and separating any text into a new line if too long.
        //Because this only treats the separation with spaces, really long words like "Supercalifragilistic" go over the boarder.
        StringBuilder test = new StringBuilder();
        for (String word : words) {
            if (graphics.getFont().getStringBounds(word, graphics.getFontRenderContext()).getWidth() > 610) throw new IllegalArgumentException("String too long pls");
            if (graphics.getFont().getStringBounds(test + " " + word, graphics.getFontRenderContext()).getWidth() <= 610)
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
        for (String aSplit : split) {
            totalY += graphics.getFontMetrics().getHeight();
        }

        if (totalY > graphics.getFontMetrics().getHeight() * 3) throw new IllegalArgumentException("String too long pls");

        //Some math stuff for centering the image.
        Rectangle2D r2D = graphics.getFont().getStringBounds(split[0], graphics.getFontRenderContext());
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int rX = (int) Math.round(r2D.getX());
        int rY = (int) Math.round(r2D.getY());
        int a = (610 / 2) - (rWidth / 2) - rX;
        int b = (379 / 2) - (rHeight / 2) - rY;

        drawString(graphics, text.toString(), 60 + a, 717 + b - (totalY <= graphics.getFontMetrics().getHeight() * 2 ? totalY : graphics.getFontMetrics().getHeight() * 2));

        graphics.dispose();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.flush();
        ImageIO.setUseCache(false);
        ImageIO.write(template, "png", stream);
        byte[] finished = stream.toByteArray();
        stream.close();

        return finished;
    }

    private void drawString(Graphics g, String text, int x, int y) {
        for (String line : text.split("\n"))
            g.drawString(line, x, y += g.getFontMetrics().getHeight());
    }
}
