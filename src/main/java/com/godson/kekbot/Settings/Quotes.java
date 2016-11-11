package com.godson.kekbot.Settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.entities.Guild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Quotes {
    private List<String> quotes = new ArrayList<>();

    public Quotes() {}

    public void addQuote(String quote) {
        this.quotes.add(quote);
    }

    public List<String> getQuotes() {
        return quotes;
    }

    public String getQuote() {
        Random random = new Random();
        int index = random.nextInt(quotes.size());
        return quotes.get(index);
    }

    public String getQuote(int quoteNumber){
        return quotes.get(quoteNumber);
    }

    public void removeQuote(int quoteNumber) {
        quotes.remove(quoteNumber);
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this, this.getClass());
    }

    public void save(Guild guild) {
        File folder = new File("settings/" + guild.getId());
        File quotes = new File("settings/" + guild.getId() + "/Quotes.json");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try {
            FileWriter writer = new FileWriter(quotes);
            writer.write(this.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
