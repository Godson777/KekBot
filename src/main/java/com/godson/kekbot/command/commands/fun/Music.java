package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.music.Playlist;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Arrays;
import java.util.Optional;

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
        usage.add("music voteskip - Casts a vote to skip the track.");
        usage.add("music song - Gets the current song info.");
        usage.add("music playlist - Lists all the tracks that are in the queue.");
        usage.add("music host - Makes someone else the \"Host\". (Host Only)");
        usage.add("music stop - Stops the current music session. (Host Only)");
        usage.add("music pause - Pauses the current music session. (Host Only)");
        usage.add("music repeat - Toggles repeat mode, switches from OFF, SINGLE, and MULTI. (Host Only)");
        usage.add("music shuffle - Shuffles all the tracks that are in the queue. (Host Only)");
        extendedDescription = "All \"Host Only\" commands can also be executed by a user with Administrator permissions.";
        exDescPos = ExtendedPosition.AFTER;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (!event.getMember().getVoiceState().inVoiceChannel()) {
            event.getChannel().sendMessage(KekBot.respond(Action.GET_IN_VOICE_CHANNEL)).queue();
            return;
        }

        if (event.getGuild().getAudioManager().isConnected() && !event.getGuild().getAudioManager().getConnectedChannel().equals(event.getMember().getVoiceState().getChannel())) {
                event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_IN_CHANNEL, "`" + event.getGuild().getAudioManager().getConnectedChannel().getName() + "`")).queue();
                return;
        }

        if (event.getArgs().length > 0) {
            switch (event.getArgs()[0].toLowerCase()) {
                case "queue":
                    if (event.getArgs().length > 1) {
                        if (event.getArgs()[1].equalsIgnoreCase("playlist")) {
                            if (event.getArgs().length > 2) {
                                String playlistName = event.combineArgs(2);
                                Profile profile = Profile.getProfile(event.getAuthor());
                                Optional<Playlist> playlist = profile.getPlaylists().stream().filter(playlist1 -> playlist1.getName().equals(playlistName)).findFirst();
                                if (playlist.isPresent()) {
                                    KekBot.player.loadAndPlay(event, playlist.get(), profile);
                                } else
                                    event.getChannel().sendMessage(CustomEmote.think() + " I'm not finding any playlists by that name... Did you type it correctly?").queue();
                            } else
                                event.getChannel().sendMessage("Huh? I get you want to queue a playlist, but you didn't give me the name of your playlist...").queue();
                        } else if (event.getArgs()[1].equalsIgnoreCase("searchyt")) {
                            if (event.getArgs().length > 2) {
                                String search = event.combineArgs(2);
                                event.getChannel().sendMessage("Searching youtube for: `" + search + "`").queue();
                                search = "ytsearch:" + search;
                                KekBot.player.loadAndSearchYT(event, search);
                            } else event.getChannel().sendMessage("No search terms provided.").queue();
                        } else {
                            String trackUrl = event.combineArgs(1);
                            if (trackUrl.startsWith("<") && trackUrl.endsWith(">")) trackUrl = trackUrl.substring(trackUrl.indexOf("<") + 1, trackUrl.lastIndexOf(">"));
                            KekBot.player.loadAndPlay(event, trackUrl);
                        }
                    }
                    break;
                case "vol":
                    if (!event.getGuild().getAudioManager().isConnected()) {
                        event.getTextChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING)).queue();
                        return;
                    }

                    if (event.getArgs().length > 1) {
                        try {
                            int volume = Integer.valueOf(event.getArgs()[1]);
                            KekBot.player.setVolume(event, volume);
                        } catch (NumberFormatException e) {
                            event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getArgs()[1])).queue();
                        }
                    } else event.getChannel().sendMessage("You haven't even specified the volume you want to set it to!").queue();
                    break;
                case "skip":
                    if (!event.getGuild().getAudioManager().isConnected()) {
                        event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING)).queue();
                        return;
                    }

                    KekBot.player.skipTrack(event, false);
                    break;
                case "voteskip":
                    if (!event.getGuild().getAudioManager().isConnected()) {
                        event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING)).queue();
                        return;
                    }

                    KekBot.player.skipTrack(event, true);
                    break;
                case "song":
                    if (!event.getGuild().getAudioManager().isConnected()) {
                        event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING)).queue();
                        return;
                    }

                    KekBot.player.getCurrentSong(event.getTextChannel());
                    break;
                case "playlist":
                    if (!event.getGuild().getAudioManager().isConnected()) {
                        event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING)).queue();
                        return;
                    }

                    KekBot.player.getPlaylist(event);
                    break;
                case "host":
                    if (!event.getGuild().getAudioManager().isConnected()) {
                        event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING)).queue();
                        return;
                    }

                    if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) && !KekBot.player.getHost(event.getGuild()).equals(event.getAuthor())) {
                        event.getTextChannel().sendMessage("Only the host and users with the `Administrator` permission can set the volume!").queue();
                        return;
                    }

                    if (event.getArgs().length > 0) {
                        if (event.getMessage().getMentionedUsers().size() > 0) {
                            User newHost = event.getMessage().getMentionedUsers().get(0);
                            KekBot.player.changeHost(event.getGuild(), newHost);
                            event.getChannel().sendMessage("Done, " + newHost.getName() + " is now the host.").queue();
                        } else event.getChannel().sendMessage("You have to mention the user you wanna make the host!").queue();
                    } else event.getChannel().sendMessage("You haven't specified who to make the host...").queue();
                    break;
                case "stop":
                    if (!event.getGuild().getAudioManager().isConnected()) {
                        event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING)).queue();
                        return;
                    }

                    KekBot.player.closeConnection(event.getGuild());
                    break;
                case "pause":
                    if (!event.getGuild().getAudioManager().isConnected()) {
                        event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING)).queue();
                        return;
                    }

                    KekBot.player.pauseTrack(event);
                    break;
                case "repeat":
                    if (!event.getGuild().getAudioManager().isConnected()) {
                        event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING)).queue();
                        return;
                    }

                    KekBot.player.repeat(event);
                    break;
                case "shuffle":
                    if (!event.getGuild().getAudioManager().isConnected()) {
                        event.getChannel().sendMessage(KekBot.respond(Action.MUSIC_NOT_PLAYING)).queue();
                        return;
                    }

                    KekBot.player.shuffle(event);
                    break;
            }
        } else event.getChannel().sendMessage("No arguments provided. Check " + event.getPrefix() + "help " + name + " for more help.").queue();
    }
}
