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
    private final static String[] DANCES = {"<a:duane:403114999578361856>", "<a:pbjtime:404578308009885702>", "<a:BowserDance:404580609113980929>"};
    private final static String TOPKEK = "<:topkek:680961149826629632>";
    private final static String[] TROPHIES = {"<:GoldTrophy:363572128592822272>", "<:SilverTrophy:363572121928204299>", "<:BronzeTrophy:363572119101112320>"};
    private final static String[] LOADING =
            {"<a:loading:438621485985169408>",
                    "<a:loading2:438621499704737802>",
                    "<a:loading3:438621490053775370>",
                    "<a:loading4:438621490162958337>",
                    "<a:loading5:438621512807874560>"};
    private static Random random = new Random();

    public static String think() {
        return THINKINGS[random.nextInt(THINKINGS.length)];
    }

    public static String dance() {
        return DANCES[random.nextInt(DANCES.length)];
    }

    public static String printPrice(double price) {
        if (price % 1 == 0) return TOPKEK + (int) price;
        else return TOPKEK + price;
    }

    public static String printTopKek() {
        return TOPKEK;
    }

    public static String getTrophy(int place) {
        if (place < 0 || place > TROPHIES.length) return TROPHIES[0];
        else return TROPHIES[place];
    }

    public static String load() {
        return LOADING[random.nextInt(LOADING.length)];
    }
}
