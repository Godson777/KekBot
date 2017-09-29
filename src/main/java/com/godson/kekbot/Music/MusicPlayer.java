package com.godson.kekbot.Music;

import com.darichey.discord.api.CommandContext;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Questionaire.Questionnaire;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Utils;
import com.jagrosh.jdautilities.menu.pagination.PaginatorBuilder;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sun.org.apache.regexp.internal.RE;
import javafx.util.Pair;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MusicPlayer {

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public MusicPlayer() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public boolean isMeme(Guild guild) {
        return musicManagers.get(Long.parseLong(guild.getId())).isMeme();
    }

    public User getHost(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        return musicManagers.get(guildId).host;
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(CommandContext context, boolean meme) {
        long guildId = Long.parseLong(context.getGuild().getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, context, meme).setHost(context.getAuthor());
            context.getGuild().getAudioManager().setSendingHandler(musicManager.getSendHandler());
            musicManagers.put(guildId, musicManager);
        }

        return musicManager;
    }

    public int getActivePlayerCount() {
        return musicManagers.size();
    }

    public void changeHost(Guild guild, User user) {
        long guildId = Long.parseLong(guild.getId());
        musicManagers.get(guildId).setHost(user);
    }

    public void loadAndMeme(final CommandContext context, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(context, true);
        if (musicManager.isMeme()) {
            playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    playMeme(context, musicManager, track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    AudioTrack firstTrack = playlist.getSelectedTrack();
                    if (firstTrack == null) {
                        firstTrack = playlist.getTracks().get(0);
                    }
                    playMeme(context, musicManager, firstTrack);
                }

                @Override
                public void noMatches() {
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                }
            });
        } else {
            context.getTextChannel().sendMessage("I can't meme while music's playing...").queue();
        }
    }

    public void loadAndPlay(final CommandContext context, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(context, false);
        if (!musicManager.isMeme()) {
            playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    play(context, musicManager, track);
                    String timeBefore = "";
                    if (context.getGuild().getAudioManager().isConnected()) {
                        if (musicManager.scheduler.repeat != 2) {
                            final long[] totalLength = {0};
                            musicManager.scheduler.getQueue().forEach(list -> {
                                totalLength[0] += list.getKey().getDuration();
                            });
                            timeBefore = " (Time before it plays: " +
                                    Utils.convertMillisToTime(
                                            (musicManager.player.getPlayingTrack().getDuration() - musicManager.player.getPlayingTrack().getPosition() + (totalLength[0] - track.getDuration()))) + " **Queue Position: " + musicManager.scheduler.getQueue().size() + "**)";
                        } else {
                            long totalLength = 0;
                            List<Pair<AudioTrack, User>> playlist = musicManager.scheduler.getRepeatQueue();
                            for (int i = musicManager.scheduler.getCurrentRepeatTrack() + 1; i < playlist.size(); i++) {
                                totalLength += playlist.get(i).getKey().getDuration();
                            }
                            timeBefore = " (Time before it plays: " +
                                    Utils.convertMillisToTime(
                                            (musicManager.player.getPlayingTrack().getDuration() - musicManager.player.getPlayingTrack().getPosition() + (totalLength - track.getDuration()))) + " **Queue Position: " + musicManager.scheduler.getRepeatQueue().size() + "**)";

                        }
                        context.getTextChannel().sendMessage("Added \"" + track.getInfo().title + "\" to the queue." + timeBefore).queue();
                    }
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    int failed = 0;

                    for (AudioTrack track : playlist.getTracks()) {
                        if (track != null) {
                            play(context, musicManager, track);
                        } else failed++;
                    }

                    context.getTextChannel().sendMessage(context.getAuthor().getName() + " added " + (playlist.getTracks().size() - failed) + " tracks to the queue." + (failed > 0 ? " (" + failed + " track(s) could not be added.)" : "")).queue();
                }

                @Override
                public void noMatches() {
                    context.getTextChannel().sendMessage("Hm, " + trackUrl + " doesn't appear to be a valid URL. Could you try again?").queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    context.getTextChannel().sendMessage("Could not play: " + exception.getMessage()).queue();
                }
            });
        } else {
            context.getTextChannel().sendMessage("I can't play music while I'm memeing...").queue();
        }
    }

    public void loadAndPlay(final CommandContext context, final Playlist playlist) {
        GuildMusicManager musicManager = getGuildAudioPlayer(context, false);
        if (!musicManager.isMeme()) {
            context.getTextChannel().sendMessage("Attempting to add all the songs in " + playlist.getName() + ". (Note: This may take a while depending on the size of the playlist...)").queue();
            final int[] failed = {0};
            musicManager.queueing = true;

                for (int i = 0; i < playlist.getTracks().size(); i++) {
                    if (musicManager.queueing) {
                        String trackUrl = playlist.getTracks().get(i).uri;
                            playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
                                @Override
                                public void trackLoaded(AudioTrack track) {
                                    if (musicManager.queueing) {
                                        play(context, musicManager, track);
                                        if (playlist.getTracks().get(playlist.getTracks().size() - 1).uri.equals(trackUrl)) {
                                            context.getTextChannel().sendMessage("Complete." + (failed[0] > 0 ? " (" + failed[0] + " track(s) could not be added.)" : "")).queue();
                                            musicManager.queueing = false;
                                        }
                                    }
                                }

                                @Override
                                public void playlistLoaded(AudioPlaylist audioPlaylist) {
                                    //Do nothing since this'll never trigger.
                                }

                                @Override
                                public void noMatches() {
                                    //Do nothing since this'll never trigger.
                                }

                                @Override
                                public void loadFailed(FriendlyException exception) {
                                    if (musicManager.queueing) {
                                        failed[0]++;
                                        //The following statement is required in case the last track in the queue fails.
                                        if (playlist.getTracks().get(playlist.getTracks().size() - 1).uri.equals(trackUrl)) {
                                            context.getTextChannel().sendMessage("Complete." + (failed[0] > 0 ? " (" + failed[0] + " track(s) could not be added.)" : "")).queue();
                                            musicManager.queueing = false;
                                        }
                                    }
                                }
                            });
                    } else break;
                }
        } else {
            context.getTextChannel().sendMessage("I can't play music while I'm memeing...").queue();
        }
    }

    private void play(CommandContext context, GuildMusicManager musicManager, AudioTrack track) {
        connectToUsersVoiceChannel(context);

        musicManager.scheduler.queue(track, context.getAuthor());
    }

    private void playMeme(CommandContext context, GuildMusicManager musicManager, AudioTrack track) {
        connectToUsersVoiceChannel(context);

        musicManager.memeScheduler.queue(track);
    }

    public AudioTrack getCurrentTrack(CommandContext context) {
        return musicManagers.get(Long.valueOf(context.getGuild().getId())).player.getPlayingTrack();
    }

    public void skipTrack(CommandContext context, boolean vote) {
        GuildMusicManager musicManager = getGuildAudioPlayer(context, false);
        Guild guild = context.getGuild();
        if (!musicManager.isMeme()) {
            if (getHost(guild).equals(context.getAuthor()) || context.getMember().hasPermission(Permission.ADMINISTRATOR) || vote) {
                if (musicManager.scheduler.repeat != 2) {
                    if (musicManager.scheduler.getQueue().size() > 0) {
                        context.getTextChannel().sendMessage((!vote ? "Skipped to next track." : "Due to popular vote, this track was skipped.")).queue();
                        musicManager.scheduler.nextTrack();
                    } else {
                        context.getTextChannel().sendMessage("There are no more tracks to skip to!").queue();
                    }
                } else {
                    if (musicManager.scheduler.getRepeatQueue().size() > 1) {
                        context.getTextChannel().sendMessage((!vote ? "Skipped to next track." : "Due to popular vote, this track was skipped.")).queue();
                        musicManager.scheduler.nextTrack();
                    } else {
                        context.getTextChannel().sendMessage("There are no more tracks to skip to!").queue();
                    }
                }
            } else {
                context.getTextChannel().sendMessage("Only the host and users with the `Administrator` permission can skip tracks.").queue();
            }
        } else {
            context.getTextChannel().sendMessage("I can't skip memes. :neutral_face:").queue();
        }
    }

    public void pauseTrack(CommandContext context) {
        GuildMusicManager musicManager = getGuildAudioPlayer(context, false);
        Guild guild = context.getGuild();
        if (!musicManager.isMeme()) {
            if (getHost(guild).equals(context.getAuthor()) || context.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                if (!musicManager.player.isPaused()) {
                    musicManager.player.setPaused(true);
                    context.getTextChannel().sendMessage("Music Paused.").queue();
                } else {
                    musicManager.player.setPaused(false);
                    context.getTextChannel().sendMessage("Music Resumed.").queue();
                }
            } else {
                context.getTextChannel().sendMessage("Only the host and users with the `Administrator` permission can pause this session.").queue();
            }
        }
    }

    public void voteSkip(CommandContext context) {
        GuildMusicManager musicManager = getGuildAudioPlayer(context, false);
        Guild guild = context.getGuild();
        if (!musicManager.isMeme()) {
            if (musicManager.scheduler.getQueue().size() > 0) {
                if (!musicManager.scheduler.voteSkippers.contains(context.getAuthor())) {
                    ++musicManager.scheduler.voteSkip;
                    musicManager.scheduler.voteSkippers.add(context.getAuthor());
                    int users = guild.getAudioManager().getConnectedChannel().getMembers().size() - 1;
                    if (musicManager.scheduler.voteSkip == (users < 4 ? Math.ceil(users * 0.6) : Math.round(users * 0.6))) {
                        skipTrack(context, true);
                    } else {
                        context.getTextChannel().sendMessage("Your vote has been added. (" + musicManager.scheduler.voteSkip + "/" + Math.round((context.getGuild().getAudioManager().getConnectedChannel().getMembers().size() - 1) * 0.6) + ")").queue();
                    }
                } else {
                    context.getTextChannel().sendMessage("You've already voted to skip.").queue();
                }
            } else {
                context.getTextChannel().sendMessage("There are no more tracks to skip to!").queue();
            }
        }
    }

    public void repeat(CommandContext context) {
        GuildMusicManager musicManager = getGuildAudioPlayer(context, false);
        if (!musicManager.isMeme()) {
            musicManager.scheduler.toggleRepeat();
            if (musicManager.scheduler.repeat == 0) context.getTextChannel().sendMessage("Repeat is now set to: **OFF**.").queue();
            else if (musicManager.scheduler.repeat == 1) context.getTextChannel().sendMessage("Repeat is now set to **SINGLE**.").queue();
            else if (musicManager.scheduler.repeat == 2) context.getTextChannel().sendMessage("Repeat is now set to **MULTI**.").queue();
        }
    }

    private void connectToUsersVoiceChannel(CommandContext context) {
        AudioManager audioManager = context.getGuild().getAudioManager();
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            Optional<VoiceChannel> voiceChannel = context.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(context.getMember())).findFirst();
            if (!voiceChannel.isPresent()) {
                context.getTextChannel().sendMessage(KekBot.respond(context, Action.GET_IN_VOICE_CHANNEL)).queue();
            } else {
                audioManager.openAudioConnection(voiceChannel.get());
                if (!isMeme(context.getGuild())) {
                    context.getTextChannel().sendMessage(context.getAuthor().getAsMention() + " is now hosting a music session in: `" + voiceChannel.get().getName() + "`" + KekBot.replacePrefix(context.getGuild(), ", use {p}music to get the list of all music commands.")).queue();
                    musicManagers.get(Long.parseLong(context.getGuild().getId())).scheduler.currentPlayer = context.getAuthor();
                }
            }
        }
    }

    public void announceToMusicSession(Guild guild, String message) {
        musicManagers.get(Long.parseLong(guild.getId())).channel.sendMessage(message).queue();
    }

    public void getPlaylist(CommandContext context) {
        long guildId = Long.parseLong(context.getGuild().getId());
        if (musicManagers.containsKey(guildId)) {
            GuildMusicManager musicManager = musicManagers.get(guildId);
            if (!musicManager.isMeme()) {
                if (musicManager.scheduler.repeat != 2) {
                    if (musicManager.scheduler.getQueue().size() > 0) {
                        List<String> tracks = new ArrayList<>();
                        long totalLength = 0;
                        List<Pair<AudioTrack, User>> queue = new ArrayList<>();
                        queue.addAll(musicManager.scheduler.getQueue());
                        for (int i = 0; i < queue.size(); i++) {
                            AudioTrack track = queue.get(i).getKey();
                            User user = queue.get(i).getValue();
                            tracks.add(i + 1 + ".) " + track.getInfo().title + " - **(" + Utils.convertMillisToHMmSs(track.getDuration()) + ")** - " + "Queued by: " + user.getName());
                            totalLength += track.getDuration();
                        }
                        /*Below is unused code. Whether this will ever be removed or not is currently unknown,
                          mostly due to the fact that while this code is now obsolete, it may still have some use later on.
                        String playlist;
                        try {
                            if ((page * 15) > tracks.size() || (page * 15) < 0) playlist = "That page doesn't exist!";
                            else playlist = StringUtils.join(tracks.subList((page * 15), ((page + 1) * 15)), "\n") +
                                    (tracks.size() > 15 ? "\n\nPage " + (page + 1) + "/" + (tracks.size() / 15 + 1) +
                                            (page == 0 ? KekBot.replacePrefix(channel.getGuild(), "\n\nDo {p}playlist <number> to view that page.") : "") : "");
                        } catch (IndexOutOfBoundsException e) {
                            playlist = StringUtils.join(tracks.subList((page * 15), tracks.size()), "\n") +
                                    (tracks.size() > 15 ? "\n\nPage " + (page + 1) + "/" + (tracks.size() / 15 + 1) : "");
                        }
                        channel.sendMessage(playlist + "\n**Total Length: " + (musicManager.scheduler.repeat == 1 ? "∞ Infinity." : Utils.convertMillisToTime(totalLength)) + "**").queue();*/
                        PaginatorBuilder pb = new PaginatorBuilder();
                        pb.addItems(tracks.toArray(new String[tracks.size()]));
                        pb.setText("**Total Length: " + (musicManager.scheduler.repeat == 1 ? "∞ Infinity." : Utils.convertMillisToTime(totalLength)) + "**");
                        pb.setEventWaiter(KekBot.waiter);
                        pb.setItemsPerPage(15);
                        pb.setColor(context.getGuild().getSelfMember().getColor() == null?Color.RED:context.getGuild().getSelfMember().getColor());
                        pb.setTimeout(1, TimeUnit.MINUTES);
                        pb.showPageNumbers(true);
                        pb.waitOnSinglePage(true);
                        pb.setUsers(context.getAuthor());
                        pb.build().display(context.getTextChannel());
                    } else context.getTextChannel().sendMessage("There is nothing in the playlist!").queue();
                } else {
                    List<String> tracks = new ArrayList<>();
                    List<Pair<AudioTrack, User>> queue = new ArrayList<>();
                    queue.addAll(musicManager.scheduler.getQueue());
                    for (int i = 0; i < queue.size(); i++) {
                        AudioTrack track = queue.get(i).getKey();
                        User user = queue.get(i).getValue();
                        tracks.add(i + 1 + ".) " + track.getInfo().title + " - **(" + Utils.convertMillisToHMmSs(track.getDuration()) + ")** - " + "Queued by: " + user.getName() + (i == musicManager.scheduler.getCurrentRepeatTrack() ? " **(Current Track)**" : ""));
                    }
                    /*Also unused code.
                    String playlist;
                    try {
                        if ((page * 15) > tracks.size() || (page * 15) < 0) playlist = "That page doesn't exist!";
                        else playlist = StringUtils.join(tracks.subList((page * 15), ((page + 1) * 15)), "\n") +
                                (tracks.size() > 15 ? "\n\nPage " + (page + 1) + "/" + (tracks.size() / 15 + 1) +
                                        (page == 0 ? KekBot.replacePrefix(channel.getGuild(), "\n\nDo {p}playlist <number> to view that page.") : "") : "");
                    } catch (IndexOutOfBoundsException e) {
                        playlist = StringUtils.join(tracks.subList((page * 15), tracks.size()), "\n") +
                                (tracks.size() > 15 ? "\n\nPage " + (page + 1) + "/" + (tracks.size() / 15 + 1) : "");
                    }
                    */
                    PaginatorBuilder pb = new PaginatorBuilder();
                    pb.addItems(tracks.toArray(new String[tracks.size()]));
                    pb.setText("\n**Total Length: " + "∞ Infinity.**");
                    pb.setEventWaiter(KekBot.waiter);
                    pb.setItemsPerPage(15);
                    pb.setColor(context.getGuild().getSelfMember().getColor() == null?Color.RED:context.getGuild().getSelfMember().getColor());
                    pb.setTimeout(1, TimeUnit.MINUTES);
                    pb.showPageNumbers(true);
                    pb.waitOnSinglePage(true);
                    pb.setUsers(context.getAuthor());
                    pb.build().display(context.getTextChannel());
                }
            } else context.getTextChannel().sendMessage("I'm memeing at the moment, there is no playlist...").queue();
        } else context.getTextChannel().sendMessage("There is no music playing!").queue();
    }

    public void getCurrentSong(TextChannel channel) {
        long guildId = Long.parseLong(channel.getGuild().getId());
        if (musicManagers.containsKey(guildId)) {
            if (!musicManagers.get(guildId).isMeme()) {
                AudioTrack track = musicManagers.get(guildId).player.getPlayingTrack();
                channel.sendMessage("Currently Playing: `" + track.getInfo().title + "` [" + Utils.songTimestamp(track.getPosition(), track.getDuration()) + "]"
                        + "\nSong URL: `" + musicManagers.get(guildId).player.getPlayingTrack().getInfo().uri + "`"
                        + "\nQueued by: " + musicManagers.get(guildId).scheduler.currentPlayer.getName()
                        + "\nVolume: " + musicManagers.get(guildId).player.getVolume()).queue();
            } else {
                channel.sendMessage("I'm memeing, there is no song playing.").queue();
            }
        }
    }

    public void closeConnection(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        if (!this.musicManagers.get(guildId).queueing) {
            if (!isMeme(guild)) announceToMusicSession(guild, "This music session has now ended.");
            this.musicManagers.remove(guildId);
            guild.getAudioManager().closeAudioConnection();
        } else {
            musicManagers.get(guildId).queueing = false;
            if (!isMeme(guild)) announceToMusicSession(guild, "This music session has now ended.");
            this.musicManagers.get(guildId).player.destroy();
            this.musicManagers.remove(guildId);
            guild.getAudioManager().closeAudioConnection();
        }
    }

    public void closeConnection(Guild guild, String reason) {
        long guildId = Long.parseLong(guild.getId());
        if (!this.musicManagers.get(guildId).queueing) {
            if (!isMeme(guild)) announceToMusicSession(guild, reason);
            this.musicManagers.remove(guildId);
            guild.getAudioManager().closeAudioConnection();
        } else {
            musicManagers.get(guildId).queueing = false;
            if (!isMeme(guild)) announceToMusicSession(guild, reason);
            this.musicManagers.get(guildId).player.destroy();
            this.musicManagers.remove(guildId);
            guild.getAudioManager().closeAudioConnection();
        }
    }

    public boolean containsConnection(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        return musicManagers.containsKey(guildId);
    }

    public void killConnection(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        this.musicManagers.remove(guildId);
    }

    public void setVolume(CommandContext context, int volume) {
        long guildId = Long.parseLong(context.getGuild().getId());
        if (musicManagers.containsKey(guildId)) {
            if (volume <= 100 && volume >= 0) {
                musicManagers.get(guildId).player.setVolume(volume);
                context.getTextChannel().sendMessage("Volume set to " + volume).queue();
            } else context.getTextChannel().sendMessage("Specified volume must be between 100 and 0!").queue();
        }
    }

    public void shuffle(CommandContext context) {
        long guildId = Long.parseLong(context.getGuild().getId());
        if (musicManagers.containsKey(guildId)) {
            musicManagers.get(guildId).scheduler.shuffle();
        }
        context.getTextChannel().sendMessage("Shuffled! \uD83D\uDD04").queue();
    }

    public void addToPlaylist(Questionnaire.Results results, String trackUrl, Playlist playlist) {
        playerManager.loadItemOrdered(playlist, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (playlist.getTracks().stream().noneMatch(audioTrackInfo -> audioTrackInfo.uri.equals(track.getInfo().uri))) {
                    playlist.addTrack(track);
                    results.getChannel().sendMessage("Added " + track.getInfo().title + " to the playlist.").queue();
                    results.reExecuteWithoutMessage();
                } else {
                    results.getChannel().sendMessage("This track is already in your playlist.").queue();
                    results.reExecuteWithoutMessage();
                }

            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                new Questionnaire(results)
                        .addChoiceQuestion("Are you sure you want to add all " + audioPlaylist.getTracks().size() + " tracks to your playlist?", "Yes", "No", "Y", "N")
                        .withoutRepeats()
                        .execute(results1 -> {
                            if (results1.getAnswer(0).toString().equalsIgnoreCase("Yes") || results1.getAnswer(0).toString().equalsIgnoreCase("Y")) {
                                int existing = 0;
                                for (AudioTrack track : audioPlaylist.getTracks()) {
                                    if (playlist.getTracks().stream().noneMatch(audioTrackInfo -> audioTrackInfo.uri.equals(track.getInfo().uri))) {
                                        playlist.addTrack(track);
                                    } else existing++;
                                }
                                results1.getChannel().sendMessage("Done." + (existing > 0 ? " (" + existing + " tracks were already in your playlist, so they were skipped.)" : "")).queue();
                                results.reExecuteWithoutMessage();
                            } else {
                                results1.getChannel().sendMessage("Alright, I won't add those tracks. You can still paste URLs, though.").queue();
                                results.reExecuteWithoutMessage();
                            }
                        });
            }

            @Override
            public void noMatches() {
                results.getChannel().sendMessage("Hm, `" + trackUrl + "` doesn't appear to be a valid URL. Could you try again?").queue();
                results.reExecuteWithoutMessage();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                results.getChannel().sendMessage("Could not add to the playlist: " + exception.getMessage()).queue();
                results.reExecuteWithoutMessage();
            }
        });
    }
}
