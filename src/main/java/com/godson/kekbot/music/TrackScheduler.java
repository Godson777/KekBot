package com.godson.kekbot.music;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import javafx.util.Pair;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.*;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<Pair<AudioTrack, User>> queue;
    private final Guild guild;
    private final TextChannel channel;
    public User currentPlayer;
    public int voteSkip;
    public final List<User> voteSkippers = new ArrayList<>();
    public int repeat = 0;
    private final List<Pair<AudioTrack, User>> repeatQueue = new ArrayList<>();
    private int currentRepeatTrack = 0;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player, CommandEvent event) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.guild = event.getGuild();
        this.channel = event.getTextChannel();
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
                queue.offer(new Pair<>(track, user));
            } else {
                repeatQueue.add(new Pair<>(track, user));
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
            Pair<AudioTrack, User> pair = queue.poll();
            AudioTrack track = pair.getKey();
            this.currentPlayer = pair.getValue();
            player.startTrack(track, false);
            //channel.sendMessage("Now playing: `" + track.getInfo().title + "` Queued by: " + currentPlayer.getAsMention()).queue();
            channel.sendMessage(embedTrack(track.getInfo(), currentPlayer)).queue();
        } else {
            if (currentRepeatTrack < repeatQueue.size()-1) ++currentRepeatTrack;
            else currentRepeatTrack = 0;
            player.startTrack(repeatQueue.get(currentRepeatTrack).getKey().makeClone(), false);
            //channel.sendMessage("Now playing: `" + repeatQueue.get(currentRepeatTrack).getKey().getInfo().title + "` Queued by: " + repeatQueue.get(currentRepeatTrack).getValue().getAsMention()).queue();
            channel.sendMessage(embedTrack(repeatQueue.get(currentRepeatTrack).getKey().getInfo(), repeatQueue.get(currentRepeatTrack).getValue())).queue();
        }
            clearVotes();
    }

    private MessageEmbed embedTrack(AudioTrackInfo track, User queuer) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(channel.getGuild().getSelfMember().getColor() == null ? Color.RED : channel.getGuild().getSelfMember().getColor());
        builder.addField("Now Playing:", track.title, true);
        builder.addField("Queued By:", queuer.getAsMention(), true);
        builder.addField("URL:", track.uri, false);
        builder.setThumbnail(Utils.getUserAvatarURL(queuer));
        return builder.build();
    }

    public void toggleRepeat() {
        switch (repeat) {
            case 0:
                ++repeat;
                break;
            case 1:
                ++repeat;
                repeatQueue.add(new Pair<>(player.getPlayingTrack().makeClone(), currentPlayer));
                queue.drainTo(repeatQueue);
                break;
            case 2:
                repeat = 0;
                for (int i = currentRepeatTrack + 1; i < repeatQueue.size(); i++) {
                    queue.offer(repeatQueue.get(i));
                }
                repeatQueue.clear();
        }
    }

    public void shuffle() {
        List<Pair<AudioTrack, User>> queue = new ArrayList<>();
        if (repeat != 2) {
            this.queue.drainTo(queue);
            Collections.shuffle(queue);
            this.queue.addAll(queue);
        } else {
            queue.addAll(repeatQueue);
            repeatQueue.clear();
            Collections.shuffle(queue);
            repeatQueue.addAll(queue);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            if (repeat == 1) player.startTrack(track.makeClone(), false);
            else if (queue.size() > 0 || repeat == 2) nextTrack();
            else closeConnection();
        }
    }

    private void clearVotes() {
        if (voteSkippers.size() > 0 && voteSkip > 0) {
            voteSkip = 0;
            voteSkippers.clear();
        }
    }

    private void closeConnection() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            KekBot.player.closeConnection(guild);
            executor.shutdown();
        }, 0, TimeUnit.SECONDS);
    }

    public BlockingQueue<Pair<AudioTrack, User>> getQueue() {
        return queue;
    }

    public List<Pair<AudioTrack, User>> getRepeatQueue() {
        return repeatQueue;
    }

    public int getCurrentRepeatTrack() {
        return currentRepeatTrack;
    }
}
