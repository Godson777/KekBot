package com.godson.kekbot.music;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.LocaleUtils;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.CommandEvent;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.jagrosh.jdautilities.menu.Paginator;
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
            event.getChannel().sendMessage(event.getString("music.ongoingmusic")).queue();
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
            event.getChannel().sendMessage(event.getString("music.ongoingmeme")).queue();
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

                    event.getChannel().sendMessage(event.getString("music.queue.urlplaylist", event.getAuthor().getName(), (playlist.getTracks().size() - failed))
                            + (failed > 0 ? event.getString("music.queue.urlplaylist.fail", failed, event.getPluralString(failed, "amount.tracks")) : "")).queue();
                }

                @Override
                public void noMatches() {
                    event.getChannel().sendMessage(event.getString("music.queue.invalidurl", "`" + trackUrl + "`")).queue();
                    if (musicManager.player.getPlayingTrack() == null) killConnection(event.getGuild());
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    event.getChannel().sendMessage(event.getString("music.queue.loadfailed", exception.getMessage())).queue();
                    if (musicManager.player.getPlayingTrack() == null) killConnection(event.getGuild());
                }
            });
    }

    public void loadAndSearchYT(final CommandEvent event, final String search) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event, 0);
        if (musicManager.getStatus() == 1) {
            event.getChannel().sendMessage(event.getString("music.ongoingmeme")).queue();
            return;
        }
            playerManager.loadItemOrdered(musicManager, search, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    queueTrack(event, musicManager, track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    OrderedMenu.Builder builder = new OrderedMenu.Builder();
                    builder.useNumbers();
                    builder.setEventWaiter(KekBot.waiter);
                    builder.setUsers(event.getAuthor());
                    builder.useCancelButton(true);
                    int numOfResults = playlist.getTracks().size();
                    builder.addChoices(playlist.getTracks().subList(0, 10 < numOfResults ? 10 : numOfResults).stream().map(track -> "`" + track.getInfo().title + "`").toArray(String[]::new));
                    builder.setSelection((m, i) -> {
                       m.delete().queue();
                       trackLoaded(playlist.getTracks().get(i-1));
                    });
                    builder.setColor(Color.RED);
                    builder.setText(event.getString("music.queue.searchyt.choose"));
                    builder.allowTextInput(false);
                    builder.build().display(event.getChannel());
                }

                @Override
                public void noMatches() {
                    event.getChannel().sendMessage(event.getString("music.queue.searchyt.nomatches", "`" + search.substring(9) + "`")).queue();
                    if (musicManager.player.getPlayingTrack() == null) killConnection(event.getGuild());
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    event.getChannel().sendMessage(event.getString("music.queue.loadfailed", exception.getMessage())).queue();
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
                timeBefore = " " + event.getString("music.queue.success.timebefore", Utils.convertMillisToTime(
                        (musicManager.player.getPlayingTrack().getDuration() - musicManager.player.getPlayingTrack().getPosition() + (totalLength[0] - track.getDuration())), event.getLocale()),
                        musicManager.scheduler.getQueue().size());
            } else {
                long totalLength = 0;
                List<Pair<AudioTrack, User>> playlist = musicManager.scheduler.getRepeatQueue();
                for (int i = musicManager.scheduler.getRepeat() + 1; i < playlist.size(); i++) {
                    totalLength += playlist.get(i).getKey().getDuration();
                }
                timeBefore = " " + event.getString("music.queue.success.timebefore",
                        Utils.convertMillisToTime((musicManager.player.getPlayingTrack().getDuration() - musicManager.player.getPlayingTrack().getPosition() + (totalLength - track.getDuration())), event.getLocale()),
                        playlist.size());
            }
            event.getChannel().sendMessage(event.getString("music.queue.success", "`" + track.getInfo().title + "`") + timeBefore).queue();
        }
    }

    public void loadAndPlay(final CommandEvent event, final Playlist playlist, final Profile profile) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event, 0);
        if (musicManager.getStatus() == 1) {
            event.getChannel().sendMessage(event.getString("music.ongoingmeme")).queue();
            return;
        }
        event.getChannel().sendMessage(event.getString("music.queue.userplaylist.attempting", playlist.getName())).queue();
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
                                            event.getChannel().sendMessage(event.getString("music.queue.userplaylist.success")
                                                    + (failed[0] > 0 ? event.getString("music.queue.userplaylist.failed", failed[0], event.getPluralString(failed[0], "amount.tracks")) : "")).queue();
                                            musicManager.queueing = false;
                                            if (failed[0] > 0) profile.save();
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
                                            event.getChannel().sendMessage(event.getString("music.queue.userplaylist.success")
                                                    + (failed[0] > 0 ? event.getString("music.queue.userplaylist.failed", failed[0], event.getPluralString(failed[0], "amount.tracks")) : "")).queue();
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
                    event.getChannel().sendMessage(event.getString("music.remove.empty")).queue();
                    return;
                }

                int size = musicManager.scheduler.getQueueSize();

                if (toRemove > size) {
                    event.getChannel().sendMessage(event.getString("music.remove.invalidtrack")).queue();
                    return;
                }

                if (toRemove < 0) {
                    event.getChannel().sendMessage(event.getString("music.remove.invalidnum")).queue();
                    return;
                }

                event.getChannel().sendMessage(event.getString("music.remove.success", "`" + musicManager.scheduler.removeTrack(toRemove).title + "`")).queue();
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
            if (musicManager.getStatus() == 1) event.getChannel().sendMessage(event.getString("music.skip.meme")).queue();
            return;
        }

            if (getHost(guild).equals(event.getEvent().getAuthor()) || event.getEvent().getMember().hasPermission(Permission.ADMINISTRATOR) || vote) {
                if (musicManager.scheduler.repeat != 2) {
                    if (musicManager.scheduler.getQueue().size() < 1) {
                        event.getChannel().sendMessage(event.getString("music.skip.notracks")).queue();
                        return;
                    }

                    if (toSkip < 2 && !skipTo) musicManager.scheduler.skipTrack(vote);
                    else {
                        if (toSkip > musicManager.scheduler.getQueueSize()) {
                            if (skipTo) event.getChannel().sendMessage(event.getString("music.skipto.invalid")).queue();
                            else event.getChannel().sendMessage(event.getString("music.skipmulti.invalid")).queue();
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
                        event.getChannel().sendMessage(event.getString("music.skip.notracks")).queue();
                        return;
                    }

                    if (skipTo) {
                        musicManager.scheduler.skipToTrack(toSkip);
                    } else {
                        musicManager.scheduler.skipTracks(toSkip);
                    }
                }
            } else {
                event.getChannel().sendMessage(event.getString("music.nothost", "`Administrator`")).queue();
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
                    event.getChannel().sendMessage(event.getString("music.paused")).queue();
                } else {
                    musicManager.player.setPaused(false);
                    event.getChannel().sendMessage(event.getString("music.resumed")).queue();
                }
            } else {
                event.getChannel().sendMessage(event.getString("music.nothost", "`Administrator`")).queue();
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
                        event.getChannel().sendMessage(event.getString("music.voteskip.success", musicManager.scheduler.voteSkip, Math.round((event.getGuild().getAudioManager().getConnectedChannel().getMembers().size() - 1) * 0.6))).queue();
                    }
                } else {
                    event.getChannel().sendMessage(event.getString("music.voteskip.error")).queue();
                }
            } else {
                event.getChannel().sendMessage(event.getString("music.skip.notracks")).queue();
            }
    }

    public void repeat(CommandEvent event) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event, 0);
        if (!musicManager.isMusic()) {
            return;
        }
        musicManager.scheduler.toggleRepeat();
        String status = null;

        if (musicManager.scheduler.repeat == 0) status = "**" + event.getString("music.repeat.off") + "**";
        else if (musicManager.scheduler.repeat == 1) status = "**" + event.getString("music.repeat.single") + "**";
        else if (musicManager.scheduler.repeat == 2) status = "**" + event.getString("music.repeat.multi") + "**";
        event.getChannel().sendMessage(event.getString("music.repeat.success", status)).queue();
    }

    private void connectToUsersVoiceChannel(CommandEvent event) {
        AudioManager audioManager = event.getGuild().getAudioManager();
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            Optional<VoiceChannel> voiceChannel = event.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(event.getEvent().getMember())).findFirst();
            if (!voiceChannel.isPresent()) {
                event.getTextChannel().sendMessage(KekBot.respond(Action.GET_IN_VOICE_CHANNEL, event.getLocale())).queue();
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
            event.getChannel().sendMessage(event.getString("music.newsession", event.getAuthor().getAsMention(), "`" + channel.getName() + "`", event.getPrefix() +  "help music") + CustomEmote.dance()).queue();
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
                            tracks.add(i + 1 + ".) " + track.getInfo().title + " - **(" + Utils.convertMillisToHMmSs(track.getDuration()) + ")** - " + event.getString("music.queuedby", user.getName()));
                            totalLength += track.getDuration();
                        }
                        Paginator.Builder pb = new Paginator.Builder();
                        pb.addItems(tracks.toArray(new String[tracks.size()]));
                        pb.setText("**" + event.getString("music.list.length",  (musicManager.scheduler.repeat == 1 ? event.getString("music.list.length.infinity") : Utils.convertMillisToTime(totalLength, event.getLocale()))) + "**");
                        pb.setEventWaiter(KekBot.waiter);
                        pb.setItemsPerPage(15);
                        pb.setColor(event.getGuild().getSelfMember().getColor() == null?Color.RED:event.getGuild().getSelfMember().getColor());
                        pb.setTimeout(1, TimeUnit.MINUTES);
                        pb.showPageNumbers(true);
                        pb.waitOnSinglePage(true);
                        pb.setUsers(event.getAuthor());
                        pb.build().display(event.getChannel());
                    } else event.getChannel().sendMessage(event.getString("music.list.empty")).queue();
                } else {
                    List<String> tracks = new ArrayList<>();
                    List<Pair<AudioTrack, User>> queue = musicManager.scheduler.getRepeatQueue();
                    for (int i = 0; i < queue.size(); i++) {
                        AudioTrack track = queue.get(i).getKey();
                        User user = queue.get(i).getValue();
                        tracks.add(i + 1 + ".) " + track.getInfo().title + " - **(" + Utils.convertMillisToHMmSs(track.getDuration()) + ")** - " +
                                event.getString("music.queuedby", user.getName()) + (i == musicManager.scheduler.getRepeat() ? " **(" + event.getString("music.list.current") + ")**" : ""));
                    }
                    Paginator.Builder pb = new Paginator.Builder();
                    pb.addItems(tracks.toArray(new String[tracks.size()]));
                    pb.setText("\n**" + event.getString("music.list.length", event.getString("music.list.infinity")) + "**");
                    pb.setEventWaiter(KekBot.waiter);
                    pb.setItemsPerPage(15);
                    pb.setColor(event.getGuild().getSelfMember().getColor() == null?Color.RED:event.getGuild().getSelfMember().getColor());
                    pb.setTimeout(1, TimeUnit.MINUTES);
                    pb.showPageNumbers(true);
                    pb.waitOnSinglePage(true);
                    pb.setUsers(event.getAuthor());
                    pb.build().display(event.getChannel());
                }
        } else event.getChannel().sendMessage(event.getString("music.nomusic")).queue();
    }

    public void getCurrentSong(CommandEvent event) {
        TextChannel channel = event.getTextChannel();
        long guildId = Long.parseLong(event.getGuild().getId());
        if (musicManagers.containsKey(guildId)) {
            if (musicManagers.get(guildId).isMusic()) {
                AudioTrack track = musicManagers.get(guildId).player.getPlayingTrack();
                channel.sendMessage(event.getString("music.song.current", "`" + track.getInfo().title + "` [" + Utils.songTimestamp(track.getPosition(), track.getDuration()) + "]")
                        + "\n" + event.getString("music.song.url", "`" + musicManagers.get(guildId).player.getPlayingTrack().getInfo().uri + "`")
                        + "\n" + event.getString("music.queuedby", musicManagers.get(guildId).scheduler.currentPlayer.getName())
                        + "\n" + event.getString("music.volume", musicManagers.get(guildId).player.getVolume())).queue();
            } else {
                channel.sendMessage(event.getString("music.nomusic")).queue();
            }
        }
    }

    public void closeConnection(Guild guild) {
        closeConnection(guild, LocaleUtils.getString("music.sessionended", KekBot.getGuildLocale(guild)));
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
                event.getChannel().sendMessage(event.getString("music.volume.success", volume)).queue();
            } else event.getChannel().sendMessage(event.getString("music.volume.error")).queue();
        }
    }

    public void shuffle(CommandEvent event) {
        long guildId = Long.parseLong(event.getGuild().getId());
        if (musicManagers.containsKey(guildId)) {
            if (!musicManagers.get(guildId).isMusic()) return;
            musicManagers.get(guildId).scheduler.shuffle();
        }
        event.getChannel().sendMessage(event.getString("music.shuffled")).queue();
    }

    public void addToPlaylist(Questionnaire.Results results, String trackUrl, Playlist playlist) {
        playerManager.loadItemOrdered(playlist, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (playlist.getTracks().stream().noneMatch(audioTrackInfo -> audioTrackInfo.uri.equals(track.getInfo().uri))) {
                    playlist.addTrack(track);
                    results.getChannel().sendMessage(LocaleUtils.getString("music.userplaylist.added", KekBot.getGuildLocale(results.getGuild()), "`" + track.getInfo().title + "`")).queue();
                    results.reExecuteWithoutMessage();
                } else {
                    results.getChannel().sendMessage(LocaleUtils.getString("music.userplaylist.existing", KekBot.getGuildLocale(results.getGuild()))).queue();
                    results.reExecuteWithoutMessage();
                }

            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
            Questionnaire.newQuestionnaire(results)
                        .addYesNoQuestion(LocaleUtils.getString("music.userplaylist.mass", KekBot.getGuildLocale(results.getGuild()), audioPlaylist.getTracks().size()))
                        .withoutRepeats()
                        .execute(results1 -> {
                            if (results1.getAnswerAsType(0, boolean.class)) {
                                for (AudioTrack track : audioPlaylist.getTracks()) {
                                    if (playlist.getTracks().stream().noneMatch(audioTrackInfo -> audioTrackInfo.uri.equals(track.getInfo().uri))) {
                                        playlist.addTrack(track);
                                    }
                                }
                                results1.getChannel().sendMessage(LocaleUtils.getString("music.userplaylist.mass.added", KekBot.getGuildLocale(results.getGuild()))).queue();
                                results.reExecuteWithoutMessage();
                            } else {
                                results1.getChannel().sendMessage(LocaleUtils.getString("music.userplaylist.mass.cancelled", KekBot.getGuildLocale(results.getGuild()))).queue();
                                results.reExecuteWithoutMessage();
                            }
                        });
            }

            @Override
            public void noMatches() {
                results.getChannel().sendMessage(LocaleUtils.getString("music.queue.invalidurl", KekBot.getGuildLocale(results.getGuild()), "`" + trackUrl + "`")).queue();
                results.reExecuteWithoutMessage();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                results.getChannel().sendMessage(LocaleUtils.getString("music.userplaylist.error", KekBot.getGuildLocale(results.getGuild()), exception.getMessage())).queue();
                results.reExecuteWithoutMessage();
            }
        });
    }

    public void shutdown() {
        shutdown("shut down");
    }

    public void shutdown(String reason) {
        Set<Long> sessions = new HashSet<>(musicManagers.keySet());
        sessions.forEach(id -> closeConnection(KekBot.jda.getGuildById(id), LocaleUtils.getString("music.sessionshutdown", KekBot.getGuildLocale(KekBot.jda.getGuildById(id)), "`" + reason + "`")));
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (!event.getGuild().getAudioManager().isConnected() || !isMusic(event.getGuild()) || !event.getChannelLeft().equals(event.getGuild().getAudioManager().getConnectedChannel())) return;
        List<User> users = event.getChannelLeft().getMembers().stream().map(Member::getUser).filter(user -> !user.isBot()).collect(Collectors.toList());
        GuildMusicManager musicManager = musicManagers.get(Long.parseLong(event.getGuild().getId()));
        if (users.size() > 0) {
            if (getHost(event.getGuild()).equals(event.getMember().getUser())) {
                if (!musicManager.waiting) musicManager.channel.sendMessage(LocaleUtils.getString("music.awaithost", KekBot.getGuildLocale(event.getGuild()))).queue(m -> {
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
                                m.editMessage(LocaleUtils.getString("music.newhost", KekBot.getGuildLocale(event.getGuild()), newHost.getName())).queue();
                                musicManager.stopWaiting();
                            });
                });
            }
        } else {
            if (!musicManager.waiting) musicManager.channel.sendMessage(LocaleUtils.getString("music.awaituser", KekBot.getGuildLocale(event.getGuild()))).queue(m -> {
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
                    m.editMessage(LocaleUtils.getString("music.awaituser.success", KekBot.getGuildLocale(event.getGuild()), member.getUser().getName())).queue();
                    if (musicManager.player.isPaused()) musicManager.player.setPaused(false);
                    changeHost(event.getGuild(), member.getUser());
                    musicManager.stopWaiting();
                }, 10, TimeUnit.SECONDS, () -> {
                    m.editMessage(KekBot.respond(Action.MUSIC_EMPTY_CHANNEL, KekBot.getGuildLocale(event.getGuild()))).queue();
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
            m.editMessage(LocaleUtils.getString("music.awaituser.host", KekBot.getGuildLocale(guild))).queue();
            if (musicManager.player.isPaused()) musicManager.player.setPaused(false);
            musicManager.stopWaiting();
            return true;
        }
        return false;
    }

    private void reactToWaitingMove(GuildVoiceLeaveEvent event, List<User> users, Message m, GenericGuildVoiceEvent success) {
        String message = LocaleUtils.getString("music.moved", KekBot.getGuildLocale(event.getGuild()), "`" + success.getVoiceState().getChannel().getName() + "`");
        if (success.getVoiceState().getChannel().getMembers().contains(event.getMember()))
            m.editMessage(message + " " + LocaleUtils.getString("music.moved.hostfound", KekBot.getGuildLocale(event.getGuild()))).queue();
        else {
            List<User> potentialHosts = KekBot.jda.getGuildById(event.getGuild().getId()).getAudioManager().getConnectedChannel().getMembers().stream().map(Member::getUser).filter(user -> !user.isBot()).collect(Collectors.toList());
            Random random = new Random();
            int user = random.nextInt(potentialHosts.size());
            User newHost = users.get(user);
            changeHost(event.getGuild(), newHost);
            m.editMessage(message + " " + LocaleUtils.getString("music.moved.hostnotfound", KekBot.getGuildLocale(event.getGuild())) + "\n\n" +
                    LocaleUtils.getString("music.newhost", KekBot.getGuildLocale(event.getGuild()), newHost.getName())).queue();
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
            String message = LocaleUtils.getString("music.moved", KekBot.getGuildLocale(event.getGuild()), "`" + event.getChannelJoined().getName() + "`");
            announceToMusicSession(event.getGuild(), message + " " + LocaleUtils.getString("music.moved.hostnotfound", KekBot.getGuildLocale(event.getGuild())) + "\n\n" +
                    LocaleUtils.getString("music.newhost", KekBot.getGuildLocale(event.getGuild()), newHost.getName()));
        } else {
            musicManager.channel.sendMessage(LocaleUtils.getString("music.awaituser", KekBot.getGuildLocale(event.getGuild()))).queue(m -> {
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
                    else {
                        if (channelJoined == null) return false;
                        else return channelJoined.equals(KekBot.jda.getGuildById(event1.getGuild().getId()).getAudioManager().getConnectedChannel());
                    }
                }, event1 -> {
                    if (leftWaitingVoice(musicManager, m, event1)) return;


                    if (event1.getMember().equals(event1.getGuild().getSelfMember()) && event1 instanceof GuildVoiceMoveEvent) {
                        String message = LocaleUtils.getString("music.moved", KekBot.getGuildLocale(event.getGuild()), "`" + event1.getVoiceState().getChannel().getName() + "`");
                        if (event1.getVoiceState().getChannel().getMembers().contains(event.getMember()))
                            m.editMessage(message + " " + LocaleUtils.getString("music.moved.hostfound", KekBot.getGuildLocale(event.getGuild()))).queue();
                        else {
                            Random random = new Random();
                            int user = random.nextInt(users.size());
                            User newHost = users.get(user);
                            changeHost(event.getGuild(), newHost);
                            m.editMessage(message + " " + LocaleUtils.getString("music.moved.hostnotfound", KekBot.getGuildLocale(event.getGuild())) + "\n\n" +
                                    LocaleUtils.getString("music.newhost", KekBot.getGuildLocale(event.getGuild()), newHost.getName())).queue();
                        }
                        if (musicManager.player.isPaused()) musicManager.player.setPaused(false);
                        musicManager.stopWaiting();
                        return;
                    }

                    Member member = null;
                    if (event1 instanceof GuildVoiceJoinEvent) member = event1.getMember();
                    if (event1 instanceof GuildVoiceMoveEvent) member = event1.getMember();

                    if (isWaitingUserHost(musicManager, m, member, event.getGuild())) return;
                    if (!member.getUser().isBot()) m.editMessage(LocaleUtils.getString("music.awaituser.success", KekBot.getGuildLocale(event.getGuild()), member.getUser().getName())).queue();
                    if (musicManager.player.isPaused()) musicManager.player.setPaused(false);
                    changeHost(event.getGuild(), member.getUser());
                    musicManager.stopWaiting();
                }, 60, TimeUnit.SECONDS, () -> {
                    m.editMessage(KekBot.respond(Action.MUSIC_EMPTY_CHANNEL, KekBot.getGuildLocale(event.getGuild()))).queue();
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
