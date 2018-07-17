package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.util.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;

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

public class Gru extends Command {

    public Gru() {
        name = "gru";
        description = "Gru demonstrates your master plan...?";
        usage.add("gru <attachment>");
        usage.add("gru <image URL>");
        usage.add("gru <text>");
        usage.add("gru <text> | <text> | <text>");
        category = CommandCategories.meme;
        exDescPos = ExtendedPosition.AFTER;
        extendedDescription = "I wonder what would happen if you typed --hyper...";
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        String filename = "masterplan";
        boolean hyper = false;
        boolean egg = false;

        if (event.combineArgs().contains("--hyper")) hyper = true;
        if (event.combineArgs().contains("--egg")) egg = true;

        String[] args = event.getArgs();

        args = Arrays.stream(args).filter(s -> !s.equalsIgnoreCase("--hyper") && !s.equalsIgnoreCase("--egg")).toArray(String[]::new);

        if (event.getMessage().getAttachments().size() > 0) {
            if (event.getMessage().getAttachments().get(0).isImage()) {
                try {
                    event.getChannel().sendTyping().queue();
                    event.getChannel().sendFile(generate(ImageIO.read(event.getMessage().getAttachments().get(0).getInputStream()), hyper, egg),  filename + ".png", null).queue();
                } catch (IOException e) {
                    throwException(e, event, "Image Generation Problem");
                }
            } else event.getChannel().sendMessage("That's not a valid image.").queue();
        } else {
            if (args.length > 0) {
                event.getChannel().sendTyping().queue();
                try {
                    URL image = new URL(event.combineArgs().replaceAll("--hyper", ""));
                    URLConnection connection = image.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    connection.connect();
                    BufferedImage check = ImageIO.read(connection.getInputStream());
                    if (check == null) {
                        try {
                            event.getChannel().sendFile(generate(event.combineArgs().replaceAll("--hyper", "").replace("--egg", ""), hyper, egg), filename + ".png", null).queue();
                        } catch (IllegalArgumentException e1) {
                            event.getChannel().sendMessage("The text you have provided is too long for one of the panels. Please try something else.").queue();
                        }
                        return;
                    }

                    event.getChannel().sendFile(generate(check, hyper, egg), filename + ".png", null).queue();
                } catch (MalformedURLException | UnknownHostException | IllegalArgumentException | FileNotFoundException e) {
                    try {
                        event.getChannel().sendFile(generate(event.combineArgs().replaceAll("--hyper", "").replace("--egg", ""), hyper, egg), filename + ".png", null).queue();
                    } catch (IllegalArgumentException e1) {
                        event.getChannel().sendMessage("The text you have provided is too long for one of the panels. Please try something else.").queue();
                    }
                } catch (SSLHandshakeException | SocketException e) {
                    event.getChannel().sendMessage("Unable to connect to URL.").queue();
                } catch (IOException e) {
                    try {
                        event.getChannel().sendFile(generate(event.combineArgs().replaceAll("--hyper", "").replace("--egg", ""), hyper, egg), filename + ".png", null).queue();
                    } catch (IllegalArgumentException e1) {
                        event.getChannel().sendMessage("The text you have provided is too long for one or more of the panels. Please try something else.").queue();
                    }
                }
            } else event.getChannel().sendMessage("No image or text provided.").queue();
        }
    }

    private byte[] generate(BufferedImage image, boolean hyper, boolean egg) throws IOException {
        BufferedImage base = ImageIO.read(new File("resources/memegen/grusmasterplan" + (egg ? "egg" : "") + (hyper ? "hyper" : "") + ".png"));
        Graphics2D graphics = base.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double widthRatio = 270d / image.getWidth();
        double heightRatio = 360d / image.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);

        Dimension dimension = new Dimension((int) (image.getWidth() * ratio), (int) (image.getHeight() * ratio));

        Rectangle2D r2D = new Rectangle(dimension);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int rX = (int) Math.round(r2D.getX());
        int rY = (int) Math.round(r2D.getY());
        int a = (270 / 2) - (rWidth / 2) - rX;
        int b = (360 / 2) - (rHeight / 2) - rY;

        graphics.drawImage(image, (egg ? 493 : 436) + a, 114 + b, dimension.width, dimension.height, null);
        graphics.drawImage(image, (egg ? 1343 : 1191) + a, 114 + b, dimension.width, dimension.height, null);
        graphics.drawImage(image, (egg ? 491 : 442) + a, 595 + b, dimension.width, dimension.height, null);
        graphics.drawImage(image, (egg ? 1347 : 1190) + a, 595 + b, dimension.width, dimension.height, null);
        if (hyper) graphics.drawImage(image, (egg ? 918 : 786) + a, 1073 + b, dimension.width, dimension.height, null);
        graphics.dispose();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.flush();
        ImageIO.setUseCache(false);
        ImageIO.write(base, "png", stream);
        byte[] finished = stream.toByteArray();
        stream.close();

