package com.godson.kekbot.shop;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.profile.Background;
import com.godson.kekbot.profile.Profile;
import net.dv8tion.jda.core.entities.User;

import java.util.Comparator;

public class BackgroundShop extends Shop<Background> {

    public BackgroundShop() {
        super();
        for (Background background : KekBot.backgroundManager.getBackgrounds()) {
            if (background.hasPrice()) addToInventory(background);
        }
        getInventory().sort(Comparator.comparing(Background::getRequiredLevel).thenComparing(Background::getPrice));
    }

    public String buy(Background background, User user) {
        Profile profile = Profile.getProfile(user);
        if (background.getRequiredLevel() <= profile.getLevel()) {
            if (!profile.hasBackground(background)) {
                if (profile.canSpend(background.getPrice())) {
                    profile.spendTopKeks(background.getPrice());
                    profile.addBackground(background);
                    profile.save();
                    return "Purchase complete. \uD83D\uDCB0";
                } else
                    return "You can't afford this background! This costs **" + CustomEmote.printPrice(background.getPrice()) + "**, you have **" + CustomEmote.printPrice(profile.getTopkeks()) + "**.";
            } else return "You already have this background!";
        } else return "This background requires you to be at least Level " + background.getRequiredLevel() + ". You are Level " + profile.getLevel() + ".";
    }
}
