package com.godson.kekbot.Music;

import com.godson.kekbot.Profile.Profile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String name;
    private List<AudioTrackInfo> tracks = new ArrayList<>();

    public List<AudioTrackInfo> getTracks() {
        return tracks;
    }

    public void addTrack(AudioTrack track) {
        tracks.add(track.getInfo());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void save() {
        File folder = new File("profiles");
        File settings = new File("test/" + "music.json");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try {
            FileWriter writer = new FileWriter(settings);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(this, this.getClass()));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Playlist getPlaylist() {
        Playlist playlist = new Playlist();
        if (new File("test/" + "music.json").exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader("test/" + "music.json"));
                Gson gson = new Gson();
                playlist = gson.fromJson(br, Playlist.class);
                br.close();
            } catch (FileNotFoundException e) {
                //do nothing
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return playlist;
    }
}
