package com.godson.kekbot.Music;

import com.darichey.discord.api.CommandContext;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.entities.TextChannel;
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
    public final TextChannel channel;
    public final boolean meme;
    public User host;
    public boolean queueing = false;

    /**
     * Creates a player and a track scheduler.
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(AudioPlayerManager manager, CommandContext context, boolean meme) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player, context);
        memeScheduler = new MemeScheduler(player, context.getGuild());
        channel = context.getTextChannel();
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
