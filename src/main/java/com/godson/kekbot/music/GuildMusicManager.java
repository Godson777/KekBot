package com.godson.kekbot.music;

import com.godson.kekbot.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

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
    public final MessageChannel channel;
    public final boolean meme;
    public User host;
    public boolean queueing = false;

    /**
     * Creates a player and a track scheduler.
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(AudioPlayerManager manager, CommandEvent event, boolean meme) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player, event);
        memeScheduler = new MemeScheduler(player, event.getGuild());
        channel = event.getChannel();
        this.meme = meme;
        if (meme) {
            player.addListener(memeScheduler);
        } else {
            player.addListener(scheduler);
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

    public boolean isMeme() {
        return meme;
    }
}
