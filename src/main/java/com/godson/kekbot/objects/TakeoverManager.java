package com.godson.kekbot.objects;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.responses.Responder;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.rethinkdb.gen.ast.ReqlObject;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.entities.Icon;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TakeoverManager {

    @SerializedName("Takeovers")
    private List<Takeover> takeovers = new ArrayList<>();
    @SerializedName("Takeover Active")
    private boolean takeoverActive;
    @SerializedName("Current Takeover")
    private String currentTakeover;



    public TakeoverManager() {
        Cursor cursor = KekBot.r.table("Takeovers").run(KekBot.conn);
        List<JSONObject> takeovers = cursor.bufferedItems();
        cursor.close();
        Gson gson = new Gson();
        takeovers.forEach(takeover -> this.takeovers.add(gson.fromJson(takeover.toJSONString(), Takeover.class)));
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
        takeovers.add(takeover);
    }

    public boolean startTakeover(String takeover) throws IOException {
        Optional<Takeover> takeoverOptional = takeovers.stream().filter(t -> t.name.equals(takeover)).findFirst();
        if (!takeoverOptional.isPresent()) return false;
        takeoverActive = true;
        FileWriter fileWriter = new FileWriter("takeover");
        fileWriter.write(takeover);
        fileWriter.flush();
        fileWriter.close();
        activateTakeover(takeoverOptional.get(), false);
        return true;
    }

    private void activateTakeover(Takeover takeover, boolean reboot) {
        if (!reboot) {
            try {
                KekBot.jda.getShards().get(0).getSelfUser().getManager().setAvatar(Icon.from(new File("takeovers/" + takeover.getName() + "/" + takeover.getAvaFile()))).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        KekBot.takeoverReponses(takeover.getResponses());
        KekBot.listener.gameStatus.takeoverGames(takeover.getGames());
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
            responses = new HashMap<Action, List<String>>();
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

            MapObject takeover = KekBot.r.hashMap()
                    .with("Name", name)
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
