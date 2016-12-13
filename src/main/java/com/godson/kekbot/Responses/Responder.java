package com.godson.kekbot.Responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Responder {
    private Action action;
    @Expose
    private List<String> responses = new ArrayList<>();

    public Responder(Action action) {
        this.action = action;
    }

    public Responder setAction(Action action) {
        this.action = action;
        return this;
    }

    public Responder addResponse(String response) {
        responses.add(response);
        return this;
    }

    public List<String> getResponses() {
        return responses;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this, this.getClass());
    }

    public void save() {
        File folder = new File("responses/");
        File settings = new File("responses/" + action.name() + ".json");
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
