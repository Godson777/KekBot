package com.godson.kekbot.responses;

import com.godson.kekbot.KekBot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Responder {
    @SerializedName("Action")
    private Action action;
    @SerializedName("Responses")
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

    public static Responder getResponder(Action action) {
        if (KekBot.r.table("Responses").get(action.name()).run(KekBot.conn) != null) {
            Gson gson = new Gson();
            return gson.fromJson((String) KekBot.r.table("Responses").get(action.name()).toJson().run(KekBot.conn), Responder.class);
        } else return new Responder(action);
    }

    public void save() {
        if (KekBot.r.table("Responses").get(action.name()).run(KekBot.conn) != null) {
            KekBot.r.table("Responses").get(action.name()).update(KekBot.r.hashMap("Responses", responses)).run(KekBot.conn);
        } else KekBot.r.table("Responses").insert(KekBot.r.hashMap("Action", action.name()).with("Responses", responses)).run(KekBot.conn);
    }
}
