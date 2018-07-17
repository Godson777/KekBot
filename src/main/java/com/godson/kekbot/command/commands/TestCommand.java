package com.godson.kekbot.command.commands;


import com.godson.kekbot.profile.item.LootBox;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

public class TestCommand extends Command {

    public TestCommand() {
        name = "test";
        description = "test";
        category = new Category("Test");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        LootBox lootBox = new LootBox();
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
        event.getChannel().sendMessage(builder.toString()).queue();
    }
}
