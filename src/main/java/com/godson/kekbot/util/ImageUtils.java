package com.godson.kekbot.util;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class ImageUtils {

    public static boolean drawCenteredString(Graphics2D graphics, String str, int posX, int posY, int maxWidth, int maxHeight) {
        StringBuilder text = new StringBuilder();
        String[] words = Utils.removeWhitespaceEdges(str).split(" ");

        //This snippet of code separates the message, going over every space and separating any text into a new line if too long.
        //Because this only treats the separation with spaces, really long words like "Supercalifragilistic" go over the boarder.
        StringBuilder test = new StringBuilder();
        for (String word : words) {
            if (graphics.getFont().getStringBounds(word, graphics.getFontRenderContext()).getWidth() > maxWidth) {
                return false;
            }
            if (graphics.getFont().getStringBounds(test + " " + word, graphics.getFontRenderContext()).getWidth() > maxWidth) {
                text.append(test).append("\n");
                test.delete(0, test.length());
            }
            test.append(word).append(" ");
        }
        text.append(test);

        //Total height of all the text.
        int totalHeight = 0;

        //The highest width gathered from all the lines of text.
        int highestWidth = 0;

        //Splits the text into an array with every new line it bumps into.
        String[] split = text.toString().split("\n");

        //For loop to determine total height of all text.
        for (String s : split) {
            totalHeight += graphics.getFontMetrics().getHeight();
            Rectangle2D temp = graphics.getFontMetrics().getStringBounds(s, graphics);
            if (temp.getWidth() > highestWidth) highestWidth = (int) Math.round(temp.getWidth());
        }

        if (totalHeight > maxHeight) {
            return false;
        }

        //Some math stuff for centering the image.
        int a = (maxWidth / 2) - (highestWidth / 2);
        int b = (maxHeight / 2) - (totalHeight / 2);
        drawString(graphics, text.toString(), posX + a, posY + b); //(totalY / 2));
        return true;
    }

    private static void drawString(Graphics g, String text, int x, int y) {
        for (String line : text.split("\n"))
            g.drawString(line, x, y += g.getFontMetrics().getHeight());
    }
}
