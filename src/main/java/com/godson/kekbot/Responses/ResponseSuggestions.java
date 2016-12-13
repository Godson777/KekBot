package com.godson.kekbot.Responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.core.entities.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResponseSuggestions {
    List<ResponseSuggestion> suggestions = new ArrayList<>();

    public ResponseSuggestions () {}

    public ResponseSuggestions addSuggestion(User suggester, Action action, String response) {
        suggestions.add(new ResponseSuggestion(suggester, action, response));
        return this;
    }

    public List<ResponseSuggestion> getSuggestions() {
        return suggestions;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this, this.getClass());
    }

    public void save() {
        File folder = new File("responses/");
        File settings = new File("responses/responses.json");
        if (!folder.exists()) {
            folder.mkdir();
        }
        try {
            FileWriter writer = new FileWriter(settings);
            writer.write(this.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
