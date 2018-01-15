package com.godson.kekbot.music;

import com.godson.kekbot.profile.Profile;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String name;
    private List<KAudioTrackInfo> tracks = new ArrayList<>();
    private boolean hidden = false;

    public Playlist(String name) {
        this.name = name;
    }

    private Playlist() {
    }

    public List<KAudioTrackInfo> getTracks() {
        return tracks;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void addTrack(AudioTrack track) {
        tracks.add(new KAudioTrackInfo(track.getInfo()));
    }

    public void removeTrack(KAudioTrackInfo track) {
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
        for (KAudioTrackInfo track : tracks) {
            length += track.length;
        }
        return length;
    }

    public static class KAudioTrackInfo extends AudioTrackInfo {

        private KAudioTrackInfo(String title, String author, long length, String identifier, boolean isStream, String uri) {
            super(title, author, length, identifier, isStream, uri);
        }

        public KAudioTrackInfo(AudioTrackInfo info) {
            super(info.title, info.author, info.length, info.identifier, info.isStream, info.uri);
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public long getLength() {
            return length;
        }

        public String getIdentifier() {
            return identifier;
        }

        public boolean getIsStream() {
            return isStream;
        }

        public String getUri() {
            return uri;
        }
    }
}
