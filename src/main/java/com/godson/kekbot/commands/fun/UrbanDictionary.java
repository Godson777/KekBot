package com.godson.kekbot.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.Objects.UDictionary;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Random;

public class UrbanDictionary {
    public static Command UrbanDictionary = new Command("ud")
            .withCategory(CommandCategory.FUN)
            .withDescription("Performs a search on Urban Dictionary.")
            .withUsage("{p}ud <term>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                if (rawSplit.length == 1) {
                    channel.sendMessage("Next time, supply a word or phrase for me to look up!").queue();
                } else {
                    UDictionary results = GSONUtils.getUDResults(rawSplit[1].replace(" ", "+"));
                    Random random = new Random();
                    if (!results.getResultType().equals("no_results")) {
                        UDictionary.Definition definition = results.getDefinitions().get(random.nextInt(results.getDefinitions().size()));
                        String ud = "**Term:** *" + definition.getWord() +
                                "*\n\n**Definition: **" + definition.getDefinition() +
                                "\n\n**Examples: **" + definition.getExample() +
                                "\n\n" + definition.getPermalink();
                        if (ud.length() > 2000) {
                            channel.sendMessage("The definition I found is too long! Either try again to getResponder receive a different one, or visit this link to see the" +
                                    "definition I found! \n" + definition.getPermalink()).queue();
                        } else {
                            channel.sendMessage(ud).queue();
                        }
                    } else {
                        channel.sendMessage("No definition found.").queue();
                    }
                }
            });
}
