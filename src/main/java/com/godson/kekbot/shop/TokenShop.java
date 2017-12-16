package com.godson.kekbot.shop;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.profile.Token;
import net.dv8tion.jda.core.entities.User;

import java.util.Comparator;

public class TokenShop extends Shop<Token> {

    public TokenShop() {
        super();
        for (Token token : Token.values()) {
            if (token.hasPrice()) addToInventory(token);
        }
        getInventory().sort(Comparator.comparing(Token::getRequiredLevel).thenComparing(Token::getPrice));
    }

    public String buy(Token token, User user) {
        Profile profile = Profile.getProfile(user);
        if (token.getRequiredLevel() <= profile.getLevel()) {
            if (!profile.hasToken(token)) {
                if (profile.canSpend(token.getPrice())) {
                    profile.spendTopKeks(token.getPrice());
                    profile.addToken(token);
                    profile.save();
                    return "Purchase complete. \uD83D\uDCB0";
                } else return "You can't afford this token! This costs **" + CustomEmote.printPrice(token.getPrice()) + "**, you have **" + CustomEmote.printPrice(profile.getTopkeks()) + "**.";
            } else return "You already have this token!";
        } else return "This token requires you to be at least Level " + token.getRequiredLevel() + ". You are Level " + profile.getLevel() + ".";
    }
}
