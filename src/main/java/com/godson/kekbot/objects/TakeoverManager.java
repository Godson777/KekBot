package com.godson.kekbot.objects;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.responses.Action;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.api.entities.Icon;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class TakeoverManager {

    @SerializedName("Takeovers")
    private final List<Takeover> takeovers = new ArrayList<>();
    @SerializedName("Takeover Active")
    private boolean takeoverActive;
    @SerializedName("Current Takeover")
    private String currentTakeover;



    public TakeoverManager() {
        Cursor<org.json.simple.JSONObject> cursor = KekBot.r.table("Takeovers").run(KekBot.conn);
        List<org.json.simple.JSONObject> takeovers = cursor.bufferedItems();
        cursor.close();
        takeovers.forEach(takeover -> this.takeovers.add(new Gson().fromJson(takeover.toJSONString(), Takeover.class)));
//        com.rethinkdb.net.Result<Object> result = KekBot.r.table("Takeovers").run(KekBot.conn);
//        List<Object> takeover = result.toList();
//        result.close();
//        takeover.forEach(takeoverT -> this.takeovers.add(new Gson().fromJson(result.toString(), Takeover.class)));

        File currentTakeover = new File("takeover");
        takeoverActive = currentTakeover.exists();
        if (takeoverActive) {
            try {
                this.currentTakeover = FileUtils.readFileToString(currentTakeover, "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            activateTakeover(getCurrentTakeover(), true);
        }
    }

    public void addTakeover(Takeover takeover) {
        if (!hasTakeover(takeover.name)) takeovers.add(takeover);
    }

    public String startTakeover(String takeover) throws IOException {
        Optional<Takeover> takeoverOptional = takeovers.stream().filter(t -> t.name.equals(takeover)).findFirst();
        if (!takeoverOptional.isPresent()) return "Takeover not found.";
        for (Action action : Action.values()) {
            if (!takeoverOptional.get().getResponses().containsKey(action)) {
                return "Missing Response for Action: " + action.name();
            }
        }
        takeoverActive = true;
        FileWriter fileWriter = new FileWriter("takeover");
        fileWriter.write(takeover);
        fileWriter.flush();
        fileWriter.close();
        activateTakeover(takeoverOptional.get(), false);
        return "Takeover Started";
    }

    private void activateTakeover(Takeover takeover, boolean reboot) {
        if (!reboot) {
            try {
                if (!takeover.getAvaFile().equalsIgnoreCase("default"))
                    KekBot.jda.getShards().get(0).getSelfUser().getManager().setAvatar(Icon.from(new File("takeovers/" + takeover.getName() + "/" + takeover.getAvaFile()))).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!takeover.getResponses().isEmpty()) KekBot.takeoverReponses(takeover.getResponses());
        if (!takeover.getGames().isEmpty()) KekBot.listener.gameStatus.takeoverGames(takeover.getGames());
    }

    public void deactivateTakeover() {
        if (!isTakeoverActive()) return;

        KekBot.jda.getShards().get(0).getSelfUser().getManager().setAvatar(KekBot.pfp).queue();
        KekBot.resetResponses();
        KekBot.listener.gameStatus.resetGames();
        new File("takeover").delete();
        takeoverActive = false;
    }

    public boolean hasTakeover(String takeover) {
        return takeovers.stream().anyMatch(t -> t.getName().equals(takeover));
    }

    public Takeover getTakeover(String takeover) {
        return takeovers.stream().filter(t -> t.getName().equals(takeover)).findFirst().orElse(null);
    }

    public void removeTakeover(String takeover) {
        Takeover toRemove = takeovers.stream().filter(t -> t.getName().equals(takeover)).findFirst().orElse(null);
        if (toRemove == null) return;
        takeovers.remove(toRemove);
    }

    public Takeover getCurrentTakeover() {
        return takeovers.stream().filter(takeover -> takeover.getName().equals(currentTakeover)).findFirst().get();
    }

    public boolean isTakeoverActive() {
        return takeoverActive;
    }

    public static class Takeover {

        @SerializedName("Name")
        String name;
        @SerializedName("Avatar")
        String avaFile;
        @SerializedName("Games")
        List<String> games;
        @SerializedName("Responses")
        Map<Action, List<String>> responses;

        public Takeover() {
            games = new ArrayList<>();
            responses = new HashMap<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAvaFile() {
            return avaFile;
        }

        public void setAvaFile(String avaFile) {
            this.avaFile = avaFile;
        }

        public List<String> getGames() {
            return games;
        }

        public Map<Action, List<String>> getResponses() {
            return responses;
        }

        public void addResponse(Action action, String response) {
            if (responses.containsKey(action)) responses.get(action).add(response);
            else {
                List<String> strings = new ArrayList<>();
                strings.add(response);
                responses.put(action, strings);
            }
        }

        public boolean isUsable() {
            if (name == null) return false;
            if (avaFile == null) return false;
            if (games.isEmpty()) return false;
            if (responses.isEmpty()) return false;
            for (Action action : Action.values()) {
                if (!responses.containsKey(action)) return false;
            }
            return true;
        }

        public void save() {
            HashMap<String, List<String>> responses = new HashMap<>();

            this.responses.forEach((action, strings) -> responses.put(action.name(), strings));

            MapObject takeover = KekBot.r.hashMap("Name", name)
                    .with("Avatar", avaFile)
                    .with("Games", games)
                    .with("Responses", responses);


            if (KekBot.r.table("Takeovers").get(name).run(KekBot.conn) == null) {
                KekBot.r.table("Takeovers").insert(takeover).run(KekBot.conn);
            } else {
                KekBot.r.table("Takeovers").get(name).update(takeover).run(KekBot.conn);
            }
        }
    }

}
