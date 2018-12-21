package com.godson.kekbot.profile;

import com.godson.kekbot.util.Utils;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;

public class ProfileUtils {
    public static Font topBarTitle = new Font("Calibri", Font.BOLD, 34);
    public static Font topBarSubtitle = new Font("Calibri", Font.BOLD, 25);
    public static Font topBarBio = new Font("Calibri", Font.BOLD, 22);
    public static Font sideBar = new Font("Calibri", Font.BOLD, 28);
    public static FontRenderContext frc = new FontRenderContext(null, true, true);

    public static boolean testBio(String bio) {
        BufferedImage image = new BufferedImage(736, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D test = image.createGraphics();
        test.setFont(ProfileUtils.topBarBio);
        String tempBio = bio;
        try {
            while (test.getFont().getStringBounds(tempBio, test.getFontRenderContext()).getWidth() > 736) {
                tempBio = tempBio.substring(0, tempBio.lastIndexOf(" ", tempBio.length()));
            }
        } catch (StringIndexOutOfBoundsException e) {
            return false;
        }
        return tempBio.length() == bio.length() || test.getFont().getStringBounds(Utils.removeWhitespaceEdges(bio.substring(tempBio.length(), bio.length())), test.getFontRenderContext()).getWidth() < 736;
    }
}
