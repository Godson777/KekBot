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
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
    private boolean started = false;

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

    private Pair<AudioTrack, User> getCurrentRepeatTrack() {
        return repeatQueue.get(currentRepeatTrack);
    }

    public void skipTrack(boolean vote) {
        nextTrack(!vote ? 1 : 2);
    }

    public void skipTracks(int toSkip) {
        String message = "Skipped " + toSkip + " tracks.";
        if (repeat != 2) {
            if (toSkip > queue.size()) return;

            for (int i = 0; i < toSkip - 1; i++) {
                queue.poll();
            }
            channel.sendMessage(getTrackMessage(message)).queue();
            Pair<AudioTrack, User> pair = queue.poll();
            AudioTrack track = pair.getKey();
            this.currentPlayer = pair.getValue();
            player.startTrack(track, false);
        } else {
            currentRepeatTrack = (currentRepeatTrack + toSkip) % repeatQueue.size();
            channel.sendMessage(getTrackMessage(message)).queue();
            player.startTrack(getCurrentRepeatTrack().getKey().makeClone(), false);
        }
    }

    public void skipToTrack(int skipTo) {
        String message = "Skipped to track #" + skipTo + ".";
        if (repeat != 2) {
            if (skipTo > queue.size()) return;

            for (int i = 0; i < skipTo - 1; i++) queue.poll();

            channel.sendMessage(getTrackMessage(message)).queue();
            Pair<AudioTrack, User> pair = queue.poll();
            AudioTrack track = pair.getKey();
            this.currentPlayer = pair.getValue();
            player.startTrack(track, false);
        } else {
            if (skipTo > repeatQueue.size()) return;

            currentRepeatTrack = skipTo;
            channel.sendMessage(getTrackMessage(message)).queue();
            player.startTrack(getCurrentRepeatTrack().getKey().makeClone(), false);
        }
    }

    public void nextTrack() {
        nextTrack(0);
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    private void nextTrack(int skip) {
        if (repeat != 2) {
            channel.sendMessage(getTrackMessage(skip)).queue();
            Pair<AudioTrack, User> pair = queue.poll();
            AudioTrack track = pair.getKey();
            this.currentPlayer = pair.getValue();
            player.startTrack(track, false);
        } else {
            channel.sendMessage(getTrackMessage(skip)).queue();
            if (currentRepeatTrack < repeatQueue.size() - 1) ++currentRepeatTrack;
            else currentRepeatTrack = 0;
            player.startTrack(getCurrentRepeatTrack().getKey().makeClone(), false);
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

    private Message getTrackMessage(int skip) {
        MessageBuilder builder = new MessageBuilder();
        if (skip == 1) builder.append("Skipped to next track.");
        if (skip == 2) builder.append("Due to popular vote, this track was skipped.");
        if (repeat != 2) {
            builder.setEmbed(embedTrack(queue.element().getKey().getInfo(), queue.element().getValue()));
        } else {
            builder.setEmbed(embedTrack(getCurrentRepeatTrack().getKey().getInfo(), getCurrentRepeatTrack().getValue()));
        }
        return builder.build();
    }

    private Message getTrackMessage(String note) {
        MessageBuilder builder = new MessageBuilder();
        builder.append(note);
        if (repeat != 2) {
            builder.setEmbed(embedTrack(queue.element().getKey().getInfo(), queue.element().getValue()));
        } else {
            builder.setEmbed(embedTrack(getCurrentRepeatTrack().getKey().getInfo(), getCurrentRepeatTrack().getValue()));
        }
        return builder.build();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            if (repeat == 1) player.startTrack(track.makeClone(), false);
            else if (queue.size() > 0 || repeat == 2) nextTrack(0);
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
        new Thread(() -> KekBot.player.closeConnection(guild)).start();
    }

    public AudioTrackInfo removeTrack(int toRemove) {
        AudioTrackInfo trackInfo = null;
        if (queue.size() > 0) {
            List<Pair<AudioTrack, User>> queue = new ArrayList<>();
            this.queue.drainTo(queue);
            trackInfo = queue.get(toRemove).getKey().getInfo();
            queue.remove(toRemove);
            this.queue.addAll(queue);
        } else {
            if (repeatQueue.size() > 0) {
                trackInfo = repeatQueue.get(toRemove).getKey().getInfo();
                repeatQueue.remove(toRemove);
            }
        }
        return trackInfo;
    }

    public BlockingQueue<Pair<AudioTrack, User>> getQueue() {
        return queue;
    }

    public int getQueueSize() {
        if (queue.size() == 0) return repeatQueue.size();
        else return queue.size();
    }

    public List<Pair<AudioTrack, User>> getRepeatQueue() {
        return repeatQueue;
    }

    public int getRepeat() {
        return currentRepeatTrack;
    }

    public boolean hasStarted() {
        return started;
    }

    public void setStarted() {
        started = true;
    }
}
