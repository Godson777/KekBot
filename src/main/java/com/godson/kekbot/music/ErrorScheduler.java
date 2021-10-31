package com.godson.kekbot.music;

import com.godson.kekbot.KekBot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;

public class ErrorScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final Guild guild;

    /**
     * @param player The audio player this scheduler uses
     */
    public ErrorScheduler(AudioPlayer player, Guild guild) {
        this.player = player;
        this.guild = guild;
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void play(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        player.startTrack(track, true);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            closeConnection();
        }
    }

    private void closeConnection() {
        new Thread(() -> KekBot.player.closeConnection(guild)).start();
    }
}
