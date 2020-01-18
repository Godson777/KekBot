package com.godson.kekbot.music;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GuildMusicManager {
    /**
     * Audio player for the guild.
     */
    public final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    public final TrackScheduler scheduler;
    public final MemeScheduler memeScheduler;
    public final ErrorScheduler errorScheduler;
    public final long channelID;
    public final int status;
    public User host;
    public boolean queueing = false;
    public boolean waiting = false;

    /**
     * Creates a player and a track scheduler.
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(AudioPlayerManager manager, CommandEvent event, int status) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player, event);
        memeScheduler = new MemeScheduler(player, event.getGuild());
        errorScheduler = new ErrorScheduler(player, event.getGuild());
        channelID = event.getChannel().getIdLong();
        this.status = status;
        switch (status) {
            case 0: player.addListener(scheduler);
            break;
            case 1: player.addListener(memeScheduler);
            break;
            case 2: player.addListener(errorScheduler);
            break;
        }
    }

    public GuildMusicManager setHost(User host) {
        this.host = host;
        return this;
    }

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }

    public int getStatus() {
        return status;
    }

    public boolean isMusic() {
        return status == 0;
    }

    public void stopWaiting() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            waiting = false;
            executor.shutdown();
        }, 1, TimeUnit.NANOSECONDS);
    }
}
