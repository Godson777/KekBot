package com.godson.kekbot;

import java.util.Random;

public class CustomEmote {
    private final static String[] THINKINGS =
            {"<:GRANDTHONK:283540692502970368>",
            "<:Thunk:280126422213853194>",
            "<:thinkingnohands:319056770943287296>",
            "<:OwOThink:319056770959933440>",
            "<:thonkang:319056771027304448>",
            "\uD83E\uDD14"};
    private final static String TOPKEK = "<:topkek:317825573441503243>";
    private final static String[] TROPHIES = {"<:GoldTrophy:363572128592822272>", "<:SilverTrophy:363572121928204299>", "<:BronzeTrophy:363572119101112320>"};
    private static Random random = new Random();

    public static String think() {
        return THINKINGS[random.nextInt(THINKINGS.length)];
    }

    public static String printPrice(double price) {
        if (price % 1 == 0) return (int) price + TOPKEK;
        else return price + TOPKEK;
    }

    public static String printTopKek() {
        return TOPKEK;
    }

    public static String getTrophy(int place) {
        if (place < 0 || place > TROPHIES.length) return TROPHIES[0];
        else return TROPHIES[place];
    }
}