        return finished;
    }

    private byte[] generate(String string, boolean hyper, boolean egg) throws IOException {
        BufferedImage base = ImageIO.read(new File("resources/memegen/grusmasterplan" + (egg ? "egg" : "") + (hyper ? "hyper" : "") + ".png"));
        Graphics2D graphics = base.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Splits the whole string into arguments because heck.
        String[] args = string.split("\\|", 3);

        if (args.length < 3) args = Utils.increaseArraySize(args, 3);

        if (args[1] == null) args[1] = args[0];
        if (args[2] == null) args[2] = args[1];

        //Sets the font, and the text color.
        graphics.setFont(new Font("Calibri", Font.BOLD, 40));
        graphics.setColor(Color.black);

        //For every argument given...
        for (int i = 0; i < args.length; i++) {
            String str = args[i];
            try {
                URL image = new URL(Utils.removeWhitespaceEdges(str));
                URLConnection connection = image.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.connect();
                BufferedImage check = ImageIO.read(connection.getInputStream());

                //If this isn't a valid image
                if (check == null) {
                    //Do literally nothing.
                    continue;
                }

                //Resizing images relative to the pre-determined size needed to fit the paper.
                double widthRatio = 270d / check.getWidth();
                double heightRatio = 360d / check.getHeight();
                double ratio = Math.min(widthRatio, heightRatio);

                Dimension dimension = new Dimension((int) (check.getWidth() * ratio), (int) (check.getHeight() * ratio));

                //Centers the image.
                Rectangle2D r2D = new Rectangle(dimension);
                int rWidth = (int) Math.round(r2D.getWidth());
                int rHeight = (int) Math.round(r2D.getHeight());
                int rX = (int) Math.round(r2D.getX());
                int rY = (int) Math.round(r2D.getY());
                int a = (270 / 2) - (rWidth / 2) - rX;
                int b = (360 / 2) - (rHeight / 2) - rY;

                //Draws the image on the panels, depending on what i is, and using the a and b values to determine the "centered" position relative to the x and y values given.
                if (i == 0) graphics.drawImage(check, (egg ? 493 : 436) + a, 114  + b, dimension.width, dimension.height, null);
                if (i == 1) graphics.drawImage(check, (egg ? 1343 : 1191) + a, 114 + b, dimension.width, dimension.height, null);
                if (i == 2) {
                    graphics.drawImage(check, (egg ? 491 : 442) + a, 595 + b, dimension.width, dimension.height, null);
                    graphics.drawImage(check, (egg ? 1347 : 1190) + a, 595 + b, dimension.width, dimension.height, null);
                    if (hyper) graphics.drawImage(check, (egg ? 918 : 786) + a, 1073 + b, dimension.width, dimension.height, null);
                }

            } catch (MalformedURLException | UnknownHostException | IllegalArgumentException | FileNotFoundException e) {

                //This snippet of code separates the message, going over every space and separating any text into a new line if too long.
                //Because this only treats the separation with spaces, really long words like "Supercalifragilistic" go over the boarder.
                StringBuilder text = new StringBuilder();
                String[] words = Utils.removeWhitespaceEdges(str).split(" ");

                StringBuilder test = new StringBuilder();
                for (String word : words) {
                    if (graphics.getFont().getStringBounds(word, graphics.getFontRenderContext()).getWidth() > 250) throw new IllegalArgumentException("String too long pls");
                    if (graphics.getFont().getStringBounds(test + " " + word, graphics.getFontRenderContext()).getWidth() <= 250)
                        test.append(word).append(" ");
                    else {
                        text.append(test).append("\n");
                        test.delete(0, test.length());
                        test.append(word).append(" ");
                    }
                }
                text.append(test);
                //End of snippet.

                //Total height of all the text.
                int totalY = 0;

                //Splits the text into an array with every new line it bumps into.
                String[] split = text.toString().split("\n");

                //For loop to determine total height of all text.
                for (int i1 = 0; i1 < split.length; i1++) {
                    totalY += graphics.getFontMetrics().getHeight();
                }

                if (totalY > 50 * 7) throw new IllegalArgumentException("String too long pls");

                //WOO CENTERING THE TEXT VERTICALLY LIKE A BITCH!
                Rectangle2D r2D = graphics.getFont().getStringBounds(split[0], graphics.getFontRenderContext());
                int rWidth = (int) Math.round(270);
                int rHeight = (int) Math.round(totalY);
                int rX = (int) Math.round(r2D.getX());
                int rY = (int) Math.round(r2D.getY());
                int a = (270 / 2) - (rWidth / 2) - rX;
                int b = (360 / 2) - (rHeight / 2) - rY;

                //Draws the text in their proper place, using the a and b values to apply the centering, and subtracting 50 to adjust with the font's size and "totalY"s value.
                if (i == 0) {
                    drawString(graphics, text.toString(), (egg ? 493 : 436) + a, 114 + b - 50);
                }
                if (i == 1) {
                    drawString(graphics, text.toString(), (egg ? 1343 : 1191) + a, 114 + b - 50);
                }
                if (i == 2) {
                    drawString(graphics, text.toString(), (egg ? 491 : 442) + a, 595 + b - 50);
                    drawString(graphics, text.toString(), (egg ? 1347 : 1190) + a, 595 + b - 50);
                    if (hyper) drawString(graphics, text.toString(), (egg ? 918 : 786) + a, 1073 + b - 50);
                }

            } catch (SSLHandshakeException | SocketException e) {
                //event.getChannel().sendMessage("Unable to connect to URL.").queue();
            }
        }

        //The rest of simply turns the image into a byte array, where it is sent to JDA to upload to Discord.
        graphics.dispose();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.flush();
        ImageIO.setUseCache(false);
        ImageIO.write(base, "png", stream);
        byte[] finished = stream.toByteArray();
        stream.close();

        return finished;
    }

    private void drawString(Graphics g, String text, int x, int y) {
        for (String line : text.split("\n"))
            g.drawString(line, x, y += g.getFontMetrics().getHeight());
    }
}
