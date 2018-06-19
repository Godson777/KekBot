package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.LocaleUtils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.music.Playlist;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class Music extends Command {

    public Music() {
        name = "music";
        description = "Central command for all music related actions.";
        aliases = new String[]{"m"};
        category = new Category("Fun");
        usage.add("music queue <URL> - Queues a music track.");
        usage.add("music queue searchyt <name> - Queues a music track based on a youtube search.");
        usage.add("music queue playlist <name> - Plays a playlist you have saved.");
        usage.add("music vol <volume> - Sets the volume to the number you set.");
        usage.add("music skip - Skips a track. (Host Only)");
        usage.add("music skip <to skip> - Skips X tracks. (Host Only)");
        usage.add("music skipto <track #> - Skips to X track. (Host Only)");
        usage.add("music voteskip - Casts a vote to skip the track.");
        usage.add("music remove <track #> - Removes a track from the queue. (Host Only)");
        usage.add("music song - Gets the current song info.");
        usage.add("music playlist - Lists all the tracks that are in the queue.");
        usage.add("music host - Makes someone else the \"Host\". (Host Only)");
        usage.add("music stop - Stops the current music session. (Host Only)");
        usage.add("music pause - Pauses the current music session. (Host Only)");
        usage.add("music resume - Resumes the current music session. (Host Only)");
        usage.add("music repeat - Toggles repeat mode, switches from OFF, SINGLE, and MULTI. (Host Only)");
        usage.add("music shuffle - Shuffles all the tracks that are in the queue. (Host Only)");
        extendedDescription = "All \"Host Only\" commands can also be executed by a user with Administrator permissions.";
        exDescPos = ExtendedPosition.AFTER;
    }

    private static Function<String, String> parseURL = url -> url.startsWith("<") && url.endsWith(">") ?
        url.substring(url.indexOf("<") + 1, url.lastIndexOf(">")) :
        url;

    private static Stream<String> parseURLs(String[] args) {
        return Arrays.stream(args).map(parseURL);
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (!event.getMember().getVoiceState().inVoiceChannel()) {
            event.getChannel().sendMessage(KekBot.respond(Action.GET_IN_VOICE_CHANNEL, event.getLocale())).queue();
            return;
        }

        if (event.getGuild().getAudioManager().isConnected() && !event.getGuild().getAudioManager().getConnectedChannel().equals(event.getMember().getVoiceState().getChannel())) {
            event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_IN_CHANNEL, event.getLocale(), "`" + event.getGuild().getAudioManager().getConnectedChannel().getName() + "`")).queue();
            return;
        }

        final String[] args = event.getArgs();

        if (args.length == 0) {
            event.getChannel().sendMessage(LocaleUtils.getString("command.noargs", event.getLocale(), "`" + event.getPrefix() + "help " + name + "`")).queue();
            return;
        }

        final String cmd = args[0].toLowerCase();
        switch (cmd) {
            case "play":
            case "queue":
            case "que": // lul
            case "q":
                if (args.length == 1) {
                    final List<Message.Attachment> attachments = event.getMessage().getAttachments();
                    if (attachments.size() > 0) {
                        for (final Message.Attachment attachment : attachments) {
                            KekBot.player.loadAndPlay(event, attachment.getUrl());
                        }
                    } else if (!cmd.equals("play")) showPlaylist(event);
                } else {
                    final String subcmd = args[1].toLowerCase();

                    switch (subcmd) {
                        case "playlist":
                        case "pl":
                            if (args.length < 3) {
                                event.getChannel().sendMessage(event.getString("command.fun.music.queue.playlist.noargs")).queue();
                                return;
                            }

                            String playlistName = event.combineArgs(2);
                            Profile profile = Profile.getProfile(event.getAuthor());
                            Stream<Playlist> playlists = profile.getPlaylists().stream();
                            // Try to find an exact match. If there isn't one, try a case insensitive match.
                            Optional<Playlist> playlist = playlists.filter(pl -> pl.getName().equals(playlistName)).findFirst();
                            if (!playlist.isPresent()) {
                                playlist = playlists.filter(pl -> pl.getName().equalsIgnoreCase(playlistName)).findFirst();
                            }

                            if (playlist.isPresent()) KekBot.player.loadAndPlay(event, playlist.get(), profile);
                            else
                                event.getChannel().sendMessage(CustomEmote.think() + " " + event.getString("command.fun.music.queue.playlist.playlistnotfound")).queue();
                            break;
                        case "searchyt":
                        case "syt":
                            if (args.length < 3) {
                                event.getChannel().sendMessage(event.getString("command.fun.music.queue.searchyt.noargs")).queue();
                                return;
                            }
                            String search = event.combineArgs(2);
                            event.getChannel().sendMessage(event.getString("command.fun.music.queue.searchyt.search", "`" + search + "`")).queue();
                            search = "ytsearch:" + search;
                            KekBot.player.loadAndSearchYT(event, search);
                            break;
                        default:
                            parseURLs(Arrays.copyOfRange(args, 1, args.length)).forEach(trackUrl -> KekBot.player.loadAndPlay(event, trackUrl));
                            break;
                    }
                }
                break;

            case "volume":
            case "vol":
            case "v":
                if (!event.getGuild().getAudioManager().isConnected()) {
                    event.getTextChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING, event.getLocale())).queue();
                    return;
                }

                if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) && !KekBot.player.getHost(event.getGuild()).equals(event.getAuthor())) {
                    event.getTextChannel().sendMessage(event.getString("music.nothost", "`Administrator`")).queue();
                    return;
                }

                if (args.length == 1) {
                    event.getChannel().sendMessage(event.getString("command.fun.music.volume.noargs")).queue();
                    return;
                }

                try {
                    int volume = Integer.valueOf(args[1]);
                    KekBot.player.setVolume(event, volume);
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), args[1])).queue();
                }
                break;

            case "skip":
                if (!event.getGuild().getAudioManager().isConnected()) {
                    event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING, event.getLocale())).queue();
                    return;
                }

                if (args.length == 1) {
                    KekBot.player.skipTrack(event);
                    return;
                }
                try {
                    int toSkip = Integer.valueOf(args[1]);
                    KekBot.player.skipTrack(event, toSkip);
                } catch (NumberFormatException e) {
                    KekBot.player.skipTrack(event);
                }
                break;

            case "skipto":
                if (!event.getGuild().getAudioManager().isConnected()) {
                    event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING, event.getLocale())).queue();
                    return;
                }

                if (args.length == 1) {
                    event.getChannel().sendMessage(event.getString("command.fun.music.notrack")).queue();
                    return;
                }
                try {
                    int skipTo = Integer.valueOf(args[1]);
                    KekBot.player.skipToTrack(event, skipTo);
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), "`" + args[1] + "`")).queue();
                }
                break;

            case "remove":
                if (!event.getGuild().getAudioManager().isConnected()) {
                    event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING, event.getLocale())).queue();
                    return;
                }

                if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) && !KekBot.player.getHost(event.getGuild()).equals(event.getAuthor())) {
                    event.getTextChannel().sendMessage(event.getString("music.nothost", "`Administrator`")).queue();
                    return;
                }

                if (args.length == 1) {
                    event.getChannel().sendMessage(event.getString("command.fun.music.notrack")).queue();
                    return;
                }
                try {
                    int toRemove = Integer.valueOf(args[1]) - 1;
                    KekBot.player.removeTrack(event, toRemove);
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), "`" + args[1] + "`")).queue();
                }
                break;

            case "voteskip":
                if (!event.getGuild().getAudioManager().isConnected()) {
                    event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING, event.getLocale())).queue();
                    return;
                }

                KekBot.player.voteSkip(event);
                break;

            case "song":
            case "nowplaying":
            case "np":
                if (!event.getGuild().getAudioManager().isConnected()) {
                    event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING, event.getLocale())).queue();
                    return;
                }

                KekBot.player.getCurrentSong(event);
                break;

            case "playlist":
                showPlaylist(event);
                break;

            case "host":
                if (!event.getGuild().getAudioManager().isConnected()) {
                    event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING, event.getLocale())).queue();
                    return;
                }

                if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) && !KekBot.player.getHost(event.getGuild()).equals(event.getAuthor())) {
                    event.getTextChannel().sendMessage(event.getString("music.nothost", "`Administrator`")).queue();
                    return;
                }

                if (args.length == 1) {
                    event.getChannel().sendMessage(event.getString("command.fun.music.host.noargs")).queue();
                    return;
                }
                if (event.getMentionedUsers().size() > 0) {
                    User newHost = event.getMentionedUsers().get(0);
                    KekBot.player.changeHost(event.getGuild(), newHost);
                    event.getChannel().sendMessage(event.getString("command.fun.music.host.success", newHost.getName())).queue();
                } else event.getChannel().sendMessage(event.getString("command.fun.music.host.nomention")).queue();
                break;

            case "stop":
            case "disconnect":
            case "dc":
            case "leave":
            case "fuckoff":
            case "gtfo":
                if (!event.getGuild().getAudioManager().isConnected()) {
                    event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING, event.getLocale())).queue();
                    return;
                }

                KekBot.player.closeConnection(event.getGuild());
                break;

            case "unpause":
            case "resume":
                if (!event.getGuild().getAudioManager().isConnected()) {
                    event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING, event.getLocale())).queue();
                    return;
                }

                KekBot.player.unpauseTrack(event);
                break;

            case "pause":
                if (!event.getGuild().getAudioManager().isConnected()) {
                    event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING, event.getLocale())).queue();
                    return;
                }

                KekBot.player.pauseTrack(event);
                break;
            case "repeat":
                if (!event.getGuild().getAudioManager().isConnected()) {
                    event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING, event.getLocale())).queue();
                    return;
                }

                KekBot.player.repeat(event);
                break;

            case "shuffle":
                if (!event.getGuild().getAudioManager().isConnected()) {
                    event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING, event.getLocale())).queue();
                    return;
                }

                if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) && !KekBot.player.getHost(event.getGuild()).equals(event.getAuthor())) {
                    event.getTextChannel().sendMessage(event.getString("music.nothost", "`Administrator`")).queue();
                    return;
                }

                KekBot.player.shuffle(event);
                break;
        }
    }

    private void showPlaylist(CommandEvent event) {
        if (!event.getGuild().getAudioManager().isConnected()) {
            event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING, event.getLocale())).queue();
            return;
        }

        KekBot.player.getPlaylist(event);
    }

}