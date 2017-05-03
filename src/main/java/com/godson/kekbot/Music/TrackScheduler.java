package com.godson.kekbot.Music;

import com.darichey.discord.api.CommandContext;
import com.godson.kekbot.KekBot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private final BlockingQueue<User> users;
    private final Guild guild;
    private final TextChannel channel;
    public User currentPlayer;
    public int voteSkip;
    public final List<User> voteSkippers = new ArrayList<>();
    public int repeat = 0;
    private final List<AudioTrack> repeatQueue = new ArrayList<>();
    private final List<User> repeatQueueUsers = new ArrayList<>();
    private int currentRepeatTrack = 0;

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
            if (repeat != 2) {
                queue.offer(track);
                users.offer(user);
            } else {
                repeatQueue.add(track);
                repeatQueueUsers.add(user);
            }
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        if (repeat != 2) {
            AudioTrack track = queue.poll();
            this.currentPlayer = users.poll();
            player.startTrack(track, false);
            channel.sendMessage("Now playing: `" + track.getInfo().title + "` Queued by: " + currentPlayer.getAsMention()).queue();
        } else {
            if (currentRepeatTrack < repeatQueue.size()-1) ++currentRepeatTrack;
            else currentRepeatTrack = 0;
            player.startTrack(repeatQueue.get(currentRepeatTrack).makeClone(), false);
            channel.sendMessage("Now playing: `" + repeatQueue.get(currentRepeatTrack).getInfo().title + "` Queued by: " + repeatQueueUsers.get(currentRepeatTrack).getAsMention()).queue();
        }

            clearVotes();
    }

    public void toggleRepeat() {
        switch (repeat) {
            case 0:
                ++repeat;
                break;
            case 1:
                ++repeat;
                repeatQueue.add(player.getPlayingTrack().makeClone());
                repeatQueueUsers.add(currentPlayer);
                queue.drainTo(repeatQueue);
                users.drainTo(repeatQueueUsers);
                break;
            case 2:
                repeat = 0;
                for (int i = currentRepeatTrack + 1; i < repeatQueue.size(); i++) {
                    queue.offer(repeatQueue.get(i));
                    this.users.offer(repeatQueueUsers.get(i));
                }
                repeatQueue.clear();
                repeatQueueUsers.clear();
        }
    }

    public void shuffle() {

    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            if (repeat == 1) player.startTrack(track.makeClone(), false);
            else if (queue.size() > 0 || repeat == 2) nextTrack();
            else KekBot.player.closeConnection(guild);
        }
    }

    private void clearVotes() {
        if (voteSkippers.size() > 0 && voteSkip > 0) {
            voteSkip = 0;
            voteSkippers.clear();
        }
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public BlockingQueue<User> getUsers() {
        return users;
    }

    public List<AudioTrack> getRepeatQueue() {
        return repeatQueue;
    }

    public List<User> getRepeatQueueUsers() {
        return repeatQueueUsers;
    }

    public int getCurrentRepeatTrack() {
        return currentRepeatTrack;
    }
}
