package com.godson.kekbot.Profile;

import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.Settings.Quotes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BackgroundManager {
    List<Background> backgrounds = new ArrayList<>();

    public BackgroundManager() {
        prepareList();
    }

    private void prepareList() {
        //Unobtainable Backgrounds
        backgrounds.add(new Background("GRAND_DAD", "GRAND DAD", "GRAND.png"));

        //Obtainable Backgrounds
        backgrounds.add(new Background("DRAWN_ML", "Drawn Series: Mario & Luigi", "DRAWN_M&L.png", 1, 250,
                "Submitted by `Sgt.Psychiatrist`.\nA fan redrawing of Mario and Luigi in \"Mario & Luigi: Superstar Saga\"."));
        backgrounds.add(new Background("DRAWN_WINDWAKER", "Drawn Series: Wind Waker", "DRAWN_WINDWAKER.png", 1, 250,
                "Submitted by `Sgt.Psychiatrist#`.\nA fan redrawing of Toon Link in \"The Legend of Zelda: Wind Waker\"."));
        backgrounds.add(new Background("SAMURAI_JACK_TREE", "Samurai Jack (Cherry Tree)", "SAMURAI_JACK_TREE.png", 1, 350,
                "Submitted by `SkylordryanZ™`.\nA scene from Samurai Jack (Season 5), where Jack is sitting under a cherry tree."));
        backgrounds.add(new Background("MLG", "MLG", "MLG.png", 2, 420,
                "Show everyone your 420 noscoping power with this background!"));
        backgrounds.add(new Background("HYPER_BEAST", "Hyper Beast", "HYPER_BEAST.png", 2, 500,
                "Submitted by `SkylordryanZ™`.\nThe hyper beast has arrived! Unleash your own inner beast with this background!"));
        backgrounds.add(new Background("SAMURAI_JACK_RAIN", "Samurai Jack (Rain)", "SAMURAI_JACK_RAIN.png", 2, 500,
                "Submitted by `SkylordryanZ™`.\nA scene from the teaser trailer for Samurai Jack (Season 5)."));

        if (!getExclusiveBackgrounds().isEmpty()) {
            backgrounds.addAll(getExclusiveBackgrounds());
        }

    }

    public Background get(String ID) {
        if (doesBackgroundExist(ID)) {
            return backgrounds.stream().filter(background -> background.getID().equals(ID)).findFirst().get();
        } else throw new NullPointerException();
    }

    public boolean doesBackgroundExist(String ID) {
        return backgrounds.stream().anyMatch(background -> background.getID().equals(ID));
    }

    public List<Background> getBackgrounds() {
        return backgrounds;
    }

    private List<Background> getExclusiveBackgrounds() {
        List<Background> backgrounds = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("config/exclusiveBackgrounds.json"));
            Gson gson = new Gson();
            Type type = new TypeToken<List<Background>>(){}.getType();
            backgrounds = gson.fromJson(br, type);
            br.close();
        } catch (FileNotFoundException e) {
            //do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
        return backgrounds;
    }

    public void addExclusiveBackground(String ID, String name, String file) {
        List<Background> backgrounds = getExclusiveBackgrounds();
        backgrounds.add(new Background(ID, name, file));
        File settings = new File("config/exclusiveBackgrounds.json");
        try {
            FileWriter writer = new FileWriter(settings);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(backgrounds));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
