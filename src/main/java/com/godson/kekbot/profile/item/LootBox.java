package com.godson.kekbot.profile.item;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.commands.botowner.botadmin.Reboot;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.util.RandomCollection;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LootBox {
    public enum Rarity {
        COMMON(175), RARE(30), EPIC(10), LEGENDARY(3), GRAND(0.3);

        private double chance;

        Rarity(double chance) {
            this.chance = chance;
        }

        public double getChance() {
            return chance;
        }

        public boolean isGreaterThan(Rarity other) {
            return chance < other.chance;
        }
    }

    private Rarity rarity;


    private static RandomCollection<Rarity> rarityRNG = new RandomCollection<Rarity>()
            .add(Rarity.COMMON.chance, Rarity.COMMON)
            .add(Rarity.RARE.chance, Rarity.RARE)
            .add(Rarity.EPIC.chance, Rarity.EPIC)
            .add(Rarity.LEGENDARY.chance, Rarity.LEGENDARY)
            .add(Rarity.GRAND.chance, Rarity.GRAND);

    public LootBox() {
        rarity = rarityRNG.next();
    }

    public LootBox(Rarity rarity) {
        this.rarity = rarity;
    }

    public Rarity getRarity() {
        return rarity;
    }

    //Rewards
    public static abstract class Reward {
        private Rarity rarity;

        public Reward(Rarity rarity) {
            this.rarity = rarity;
        }

        public abstract boolean getReward(Profile profile);

        public Rarity getRarity() {
            return rarity;
        }
    }

    public static class TopKekReward extends Reward {

        private int amount;

        public TopKekReward(Rarity rarity, int amount) {
            super(rarity);
            this.amount = amount;
        }

        @Override
        public boolean getReward(Profile profile) {
            profile.addTopKeks(amount);
            profile.save();
            return true;
        }
    }

    public static class BGReward extends Reward {

        private Background background;

        public BGReward(Rarity rarity, String BG) {
            super(rarity);
            background = KekBot.backgroundManager.get(BG);
        }

        @Override
        public boolean getReward(Profile profile) {
            if (!profile.hasBackground(background)) {
                profile.addBackground(background);
                profile.save();
                return true;
            } else return false;
        }
    }

    public static class KXPReward extends Reward {

        private int amount;

        public KXPReward(Rarity rarity, int amount) {
            super(rarity);
            this.amount = amount;
        }

        @Override
        public boolean getReward(Profile profile) {
            profile.addKXP(amount);
            profile.save();
            return true;
        }
    }

    public static class TokenReward extends Reward {

        private Token token;

        public TokenReward(Rarity rarity, Token token) {
            super(rarity);
            this.token = token;
        }

        @Override
        public boolean getReward(Profile profile) {
            if (!profile.hasToken(token)) {
                profile.addToken(token);
                profile.save();
                return true;
            } return false;
        }
    }
}
