package com.godson.kekbot;

import java.util.Random;

public class CustomEmote {
    private static String[] THINKINGS =
            {"<:GRANDTHONK:283540692502970368>",
            "<:Thunk:280126422213853194>",
            "<:thinkingnohands:319056770943287296>",
            "<:OwOThink:319056770959933440>",
            "<:thonkang:319056771027304448>",
            "\uD83E\uDD14"};
    public static String TOPKEK = "<:topkek:317825573441503243>";
    private static Random random = new Random();

    public static String think() {
        return THINKINGS[random.nextInt(THINKINGS.length)];
    }
}
