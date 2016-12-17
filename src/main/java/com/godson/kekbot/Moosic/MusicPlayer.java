package com.godson.kekbot.Moosic;

import com.darichey.discord.api.CommandContext;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class MusicPlayer extends ListenerAdapter {

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
                        long totalLength = 0;
                        AudioTrack[] playlist = musicManager.scheduler.getQueue().toArray(new AudioTrack[musicManager.scheduler.getQueue().size()]);
                        for (int i = 0; i < playlist.length; i++) {
                            totalLength += playlist[i].getDuration();
                        }
                        timeBefore = " (Time before it plays: " +
                                KekBot.convertMillisToTime(
                                        (musicManager.player.getPlayingTrack().getDuration() - musicManager.player.getPlayingTrack().getPosition() + (totalLength - track.getDuration()))) + " **Queue Position: " + musicManager.scheduler.getQueue().size() + "**)";
                    }
                    context.getTextChannel().sendMessage("Added \"" + track.getInfo().title + "\" to the queue." + timeBefore).queue();
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    context.getTextChannel().sendMessage("Attempting to add all the songs in " + playlist.getName() + ". (Note: This may take a while depending on the size of the playlist...)").queue();
                    int failed = 0;

                    for (AudioTrack track : playlist.getTracks()) {
                        if (track != null) {
                            play(context, musicManager, track);
                        } else failed++;
                    }

                    context.getTextChannel().sendMessage("Complete." + (failed > 0 ? " (" + failed + " tracks could not be added.)" : "")).queue();
                }

                @Override
                public void noMatches() {
                    context.getTextChannel().sendMessage("Hm, " + trackUrl + "doesn't appear to be a valid URL. Could you try again?").queue();
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

    private void play(CommandContext context, GuildMusicManager musicManager, AudioTrack track) {
        connectToUsersVoiceChannel(context);

        musicManager.scheduler.queue(track, context.getAuthor());
    }

    private void playMeme(CommandContext context, GuildMusicManager musicManager, AudioTrack track) {
        connectToUsersVoiceChannel(context);

        musicManager.memeScheduler.queue(track);
    }

    public void skipTrack(CommandContext context) {
        GuildMusicManager musicManager = getGuildAudioPlayer(context, false);
        Guild guild = context.getGuild();
        if (!musicManager.isMeme()) {
            if (getHost(guild).equals(context.getAuthor()) || context.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                if (musicManager.scheduler.getQueue().size() > 0) {
                    context.getTextChannel().sendMessage("Skipped to next track.").queue();
                    musicManager.scheduler.nextTrack();
                } else {
                    context.getTextChannel().sendMessage("There are no more tracks to skip to!").queue();
                }
            } else {
                context.getTextChannel().sendMessage(KekBot.replacePrefix(context.getGuild(), "Only the host can skip tracks.")).queue();
            }
        } else {
            context.getTextChannel().sendMessage("I can't skip memes. :neutral_face:").queue();
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
                    context.getTextChannel().sendMessage(context.getAuthor().getAsMention() + " is now hosting a music session in: " + voiceChannel.get().getName() + KekBot.replacePrefix(context.getGuild(), ", use {p}music to get the list of all music commands.")).queue();
                    musicManagers.get(Long.parseLong(context.getGuild().getId())).scheduler.currentPlayer = context.getAuthor();
                }
            }
        }
    }

    public void announceToMusicSession(Guild guild, String message) {
        musicManagers.get(Long.parseLong(guild.getId())).channel.sendMessage(message).queue();
    }

    public void getPlaylist(TextChannel channel, int page) {
        long guildId = Long.parseLong(channel.getGuild().getId());
        if (musicManagers.containsKey(guildId)) {
            GuildMusicManager musicManager = musicManagers.get(guildId);
            if (!musicManager.isMeme()) {
                if (musicManager.scheduler.getQueue().size() > 0) {
                    List<String> tracks = new ArrayList<>();
                    long totalLength = 0;
                    AudioTrack[] audioTracks = musicManager.scheduler.getQueue().toArray(new AudioTrack[musicManager.scheduler.getQueue().size()]);
                    User[] users = musicManager.scheduler.getUsers().toArray(new User[musicManager.scheduler.getUsers().size()]);
                    for (int i = 0; i < audioTracks.length; i++) {
                        tracks.add(i + 1 + ".) " + audioTracks[i].getInfo().title + " - **(" + KekBot.convertMillisToHMmSs(audioTracks[i].getDuration()) + ")** - " + "Queued by: " + users[i].getName());
                        totalLength += audioTracks[i].getDuration();
                    }
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
                    channel.sendMessage(playlist + "\n**Total Length: " + KekBot.convertMillisToTime(totalLength) + "**").queue();
                } else channel.sendMessage("There is nothing in the playlist!").queue();
            } else channel.sendMessage("I'm memeing at the moment, there is no playlist...").queue();
        } else channel.sendMessage("There is no music playing!").queue();
    }

    public void getCurrentSong(TextChannel channel) {
        long guildId = Long.parseLong(channel.getGuild().getId());
        if (musicManagers.containsKey(guildId)) {
            if (!musicManagers.get(guildId).isMeme()) {
                AudioTrack track = musicManagers.get(guildId).player.getPlayingTrack();
                channel.sendMessage("Currently Playing: `" + track.getInfo().title + "` [" + KekBot.songTimestamp(track.getPosition(), track.getDuration()) + "]"
                        + "\nQueued by: " + musicManagers.get(guildId).scheduler.currentPlayer.getName()
                        + "\nVolume: " + musicManagers.get(guildId).player.getVolume()).queue();
            } else {
                channel.sendMessage("I'm memeing, there is no song playing.").queue();
            }
        }
    }

    public void closeConnection(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        if (!isMeme(guild)) announceToMusicSession(guild, "This music session has now ended.");
        this.musicManagers.remove(guildId);
        guild.getAudioManager().closeAudioConnection();
    }

    public void setVolume(CommandContext context, int volume) {
        long guildId = Long.parseLong(context.getGuild().getId());
        if (musicManagers.containsKey(guildId)) {
            if (volume <= 150 && volume >= 0) {
                musicManagers.get(guildId).player.setVolume(volume);
                context.getTextChannel().sendMessage("Volume set to " + volume).queue();
            } else context.getTextChannel().sendMessage("Specified volume must be between 150 and 0!").queue();
        }
    }
}
