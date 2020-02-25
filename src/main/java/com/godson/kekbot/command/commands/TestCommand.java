package com.godson.kekbot.command.commands;


import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.profile.ProfileUtils;
import com.godson.kekbot.profile.item.LootBox;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.google.gson.JsonObject;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class TestCommand extends Command {

    public TestCommand() {
        name = "test";
        description = "test";
        category = new Category("Test");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        List<Profile> leaderboard = ProfileUtils.getLocalLeaderboard(event.getGuild());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append(leaderboard.get(i).getUser().getName() + " - Level: " + leaderboard.get(i).getLevel() + " - KXP: " + leaderboard.get(i).getKXP() + " - Topkeks: " + leaderboard.get(i).getTopkeks());
            builder.append("\n");
        }
        event.getChannel().sendMessage(builder.toString()).queue();
        /*LootBox lootBox = new LootBox();
        int attempts = 1;
        StringBuilder builder = new StringBuilder();
        for (LootBox.Rarity rarity : LootBox.Rarity.values()) {
            while (lootBox.getRarity() != rarity) {
                lootBox = new LootBox();
                attempts++;
            }
            builder.append(lootBox.getRarity().name()).append("! Took ").append(attempts).append(" attempts.\n");
            attempts = 0;
        }
        event.getChannel().sendMessage(builder.toString()).queue();*/
        //event.getClient().unregisterQuestionnaire();
    }
}
