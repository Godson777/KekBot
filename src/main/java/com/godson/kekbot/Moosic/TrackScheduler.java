package com.godson.kekbot.Moosic;

import com.darichey.discord.api.CommandContext;
import com.godson.kekbot.KekBot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private final BlockingQueue<User> users;
    private final Guild guild;
    private final TextChannel channel;
    public User currentPlayer;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player, CommandContext context) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.users = new LinkedBlockingQueue<>();
        this.guild = context.getGuild();
        this.channel = context.getTextChannel();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track, User user) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(track);
            users.offer(user);
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        AudioTrack track = queue.poll();
        this.currentPlayer = users.poll();
        player.startTrack(track, false);
        channel.sendMessage(currentPlayer.getAsMention() + ", your song `" + track.getInfo().title + "` is now being played.").queue();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            if (queue.size() > 0) nextTrack();
            else KekBot.player.closeConnection(guild);
        }
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public BlockingQueue<User> getUsers() {
        return users;
    }
}
