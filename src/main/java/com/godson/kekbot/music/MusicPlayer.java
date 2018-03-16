package com.godson.kekbot.music;

import com.darichey.discord.api.CommandContext;
import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.CommandEvent;
import com.jagrosh.jdautilities.menu.pagination.PaginatorBuilder;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import javafx.util.Pair;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.*;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MusicPlayer extends ListenerAdapter {

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public MusicPlayer() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public User getHost(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        return musicManagers.get(guildId).host;
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(CommandEvent event, int status) {
        long guildId = Long.parseLong(event.getGuild().getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null || (musicManager.getStatus() == 2 && status < 2)) {
            musicManager = new GuildMusicManager(playerManager, event, status).setHost(event.getEvent().getAuthor());
            event.getGuild().getAudioManager().setSendingHandler(musicManager.getSendHandler());
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

    public void loadAndError(final CommandEvent event, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event, 2);
        if (musicManager.getStatus() < 2) {
            return;
        }

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                playError(event, musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();
                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                playMeme(event, musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
            }

            @Override
            public void loadFailed(FriendlyException exception) {
            }
        });
    }

    public void loadAndMeme(final CommandEvent event, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event, 1);
        if (musicManager.getStatus() < 1) {
            event.getChannel().sendMessage("I can't meme while music's playing...").queue();
            return;
        }
            playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    playMeme(event, musicManager, track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    AudioTrack firstTrack = playlist.getSelectedTrack();
                    if (firstTrack == null) {
                        firstTrack = playlist.getTracks().get(0);
                    }
                    playMeme(event, musicManager, firstTrack);
                }

                @Override
                public void noMatches() {
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                }
            });
    }

    public void loadAndPlay(final CommandEvent event, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event, 0);
        if (musicManager.getStatus() == 1) {
            event.getChannel().sendMessage("I can't play music while I'm memeing...").queue();
            return;
        }
            playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    queueTrack(event, musicManager, track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    int failed = 0;

                    for (AudioTrack track : playlist.getTracks()) {
                        if (track != null) {
                            play(event, musicManager, track);
                        } else failed++;
                    }

                    event.getChannel().sendMessage(event.getEvent().getAuthor().getName() + " added " + (playlist.getTracks().size() - failed) + " tracks to the queue." + (failed > 0 ? " (" + failed + " track(s) could not be added.)" : "")).queue();
                }

                @Override
                public void noMatches() {
                    event.getChannel().sendMessage("Hm, `" + trackUrl + "` doesn't appear to be a valid URL. Could you try again?").queue();
                    if (musicManager.player.getPlayingTrack() == null) killConnection(event.getGuild());
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    event.getChannel().sendMessage("Could not play: " + exception.getMessage()).queue();
                    if (musicManager.player.getPlayingTrack() == null) killConnection(event.getGuild());
                }
            });
    }

    public void loadAndSearchYT(final CommandEvent event, final String search) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event, 0);
        if (musicManager.getStatus() == 1) {
            event.getChannel().sendMessage("I can't play music while I'm memeing...").queue();
            return;
        }
            playerManager.loadItemOrdered(musicManager, search, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    queueTrack(event, musicManager, track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    trackLoaded(playlist.getTracks().get(0));
                }

                @Override
                public void noMatches() {
                    event.getChannel().sendMessage("Hm, I can't seem to find `" + search.substring(9) + "` on youtube. Could you try something else?").queue();
                    if (musicManager.player.getPlayingTrack() == null) killConnection(event.getGuild());
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    event.getChannel().sendMessage("Could not play: " + exception.getMessage()).queue();
                    if (musicManager.player.getPlayingTrack() == null) killConnection(event.getGuild());
                }
            });
    }

    private void queueTrack(CommandEvent event, GuildMusicManager musicManager, AudioTrack track) {
        play(event, musicManager, track);
        if (musicManager.scheduler.getQueueSize() < 1) return;
        String timeBefore;
        if (event.getGuild().getAudioManager().isConnected()) {
            if (musicManager.scheduler.repeat != 2) {
                final long[] totalLength = {0};
                musicManager.scheduler.getQueue().forEach(list -> totalLength[0] += list.getKey().getDuration());
                timeBefore = " (Time before it plays: " +
                        Utils.convertMillisToTime(
                                (musicManager.player.getPlayingTrack().getDuration() - musicManager.player.getPlayingTrack().getPosition() + (totalLength[0] - track.getDuration()))) + " **Queue Position: " + musicManager.scheduler.getQueue().size() + "**)";
            } else {
                long totalLength = 0;
                List<Pair<AudioTrack, User>> playlist = musicManager.scheduler.getRepeatQueue();
                for (int i = musicManager.scheduler.getRepeat() + 1; i < playlist.size(); i++) {
                    totalLength += playlist.get(i).getKey().getDuration();
                }
                timeBefore = " (Time before it plays: " +
                        Utils.convertMillisToTime(
                                (musicManager.player.getPlayingTrack().getDuration() - musicManager.player.getPlayingTrack().getPosition() + (totalLength - track.getDuration()))) + " **Queue Position: " + musicManager.scheduler.getRepeatQueue().size() + "**)";

            }
            event.getChannel().sendMessage("Added \"" + track.getInfo().title + "\" to the queue." + timeBefore).queue();
        }
    }

    public void loadAndPlay(final CommandEvent event, final Playlist playlist, final Profile profile) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event, 0);
        if (musicManager.getStatus() == 1) {
            event.getChannel().sendMessage("I can't play music while I'm memeing...").queue();
            return;
        }
        event.getChannel().sendMessage("Attempting to add all the songs in " + playlist.getName() + ". (Note: This may take a while depending on the size of the playlist...)").queue();
        final int[] failed = {0};
        musicManager.queueing = true;

                for (int i = 0; i < playlist.getTracks().size(); i++) {
                    if (musicManager.queueing) {
                        String trackUrl = playlist.getTracks().get(i).uri;
                        int finalI = i;
                        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
                                @Override
                                public void trackLoaded(AudioTrack track) {
                                    if (musicManager.queueing) {
                                        play(event, musicManager, track);
                                        if (playlist.getTracks().get(playlist.getTracks().size() - 1).uri.equals(trackUrl)) {
                                            event.getChannel().sendMessage("Complete." + (failed[0] > 0 ? " (" + failed[0] + " track(s) could not be added, and were therefore removed from your playlist.)" : "")).queue();
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
                                        playlist.removeTrack(playlist.getTracks().get(finalI));
                                        //The following statement is required in case the last track in the queue fails.
                                        if (playlist.getTracks().get(playlist.getTracks().size() - 1).uri.equals(trackUrl)) {
                                            event.getChannel().sendMessage("Complete." + (failed[0] > 0 ? " (" + failed[0] + " track(s) could not be added, and were therefore removed from your playlist.)" : "")).queue();
                                            musicManager.queueing = false;
                                            if (failed[0] > 0) profile.save();
                                        }
                                    }
                                }
                            });
                    } else break;
                }
    }

    private void play(CommandEvent event, GuildMusicManager musicManager, AudioTrack track) {
        connectToUsersVoiceChannel(event);

        musicManager.scheduler.queue(track, event.getEvent().getAuthor());
    }

    private void playMeme(CommandEvent event, GuildMusicManager musicManager, AudioTrack track) {
        connectToUsersVoiceChannel(event);

        musicManager.memeScheduler.queue(track);
    }

    private void playError(CommandEvent event, GuildMusicManager musicManager, AudioTrack track) {
        connectToUsersVoiceChannel(event);

        musicManager.errorScheduler.play(track);
    }

    public void removeTrack(CommandEvent event, int toRemove) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event, 0);
        Guild guild = event.getGuild();
        if (!musicManager.isMusic()) {
            return;
        }

        if (getHost(guild).equals(event.getEvent().getAuthor()) || event.getEvent().getMember().hasPermission(Permission.ADMINISTRATOR)) {
                if (musicManager.scheduler.getQueueSize() < 1) {
                    event.getChannel().sendMessage("There are no tracks to remove!").queue();
                    return;
                }

                int size = musicManager.scheduler.getQueueSize();

                if (toRemove > size) {
                    event.getChannel().sendMessage("That is not a valid track.").queue();
                    return;
                }

                if (toRemove < 0) {
                    event.getChannel().sendMessage("You cannot use a number less than 1.").queue();
                    return;
                }

                event.getChannel().sendMessage("Removed `" + musicManager.scheduler.removeTrack(toRemove).title + "` from the queue.").queue();
        }
    }

    public void skipTrack(CommandEvent event) {
        skipTrack(event, false, 1, false);
    }

    public void skipToTrack(CommandEvent event, int skipTo) {
        skipTrack(event, false, skipTo, true);
    }

    public void skipTrack(CommandEvent event, int toSkip) {
        skipTrack(event, false, toSkip, false);
    }

    public void voteSkipTrack(CommandEvent event) {
        skipTrack(event, true, 1, false);
    }

    private void skipTrack(CommandEvent event, boolean vote, int toSkip, boolean skipTo) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event, 0);
        Guild guild = event.getGuild();
        if (!musicManager.isMusic()) {
            if (musicManager.getStatus() == 1) event.getChannel().sendMessage("I can't skip memes. :neutral_face:").queue();
            return;
        }

            if (getHost(guild).equals(event.getEvent().getAuthor()) || event.getEvent().getMember().hasPermission(Permission.ADMINISTRATOR) || vote) {
                if (musicManager.scheduler.repeat != 2) {
                    if (musicManager.scheduler.getQueue().size() < 1) {
                        event.getChannel().sendMessage("There are no more tracks to skip to!").queue();
                        return;
                    }

                    if (toSkip < 2 && !skipTo) musicManager.scheduler.skipTrack(vote);
                    else {
                        if (toSkip > musicManager.scheduler.getQueueSize()) {
                            if (skipTo) event.getChannel().sendMessage("That isn't an available track!").queue();
                            else event.getChannel().sendMessage("There aren't that many tracks in the queue!").queue();
                            return;
                        }

                        if (skipTo) {
                            musicManager.scheduler.skipToTrack(toSkip);
                        } else {
                            musicManager.scheduler.skipTracks(toSkip);
                        }
                    }
                } else {
                    if (musicManager.scheduler.getRepeatQueue().size() < 2) {
                        event.getChannel().sendMessage("There are no more tracks to skip to!").queue();
                        return;
                    }

                    if (skipTo) {
                        musicManager.scheduler.skipToTrack(toSkip);
                    } else {
                        musicManager.scheduler.skipTracks(toSkip);
                    }
                }
            } else {
                event.getChannel().sendMessage("Only the host and users with the `Administrator` permission can skip tracks.").queue();
            }
    }

    public void pauseTrack(CommandEvent event) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event, 0);
        Guild guild = event.getGuild();
        if (!musicManager.isMusic()) {
            return;
        }
            if (getHost(guild).equals(event.getEvent().getAuthor()) || event.getEvent().getMember().hasPermission(Permission.ADMINISTRATOR)) {
                if (!musicManager.player.isPaused()) {
                    musicManager.player.setPaused(true);
                    event.getChannel().sendMessage("Music Paused.").queue();
                } else {
                    musicManager.player.setPaused(false);
                    event.getChannel().sendMessage("Music Resumed.").queue();
                }
            } else {
                event.getChannel().sendMessage("Only the host and users with the `Administrator` permission can pause this session.").queue();
            }
    }

    public void voteSkip(CommandEvent event) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event, 0);
        Guild guild = event.getGuild();
        if (!musicManager.isMusic()) {
            return;
        }
            if (musicManager.scheduler.getQueue().size() > 0) {
                if (!musicManager.scheduler.voteSkippers.contains(event.getEvent().getAuthor())) {
                    ++musicManager.scheduler.voteSkip;
                    musicManager.scheduler.voteSkippers.add(event.getEvent().getAuthor());
                    int users = guild.getAudioManager().getConnectedChannel().getMembers().size() - 1;
                    if (musicManager.scheduler.voteSkip == (users < 4 ? Math.ceil(users * 0.6) : Math.round(users * 0.6))) {
                        voteSkipTrack(event);
                    } else {
                        event.getChannel().sendMessage("Your vote has been added. (" + musicManager.scheduler.voteSkip + "/" + Math.round((event.getGuild().getAudioManager().getConnectedChannel().getMembers().size() - 1) * 0.6) + ")").queue();
                    }
                } else {
                    event.getChannel().sendMessage("You've already voted to skip.").queue();
                }
            } else {
                event.getChannel().sendMessage("There are no more tracks to skip to!").queue();
            }
    }

    public void repeat(CommandEvent event) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event, 0);
        if (!musicManager.isMusic()) {
            return;
        }
            musicManager.scheduler.toggleRepeat();
            if (musicManager.scheduler.repeat == 0) event.getChannel().sendMessage("Repeat is now set to: **OFF**.").queue();
            else if (musicManager.scheduler.repeat == 1) event.getChannel().sendMessage("Repeat is now set to **SINGLE**.").queue();
            else if (musicManager.scheduler.repeat == 2) event.getChannel().sendMessage("Repeat is now set to **MULTI**.").queue();
    }

    private void connectToUsersVoiceChannel(CommandEvent event) {
        AudioManager audioManager = event.getGuild().getAudioManager();
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            Optional<VoiceChannel> voiceChannel = event.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(event.getEvent().getMember())).findFirst();
            if (!voiceChannel.isPresent()) {
                event.getTextChannel().sendMessage(KekBot.respond(Action.GET_IN_VOICE_CHANNEL)).queue();
            } else {
                audioManager.openAudioConnection(voiceChannel.get());
                announceStart(event, voiceChannel.get());
            }
        } else {
            announceStart(event, audioManager.getConnectedChannel());
        }
    }

    private void announceStart(CommandEvent event, VoiceChannel channel) {
        GuildMusicManager musicManager = musicManagers.get(Long.parseLong(event.getGuild().getId()));
        if (musicManager.isMusic() && !musicManager.scheduler.hasStarted()) {
            musicManagers.get(Long.parseLong(event.getGuild().getId())).scheduler.setStarted();
            event.getChannel().sendMessage(event.getEvent().getAuthor().getAsMention() + " is now hosting a music session in: `" + channel.getName() + "`, use " + event.getPrefix() +  "music to get the list of all music commands." + CustomEmote.dance()).queue();
            musicManagers.get(Long.parseLong(event.getGuild().getId())).scheduler.currentPlayer = event.getEvent().getAuthor();
        }
    }

    public void announceToMusicSession(Guild guild, String message) {
        musicManagers.get(Long.parseLong(guild.getId())).channel.sendMessage(message).queue();
    }

    public boolean isMusic(Guild guild) {
        return musicManagers.get(Long.parseLong(guild.getId())).isMusic();
    }

    public void getPlaylist(CommandEvent event) {
        long guildId = Long.parseLong(event.getGuild().getId());
        if (musicManagers.containsKey(guildId)) {
            GuildMusicManager musicManager = musicManagers.get(guildId);
            if (!musicManager.isMusic()) {
                return;
            }
                if (musicManager.scheduler.repeat != 2) {
                    if (musicManager.scheduler.getQueue().size() > 0) {
                        List<String> tracks = new ArrayList<>();
                        long totalLength = 0;
                        List<Pair<AudioTrack, User>> queue = new ArrayList<>(musicManager.scheduler.getQueue());
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
                        pb.setColor(event.getGuild().getSelfMember().getColor() == null?Color.RED:event.getGuild().getSelfMember().getColor());
                        pb.setTimeout(1, TimeUnit.MINUTES);
                        pb.showPageNumbers(true);
                        pb.waitOnSinglePage(true);
                        pb.setUsers(event.getAuthor());
                        pb.build().display(event.getChannel());
                    } else event.getChannel().sendMessage("There is nothing in the playlist!").queue();
                } else {
                    List<String> tracks = new ArrayList<>();
                    List<Pair<AudioTrack, User>> queue = new ArrayList<>(musicManager.scheduler.getQueue());
                    for (int i = 0; i < queue.size(); i++) {
                        AudioTrack track = queue.get(i).getKey();
                        User user = queue.get(i).getValue();
                        tracks.add(i + 1 + ".) " + track.getInfo().title + " - **(" + Utils.convertMillisToHMmSs(track.getDuration()) + ")** - " + "Queued by: " + user.getName() + (i == musicManager.scheduler.getRepeat() ? " **(Current Track)**" : ""));
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
                    pb.setColor(event.getGuild().getSelfMember().getColor() == null?Color.RED:event.getGuild().getSelfMember().getColor());
                    pb.setTimeout(1, TimeUnit.MINUTES);
                    pb.showPageNumbers(true);
                    pb.waitOnSinglePage(true);
                    pb.setUsers(event.getAuthor());
                    pb.build().display(event.getChannel());
                }
        } else event.getChannel().sendMessage("There is no music playing!").queue();
    }

    public void getCurrentSong(TextChannel channel) {
        long guildId = Long.parseLong(channel.getGuild().getId());
        if (musicManagers.containsKey(guildId)) {
            if (musicManagers.get(guildId).isMusic()) {
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
        closeConnection(guild, "This music session has now ended.");
    }

    public void closeConnection(Guild guild, String reason) {
        long guildId = Long.parseLong(guild.getId());
        if (!this.musicManagers.get(guildId).queueing) {
            if (this.musicManagers.get(guildId).isMusic()) announceToMusicSession(guild, reason);
            this.musicManagers.remove(guildId);
            guild.getAudioManager().closeAudioConnection();
        } else {
            musicManagers.get(guildId).queueing = false;
            if (this.musicManagers.get(guildId).isMusic()) announceToMusicSession(guild, reason);
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

    public void setVolume(CommandEvent event, int volume) {
        long guildId = Long.parseLong(event.getGuild().getId());
        if (musicManagers.containsKey(guildId)) {
            if (volume <= 100 && volume >= 0) {
                musicManagers.get(guildId).player.setVolume(volume);
                event.getChannel().sendMessage("Volume set to " + volume).queue();
            } else event.getChannel().sendMessage("Specified volume must be between 100 and 0!").queue();
        }
    }

    public void shuffle(CommandEvent event) {
        long guildId = Long.parseLong(event.getGuild().getId());
        if (musicManagers.containsKey(guildId)) {
            if (!musicManagers.get(guildId).isMusic()) return;
            musicManagers.get(guildId).scheduler.shuffle();
        }
        event.getChannel().sendMessage("Shuffled! \uD83D\uDD04").queue();
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
            Questionnaire.newQuestionnaire(results)
                        .addChoiceQuestion("Are you sure you want to add all " + audioPlaylist.getTracks().size() + " tracks to your playlist?", "Yes", "No", "Y", "N")
                        .withoutRepeats()
                        .execute(results1 -> {
                            if (results1.getAnswerAsType(0, boolean.class)) {
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

    public void shutdown() {
        shutdown("shut down");
    }

    public void shutdown(String reason) {
        Iterator<Map.Entry<Long, GuildMusicManager>> itr = musicManagers.entrySet().iterator();

        while(itr.hasNext())
        {
            Map.Entry<Long, GuildMusicManager> entry = itr.next();
            Guild guild = KekBot.jda.getGuildById(entry.getKey());
            closeConnection(guild, "This music session was ended due to KekBot shutting down with the reason: `" + reason + "`");
        }
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (!event.getGuild().getAudioManager().isConnected() || !isMusic(event.getGuild()) || !event.getChannelLeft().equals(event.getGuild().getAudioManager().getConnectedChannel())) return;
        List<User> users = event.getChannelLeft().getMembers().stream().map(Member::getUser).filter(user -> !user.isBot()).collect(Collectors.toList());
        GuildMusicManager musicManager = musicManagers.get(Long.parseLong(event.getGuild().getId()));
        if (users.size() > 0) {
            if (getHost(event.getGuild()).equals(event.getMember().getUser())) {
                if (!musicManager.waiting) musicManager.channel.sendMessage("Waiting 10 seconds for host to return...").queue(m -> {
                    musicManager.waiting = true;
                    KekBot.waiter.waitForEvent(GenericGuildVoiceEvent.class, event1 -> {
                                VoiceChannel channelJoined = null;
                                Member member = event1.getMember();
                                boolean botMoved = false;
                                if (event1 instanceof GuildVoiceJoinEvent) {
                                    channelJoined = ((GuildVoiceJoinEvent) event1).getChannelJoined();
                                }
                                if (event1 instanceof GuildVoiceMoveEvent){
                                    if (event1.getMember().equals(event1.getGuild().getSelfMember())) {
                                        botMoved = true;
                                    }
                                    channelJoined = ((GuildVoiceMoveEvent) event1).getChannelJoined();
                                    if (((GuildVoiceMoveEvent) event1).getChannelLeft().equals(event.getChannelLeft()) && !event1.getMember().equals(event1.getGuild().getSelfMember())) return ((GuildVoiceMoveEvent) event1).getChannelLeft().getMembers().stream().map(Member::getUser).filter(user -> !user.isBot()).toArray().length == 0;
                                }
                                if (event1 instanceof GuildVoiceLeaveEvent) {
                                    if (event1.getMember().equals(event.getGuild().getSelfMember())) return true;
                                    if (((GuildVoiceLeaveEvent) event1).getChannelLeft().equals(event.getChannelLeft())) return ((GuildVoiceLeaveEvent) event1).getChannelLeft().getMembers().stream().map(Member::getUser).filter(user -> !user.isBot()).toArray().length == 0;
                                }

                                if (botMoved) return channelJoined.getMembers().size() > 0;
                                else return member.equals(event.getMember()) && channelJoined.equals(event.getChannelLeft());
                            }, success -> {
                                musicManager.stopWaiting();
                                if (success instanceof GuildVoiceLeaveEvent || (success instanceof GuildVoiceMoveEvent && ((GuildVoiceMoveEvent) success).getChannelLeft().equals(event.getChannelLeft()) && !success.getMember().equals(success.getGuild().getSelfMember()))) {
                                    m.delete().queue();
                                    if (success.getMember().equals(success.getGuild().getSelfMember())) {
                                        closeConnection(success.getGuild());
                                    }
                                    musicManager.stopWaiting();
                                    return;
                                }

                                if (!success.getVoiceState().getChannel().equals(event.getChannelLeft())) {
                                    reactToWaitingMove(event, users, m, success);
                                } else m.delete().queue();
                                musicManager.stopWaiting();
                            },
                            10, TimeUnit.SECONDS, () -> {
                                List<User> potentialHosts = KekBot.jda.getGuildById(event.getGuild().getId()).getAudioManager().getConnectedChannel().getMembers().stream().map(Member::getUser).filter(user -> !user.isBot()).collect(Collectors.toList());
                                Random random = new Random();
                                int user = random.nextInt(potentialHosts.size());
                                User newHost = users.get(user);
                                changeHost(event.getGuild(), newHost);
                                m.editMessage(newHost.getName() + " is now the host of this music session.").queue();
                                musicManager.stopWaiting();
                            });
                });
            }
        } else {
            if (!musicManager.waiting) musicManager.channel.sendMessage("Waiting 10 seconds for *someone* to return...").queue(m -> {
                if (!musicManager.player.isPaused()) musicManager.player.setPaused(true);
                musicManager.waiting = true;
                KekBot.waiter.waitForEvent(GenericGuildVoiceEvent.class, event1 -> {

                    VoiceChannel channelJoined = null;
                    boolean botMoved = false;
                    if (event1 instanceof GuildVoiceJoinEvent) {
                        channelJoined = ((GuildVoiceJoinEvent) event1).getChannelJoined();
                    }
                    if (event1 instanceof GuildVoiceMoveEvent){
                        if (event1.getMember().equals(event1.getGuild().getSelfMember())) {
                            botMoved = true;
                        }
                        channelJoined = ((GuildVoiceMoveEvent) event1).getChannelJoined();
                        if (((GuildVoiceMoveEvent) event1).getChannelLeft().equals(event.getChannelLeft()) && !event1.getMember().equals(event1.getGuild().getSelfMember())) return ((GuildVoiceMoveEvent) event1).getChannelLeft().getMembers().stream().map(Member::getUser).filter(user -> !user.isBot()).toArray().length == 0;
                    }
                    if (event1 instanceof GuildVoiceLeaveEvent) {
                        if (event1.getMember().equals(event.getGuild().getSelfMember())) return true;
                        if (((GuildVoiceLeaveEvent) event1).getChannelLeft().equals(event.getChannelLeft())) return ((GuildVoiceLeaveEvent) event1).getChannelLeft().getMembers().stream().map(Member::getUser).filter(user -> !user.isBot()).toArray().length == 0;
                    }

                    if (botMoved) return channelJoined.getMembers().size() > 0;
                    else return channelJoined.equals(event.getChannelLeft());


                        }, event1 -> {


                    if (leftWaitingVoice(musicManager, m, event1)) return;

                    if (event1.getMember().equals(event1.getGuild().getSelfMember()) && event1 instanceof GuildVoiceMoveEvent) {
                        reactToWaitingMove(event, users, m, event1);
                        if (musicManager.player.isPaused()) musicManager.player.setPaused(false);
                        musicManager.stopWaiting();
                        return;
                    }

                    Member member = null;
                    if (event1 instanceof GuildVoiceJoinEvent) member = event1.getMember();
                    if (event1 instanceof GuildVoiceMoveEvent) member = event1.getMember();

                    if (isWaitingUserHost(musicManager, m, member, event.getGuild())) return;
                    m.editMessage("Whew! Someone joined! Alright then, " + member.getUser().getName() + " is the new host!").queue();
                    if (musicManager.player.isPaused()) musicManager.player.setPaused(false);
                    changeHost(event.getGuild(), member.getUser());
                    musicManager.stopWaiting();
                }, 10, TimeUnit.SECONDS, () -> {
                    m.editMessage(KekBot.respond(Action.MUSIC_EMPTY_CHANNEL)).queue();
                    musicManager.stopWaiting();
                    closeConnection(event.getGuild());
                });
            });
        }
    }

    private boolean leftWaitingVoice(GuildMusicManager musicManager, Message m, GenericGuildVoiceEvent event1) {
        if (event1 instanceof GuildVoiceLeaveEvent) {
            m.delete().queue();
            closeConnection(event1.getGuild());
            musicManager.stopWaiting();
            return true;
        }
        return false;
    }

    private boolean isWaitingUserHost(GuildMusicManager musicManager, Message m, Member member, Guild guild) {
        if (member.getUser().equals(getHost(guild))) {
            m.editMessage("You're back! I knew you wouldn't leave me!").queue();
            if (musicManager.player.isPaused()) musicManager.player.setPaused(false);
            musicManager.stopWaiting();
            return true;
        }
        return false;
    }

    private void reactToWaitingMove(GuildVoiceLeaveEvent event, List<User> users, Message m, GenericGuildVoiceEvent success) {
        if (success.getVoiceState().getChannel().getMembers().contains(event.getMember()))
            m.editMessage("Oh, I guess we're here in `" + success.getVoiceState().getChannel().getName() + "` now... And hey, the host is here too! Looks like we moved the party!").queue();
        else {
            List<User> potentialHosts = KekBot.jda.getGuildById(event.getGuild().getId()).getAudioManager().getConnectedChannel().getMembers().stream().map(Member::getUser).filter(user -> !user.isBot()).collect(Collectors.toList());
            Random random = new Random();
            int user = random.nextInt(potentialHosts.size());
            User newHost = users.get(user);
            changeHost(event.getGuild(), newHost);
            m.editMessage("Oh, I guess we're here in `" + success.getVoiceState().getChannel().getName() + "` now... And I don't see the host anywhere... Looks like we're moving the party! " + newHost.getName() + " is now the host of this music session.").queue();
        }
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (!event.getMember().equals(event.getGuild().getSelfMember())) {
            onGuildVoiceLeave(new GuildVoiceLeaveEvent(event.getJDA(), event.getResponseNumber(), event.getMember(), event.getChannelLeft()));
            return;
        }
        GuildMusicManager musicManager = musicManagers.get(Long.parseLong(event.getGuild().getId()));
        if (musicManager.waiting) return;


        List<User> users = event.getChannelJoined().getMembers().stream().map(Member::getUser).filter(user -> !user.isBot()).collect(Collectors.toList());

        if (users.size() > 0) {
            Random random = new Random();
            int user = random.nextInt(users.size());
            User newHost = users.get(user);
            changeHost(event.getGuild(), newHost);
            announceToMusicSession(event.getGuild(), "Oh, I guess we're here in `" + event.getChannelJoined().getName() + "` now... And I don't see the host anywhere... Looks like we're moving the party! " + newHost.getName() + " is now the host of this music session.");
        } else {
            musicManager.channel.sendMessage("Waiting 10 seconds for *someone* to join...").queue(m -> {
                if (!musicManager.player.isPaused()) musicManager.player.setPaused(true);
                musicManager.waiting = true;
                KekBot.waiter.waitForEvent(GenericGuildVoiceEvent.class, event1 -> {
                    VoiceChannel channelJoined = null;
                    boolean botMoved = false;
                    if (event1 instanceof GuildVoiceJoinEvent) {
                        channelJoined = ((GuildVoiceJoinEvent) event1).getChannelJoined();
                    }
                    if (event1 instanceof GuildVoiceMoveEvent) {
                        if (event1.getMember().equals(event1.getGuild().getSelfMember())) {
                            botMoved = true;
                        }
                        channelJoined = ((GuildVoiceMoveEvent) event1).getChannelJoined();
                    }
                    if (event1 instanceof GuildVoiceLeaveEvent) {
                        if (event1.getMember().equals(event.getGuild().getSelfMember())) return true;
                    }

                    if (botMoved) return channelJoined.getMembers().size() > 0;
                    else return channelJoined.equals(KekBot.jda.getGuildById(event1.getGuild().getId()).getAudioManager().getConnectedChannel());
                }, event1 -> {
                    if (leftWaitingVoice(musicManager, m, event1)) return;

                    if (event1.getMember().equals(event1.getGuild().getSelfMember()) && event1 instanceof GuildVoiceMoveEvent) {
                        if (event1.getVoiceState().getChannel().getMembers().contains(event.getMember()))
                            m.editMessage("Oh, I guess we're " + (((GuildVoiceMoveEvent) event1).getChannelJoined().equals(event.getChannelLeft()) ? "back" : "") + " here in `" + event1.getVoiceState().getChannel().getName() + "` now... And hey, the host is here too! Looks like we moved the party!").queue();
                        else {
                            Random random = new Random();
                            int user = random.nextInt(users.size());
                            User newHost = users.get(user);
                            changeHost(event.getGuild(), newHost);
                            m.editMessage("Oh, I guess we're " + (((GuildVoiceMoveEvent) event1).getChannelJoined().equals(event.getChannelLeft()) ? "back" : "") + " here in `" + event1.getVoiceState().getChannel().getName() + "` now... And I don't see the host anywhere... Looks like we're moving the party! " + newHost.getName() + " is now the host of this music session.").queue();
                        }
                        if (musicManager.player.isPaused()) musicManager.player.setPaused(false);
                        musicManager.stopWaiting();
                        return;
                    }

                    Member member = null;
                    if (event1 instanceof GuildVoiceJoinEvent) member = event1.getMember();
                    if (event1 instanceof GuildVoiceMoveEvent) member = event1.getMember();

                    if (isWaitingUserHost(musicManager, m, member, event.getGuild())) return;
                    if (!member.getUser().isBot()) m.editMessage("Whew! Someone joined! Alright then, " + member.getUser().getName() + " is the new host!").queue();
                    if (musicManager.player.isPaused()) musicManager.player.setPaused(false);
                    changeHost(event.getGuild(), member.getUser());
                    musicManager.stopWaiting();
                }, 60, TimeUnit.SECONDS, () -> {
                    m.editMessage(KekBot.respond(Action.MUSIC_EMPTY_CHANNEL)).queue();
                    musicManager.stopWaiting();
                    closeConnection(event.getGuild());
                });
            });
        }
    }

    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        if (containsConnection(event.getGuild()) && !event.getGuild().getAudioManager().isConnected()) {
            if (musicManagers.get(Long.parseLong(event.getGuild().getId())).waiting) KekBot.waiter.onEvent(new GuildVoiceLeaveEvent(event.getJDA(), event.getResponseNumber(), event.getGuild().getSelfMember(), event.getChannel()));
            else closeConnection(event.getGuild());
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        if (KekBot.player.containsConnection(event.getGuild())) {
            event.getGuild().getAudioManager().closeAudioConnection();
            killConnection(event.getGuild());
        }
    }
}
