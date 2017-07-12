package com.godson.kekbot.Music;

import com.godson.kekbot.Profile.Profile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String name;
    private List<AudioTrackInfo> tracks = new ArrayList<>();
    private boolean hidden = false;

    public Playlist(String name) {
        this.name = name;
    }

    private Playlist() {
    }

    public List<AudioTrackInfo> getTracks() {
        return tracks;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void addTrack(AudioTrack track) {
        tracks.add(track.getInfo());
    }

    public void removeTrack(AudioTrackInfo track) {
        tracks.remove(track);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isHidden() { return hidden; }

    public void saveToProfile(Profile profile) {
        profile.addPlaylist(this);
        profile.save();
    }

    public long getTotalLength() {
        long length = 0;
        for (AudioTrackInfo track : tracks) {
            length += track.length;
        }
        return length;
    }
}
