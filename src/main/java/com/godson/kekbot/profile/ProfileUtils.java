package com.godson.kekbot.profile;

import com.godson.kekbot.util.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    public static List<Profile> getLocalLeaderboard(Guild guild) {
        List<Profile> profiles = getAllProfiles(guild);
        profiles.sort(Comparator.comparingInt(Profile::getLevel).thenComparingInt(Profile::getKXP).thenComparingDouble(Profile::getTopkeks).reversed());
        return profiles;
    }

    public static List<Profile> getAllProfiles(Guild guild) {
        List<Profile> profiles = new ArrayList<>();
        for (Member member : guild.getMembers()) {
            if (member.getUser().isBot()) continue;
            profiles.add(Profile.getProfile(member.getUser()));
        }
        return profiles;
    }
}
