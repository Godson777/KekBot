package com.godson.kekbot.command.commands.fun;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.entities.TextChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class UrbanDictionary {
    public static Command UrbanDictionary = new Command("ud")
            .withCategory(CommandCategory.FUN)
            .withDescription("Performs a search on Urban Dictionary.")
            .withUsage("{p}ud <term>")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                if (rawSplit.length == 1) {
                    channel.sendMessage("Next time, supply a word or phrase for me to look up!");
                } else {
                    try {
                        HttpResponse<String> response = Unirest.get("https://mashape-community-urban-dictionary.p.mashape.com/define?term=" + rawSplit[1].replace(" ", "-"))
                                .header("X-Mashape-Key", "ceU4edWIr7mshi68Xs4IQYUQ7XgTp1ILJUgjsnsO4Qf4MOc543")
                                .header("Accept", "text/plain")
                                .asString();
                        BufferedReader in = new BufferedReader(new InputStreamReader(response.getRawBody()));
                        StringBuilder response2 = new StringBuilder();
                        String inputLine;

                        while ((inputLine = in.readLine()) != null) response2.append(inputLine);
                        in.close();
                        String result = response2.toString();
                        byte[] mapData = result.getBytes();
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(mapData);
                        Random random = new Random();
                        JsonNode dictionary = rootNode.path("list").get(random.nextInt(rootNode.path("list").size()));
                        if (!rootNode.path("result_type").textValue().equals("no_results")) {
                            String ud = "**Term:** *" + dictionary.path("word").textValue() +
                                    "*\n\nDefinition: " + dictionary.path("definition").textValue() +
                                    "\n\nExamples: " + dictionary.path("example").textValue() + "\n\n" + dictionary.path("permalink").textValue();
                            if (ud.length() > 2000) {
                                channel.sendMessage("The definition I found is too long! Either try again to get receive a different one, or visit this link to see the" +
                                        "definition I found! \n" + dictionary.path("permalink").textValue());
                            } else {
                                channel.sendMessage(ud);
                            }
                        } else {
                            channel.sendMessage("No definition found.");
                        }
                    } catch (UnirestException | IOException e) {
                        e.printStackTrace();
                    }
                }
            });
}
