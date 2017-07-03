package com.godson.kekbot.Profile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public enum Badge {

    PATREON1("PATREON_BRONZE", "PATREON1.png"),
    PATREON2("PATREON_SILVER", "PATREON2.png"),
    PATREON3("PATREON_GOLD", "PATREON3.png"),
    CHAMBERSTAFF("CHAMBER_STAFF", "CHAMBERSTAFF.png");

    private String name;
    private String file;

    Badge(String name, String file) {
        this.name = name;
        this.file = file;
    }

    public BufferedImage drawBadge() throws IOException {
        return ImageIO.read(new File("resources/profile/badge/" + file));
    }

    public String getName() {
        return name;
    }
}
