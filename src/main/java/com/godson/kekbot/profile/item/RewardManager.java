package com.godson.kekbot.profile.item;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RewardManager {
    private static List<LootBox.Reward> rewards = new ArrayList<>();

    static {
        try {
            //Get list of rewards from txt file.
            List<String> rewardsFile = FileUtils.readLines(new File("resources/lootbox/rewards.txt"));
            //For each reward specified in file:
            for (String line : rewardsFile) {
                //Rewards in text format goes as follows:
                //RARITY ITEM VALUE
                //Split these arguments into their own array.
                String[] args = line.split(" ");
                String rarity = args[0];
                String item = args[1];
                String value = args[2];
                //Switch through the varying items.
                switch (item) {
                    //If topkek:
                    case "TOPKEK":
                        //add a new TopKek reward:
                        rewards.add(new LootBox.TopKekReward(LootBox.Rarity.valueOf(rarity), Integer.valueOf(value)));
                        break;
                    //If background:
                    case "BG":
                        //add a new BG reward:
                        rewards.add(new LootBox.BGReward(LootBox.Rarity.valueOf(rarity), value));
                        break;
                    //If KXP:
                    case "KXP":
                        //add a new KXP reward:
                        rewards.add(new LootBox.KXPReward(LootBox.Rarity.valueOf(rarity), Integer.valueOf(value)));
                        break;
                    case "TOKEN":
                        //add a new token reward:
                        rewards.add(new LootBox.TokenReward(LootBox.Rarity.valueOf(rarity), Token.valueOf(value)));
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
