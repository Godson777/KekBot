package com.godson.kekbot.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
        backgrounds.add(new Background("8BIT_MARIO", "8-Bit Mario", "8BIT_MARIO.png", 2, 350,
                "Based on the most iconic character in all of gaming, show your love for the retro world with this 8-Bit Mario standing in front of the sunrise!"));
        backgrounds.add(new Background("SIMPLISTIC_PM", "Simplistic Paper Mario", "PAPER_MARIO_SIMPLISTIC.png", 2, 400,
                "Based on the most iconic character in all of gaming, show off your love for the popular spin off series with this background!"));
        backgrounds.add(new Background("CRAZY_CAP", "Crazy Cap", "CRAZY_CAP.png", 2, 500,
                "Submitted by `Mr. 31415926`.\nA background based on the store found in Super Mario Odyssey! Show off your Crazy Cap pride with this background!"));
        backgrounds.add(new Background("MANIA", "Sonic Mania Background", "MANIA.png", 1, 400,
                "This is the level start screen, found on the ~~only good~~ Sonic game: \"Sonic Mania\"."));
        backgrounds.add(new Background("SPACE_DOGE", "Space Doge", "SPACE_DOGE.png", 3, 750,
                "This is a space background. But there's a *doge* in it."));
        backgrounds.add(new Background("SPACE", "Space", "SPACE.png", 2, 500,
                "This is a backgound. In *spaaaaaaaaaaace*!"));
        backgrounds.add(new Background("NIGHT_SKY", "Night Sky", "NIGHT_SKY.png", 2, 250,
                "Stare at the night sky with this background!"));
        backgrounds.add(new Background("GOLDEN_LIGHT", "Golden Light", "GOLDEN_LIGHT.png", 10, 1000,
                "Show off your gold pride with this background. It's so shiny, we're only letting the best of the best get their hands on this. For a not-so-cheap price, anyway..."));

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
