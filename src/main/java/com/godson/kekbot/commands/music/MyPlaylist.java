package com.godson.kekbot.commands.music;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Music.Playlist;
import com.godson.kekbot.Profile.Profile;
import com.godson.kekbot.Questionaire.QuestionType;
import com.godson.kekbot.Questionaire.Questionnaire;
import com.godson.kekbot.Utils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MyPlaylist {
    public static Command myPlaylist = new Command("myplaylist")
            .withAliases("myplist")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Allows the user to make their own playlist.")
            .withUsage("{p}myplaylist")
            .onExecuted(context -> {
                Profile profile = Profile.getProfile(context.getAuthor());
                new Questionnaire(context)
                        .addChoiceQuestion("Welcome to the custom playlist wizard! Here, you can **create**, **remove**, **edit** and **view** your playlists! Remember that you can also say `cancel` at any time to quit." +
                                (profile.getPlaylists().size() > 0 ? "" : "\nHm, you don't seem to have any playlists to **remove**, **edit**, or **view**... Why not try to **create** one, first?"), "create", "remove", "edit", "view")
                        .withCustomErrorMessage("Hm, that doesn't sound like a valid option... Could you try again?")
                        .withoutRepeats()
                        .execute(results -> {
                            switch (results.getAnswer(0).toString()) {
                                case "create":
                                    new Questionnaire(results)
                                            .addQuestion("Great! Now all we need is your new playlist's name. Type it in below, or say `cancel` to quit.", QuestionType.STRING)
                                            .execute(results1 -> {
                                                String playlistName = results1.getAnswer(0).toString();
                                                if (profile.getPlaylists().stream().anyMatch(playlist -> playlist.getName().equalsIgnoreCase(playlistName))) {
                                                    context.getTextChannel().sendMessage(CustomEmote.think() + " Hm, you already have a playlist with that name. Try coming up with something else?").queue();
                                                    results1.reExecuteWithoutMessage();
                                                } else if (playlistName.equalsIgnoreCase("exit")) {
                                                    context.getTextChannel().sendMessage("Woah there, you can't name your playlist *that*! That's a reserved word! Sorry fam, but I'mma need you to come up with something else...").queue();
                                                    results1.reExecuteWithoutMessage();
                                                } else if (playlistName.length() > 50) {
                                                    context.getTextChannel().sendMessage("Woah there, that playlist is too long! Could you try to keep it less than 50 characters?").queue();
                                                    results1.reExecuteWithoutMessage();
                                                } else {
                                                    Playlist playlist = new Playlist(playlistName);
                                                    new Questionnaire(results1)
                                                            .addQuestion("Hmm, `" + playlistName + "`, eh? Sounds good! Now, all we need are some tracks. All you have to do is get the URL of your favorite YouTube or SoundCloud track, and paste it. Keep pasting URLs as you add them to your playlist. Then, when you're done, say `done`. Make sure you wait for any tracks to be loaded before finishing, otherwise they won't be saved.", QuestionType.STRING)
                                                            .execute(results2 -> {
                                                                String URL = results2.getAnswer(0).toString();
                                                                if (!URL.equalsIgnoreCase("done")) {
                                                                    KekBot.player.addToPlaylist(results2, URL, playlist);
                                                                } else {
                                                                    if (playlist.getTracks().size() > 0) {
                                                                        new Questionnaire(results2)
                                                                                .addYesNoQuestion("Alright! Now that *that's* all set, I gotta ask real quick... Do you want to hide this playlist from your profile card? You'll still be able to queue it whenever, it'll just stay hidden from everyone else who might be looking.")
                                                                                .execute(results3 -> {
                                                                                    if (results3.getAnswer(0).toString().equalsIgnoreCase("Yes") || results3.getAnswer(0).toString().equalsIgnoreCase("Y")) {
                                                                                        playlist.setHidden(true);
                                                                                        profile.addPlaylist(playlist);
                                                                                        profile.save();
                                                                                        context.getTextChannel().sendMessage("Boom! You've just made a brand spankin' new playlist! You can queue it at any time using `" + KekBot.replacePrefix(context.getGuild(), "{p}") + "queue playlist " + playlist.getName() + "`!").queue();
                                                                                    } else {
                                                                                        profile.addPlaylist(playlist);
                                                                                        profile.save();
                                                                                        context.getTextChannel().sendMessage("Boom! You've just made a brand spankin' new playlist! You can queue it at any time using `" + KekBot.replacePrefix(context.getGuild(), "{p}") + "queue playlist " + playlist.getName() + "`!").queue();
                                                                                    }
                                                                                });

                                                                    } else {
                                                                        context.getTextChannel().sendMessage("You can't finish making your playlist if there are no tracks in it! Try adding some tracks, first! All you have to do is get the URL of your favorite YouTube or SoundCloud track, and paste it. Keep pasting URLs as you add them to your playlist. Then, when you're done, say `done`.").queue();
                                                                        results2.reExecuteWithoutMessage();
                                                                    }
                                                                }
                                                            });
                                                }
                                            });
                                    break;
                                case "remove":
                                    if (profile.getPlaylists().size() == 0) {
                                        context.getTextChannel().sendMessage("Woah there, you don't seem to have any playlists created. You should try to *create* a playlist, first.").queue();
                                    } else {
                                        new Questionnaire(results)
                                                .addQuestion("If you know the name of your playlist, type it here. (Case Insensitive)\nIf not, you could also try to *view* your playlists, and then go from there.", QuestionType.STRING)
                                                .execute(results1 -> {
                                                    switch (results1.getAnswer(0).toString()) {
                                                        case "view":
                                                            listPlaylists(results1, 1, profile);
                                                            break;
                                                        default:
                                                            Optional<Playlist> potentialPlaylist = profile.getPlaylists().stream().filter(playlist1 -> playlist1.getName().equalsIgnoreCase(results1.getAnswer(0).toString())).findFirst();
                                                            if (potentialPlaylist.isPresent()) {
                                                                Playlist playlist = potentialPlaylist.get();
                                                                confirmDelete(results1, playlist, profile);
                                                            } else {
                                                                context.getTextChannel().sendMessage("Hm, there doesn't appear to be a playlist by that name. Try another one?").queue();
                                                            }
                                                            break;
                                                    }
                                                });
                                    }
                                    break;
                                case "edit":
                                    if (profile.getPlaylists().size() == 0) {
                                        context.getTextChannel().sendMessage("Woah there, you don't seem to have any playlists created. You should try to *create* a playlist, first.").queue();
                                    } else {
                                        new Questionnaire(results)
                                                .addQuestion("If you know the name of your playlist, type it here. (Case Insensitive)\nIf not, you could also try to *view* your playlists, and then go from there.", QuestionType.STRING)
                                                .execute(results1 -> {
                                                    switch (results1.getAnswer(0).toString()) {
                                                        case "view":
                                                            listPlaylists(results1, 2, profile);
                                                            break;
                                                        default:
                                                            Optional<Playlist> potentialPlaylist = profile.getPlaylists().stream().filter(playlist1 -> playlist1.getName().equalsIgnoreCase(results1.getAnswer(0).toString())).findFirst();
                                                            if (potentialPlaylist.isPresent()) {
                                                                Playlist playlist = potentialPlaylist.get();
                                                                editPlaylist(results1, playlist, profile);
                                                            } else {
                                                                context.getTextChannel().sendMessage("Hm, there doesn't appear to be a playlist by that name. Try another one?").queue();
                                                            }
                                                            break;
                                                    }
                                                });
                                    }
                                    break;
                                case "view":
                                    if (profile.getPlaylists().size() == 0) {
                                        context.getTextChannel().sendMessage("Woah there, you don't seem to have any playlists created. You should try to *create* a playlist, first.").queue();
                                    } else {
                                        listPlaylists(results, 0, profile);
                                    }
                                    break;
                            }
                        });
            });

    private static void listPlaylists(Questionnaire.Results results, int mode, Profile profile) {
        new Questionnaire(results)
                .withoutRepeats()
                .addYesNoQuestion("Do you want to include your hidden playlists on this list?")
                .execute(results1 -> {
                    List<Playlist> playlists;
                    if (results1.getAnswer(0).toString().equalsIgnoreCase("Yes") || results1.getAnswer(0).toString().equalsIgnoreCase("Y")) {
                        playlists = profile.getPlaylists();
                    } else {
                        playlists = profile.getPlaylists().stream().filter(playlist -> !playlist.isHidden()).collect(Collectors.toList());
                    }
                    List<String> playlistNames = playlists.stream().map(playlist -> "`" + playlist.getName() + "` - (" + Utils.convertMillisToTime(playlist.getTotalLength()) + ")" + (playlist.isHidden() ? " ***(HIDDEN)***" : "")).collect(Collectors.toList());
                    final int page[] = {0};
                    String message = "Here are your playlists: \n\n" +
                            StringUtils.join(playlistNames.subList(page[0] * 15, ((page[0] + 1) * 15 <= playlistNames.size() ? (page[0] + 1) * 15 : playlistNames.size())), "\n") +
                            "\n" + (playlists.size() > 15 ? "\nTo view the next page, type `next`, or to go back to a previous page, type `back`. (Page " + (page[0] + 1) + "/" + (playlistNames.size() / 15 + 1) + ")\n" : "") +
                            (mode != 0 ? "Once you've found the name of the playlist you want to " + (mode == 1 ? "remove" : (mode == 2 ? "edit" : "")) + ", type it in. Otherwise... " : "") +
                            "If you're ready to leave and do something else, type `exit`, or `cancel`.";
                    new Questionnaire(results1)
                            .withoutRepeats()
                            .addQuestion(message, QuestionType.STRING)
                            .execute(results2 -> {
                                switch (results2.getAnswer(0).toString()) {
                                    case "exit":
                                        results2.getChannel().sendMessage("Exited.").queue();
                                        break;
                                    case "next":
                                        if (((page[0] + 1) * 15) > playlists.size()) {
                                            results2.getChannel().sendMessage("There is no next page!").queue();
                                            results2.reExecuteWithoutMessage();
                                        } else {
                                            ++page[0];
                                            results.getChannel().sendMessage("Here are your playlists: \n\n" +
                                                    StringUtils.join(playlistNames.subList(page[0] * 15, ((page[0] + 1) * 15 <= playlistNames.size() ? (page[0] + 1) * 15 : playlistNames.size())), "\n") +
                                                    "\n" + (playlists.size() > 15 ? "\nTo view the next page, type `next`, or to go back to a previous page, type `back`. (Page " + (page[0] + 1) + "/" + (playlistNames.size() / 15 + 1) + ")\n" : "") +
                                                    (mode != 0 ? "Once you've found the name of the playlist you want to " + (mode == 1 ? "remove" : (mode == 2 ? "edit" : "")) + ", type it in. Otherwise... " : "") +
                                                    "If you're ready to leave and do something else, type `exit`, or `cancel`.").queue();
                                            results2.reExecuteWithoutMessage();
                                        }
                                        break;
                                    case "back":
                                        if (page[0] - 1 == -1) {
                                            results2.getChannel().sendMessage("You're already at the beginning!").queue();
                                            results2.reExecuteWithoutMessage();
                                        } else {
                                            --page[0];
                                            results.getChannel().sendMessage("Here are your playlists: \n\n" +
                                                    StringUtils.join(playlistNames.subList(page[0] * 15, ((page[0] + 1) * 15 <= playlistNames.size() ? (page[0] + 1) * 15 : playlistNames.size())), "\n") +
                                                    "\n" + (playlists.size() > 15 ? "\nTo view the next page, type `next`, or to go back to a previous page, type `back`. (Page " + (page[0] + 1) + "/" + (playlistNames.size() / 15 + 1) + ")\n" : "") +
                                                    (mode != 0 ? "Once you've found the name of the playlist you want to " + (mode == 1 ? "remove" : (mode == 2 ? "edit" : "")) + ", type it in. Otherwise... " : "") +
                                                    "If you're ready to leave and do something else, type `exit`, or `cancel`.").queue();
                                            results2.reExecuteWithoutMessage();
                                        }
                                        break;
                                    default:
                                        switch (mode) {
                                            case 0:
                                                results2.getChannel().sendMessage("I'm sorry, I didn't quite catch that, let's try that again...").queue();
                                                results2.reExecuteWithoutMessage();
                                                break;
                                            case 1:
                                                Optional<Playlist> potentialRemove = profile.getPlaylists().stream().filter(playlist1 -> playlist1.getName().equalsIgnoreCase(results2.getAnswer(0).toString())).findFirst();
                                                if (potentialRemove.isPresent()) {
                                                    Playlist playlist = potentialRemove.get();
                                                    confirmDelete(results2, playlist, profile);
                                                } else {
                                                    results2.getChannel().sendMessage("Hm, there doesn't appear to be a playlist by that name. Try another one?").queue();
                                                    results2.reExecuteWithoutMessage();
                                                }
                                                break;
                                            case 2:
                                                Optional<Playlist> potentialEdit = profile.getPlaylists().stream().filter(playlist1 -> playlist1.getName().equalsIgnoreCase(results2.getAnswer(0).toString())).findFirst();
                                                if (potentialEdit.isPresent()) {
                                                    Playlist playlist = potentialEdit.get();
                                                    editPlaylist(results2, playlist, profile);
                                                } else {
                                                    results2.getChannel().sendMessage("Hm, there doesn't appear to be a playlist by that name. Try another one?").queue();
                                                    results2.reExecuteWithoutMessage();
                                                }
                                        }
                                }
                            });
                });
    }

    private static void confirmDelete(Questionnaire.Results results, Playlist playlist, Profile profile) {
        new Questionnaire(results)
                .addYesNoQuestion("Are you sure you want to delete your playlist `" + playlist.getName() + "`?")
                .execute(results1 -> {
                    if (results1.getAnswer(0).toString().equalsIgnoreCase("Yes") || results1.getAnswer(0).toString().equalsIgnoreCase("Y")) {
                        profile.removePlaylist(playlist);
                        profile.save();
                        results1.getChannel().sendMessage("Done. That playlist is no longer with us... ☠").queue();
                    }
                });
    }

    private static void editPlaylist(Questionnaire.Results results, Playlist playlist, Profile profile) {
        List<AudioTrackInfo> tracks = playlist.getTracks();
        List<String> trackNames = tracks.stream().map(track -> (tracks.indexOf(track) + 1) + ". `" + track.title + "` - (" + Utils.convertMillisToTime(track.length) + ")").collect(Collectors.toList());
        final int[] page = {0};
        String message = "Alright, I found these tracks lying around in your playlist: \n\n" +
                StringUtils.join(trackNames.subList(page[0] * 15, ((page[0] + 1) * 15 <= trackNames.size() ? (page[0] + 1) * 15 : trackNames.size())), "\n") +
                "\n" + (tracks.size() > 15 ? "\nTo view the next page, type `next`, or to go back to a previous page, type `back`. (Page " + (page[0] + 1) + "/" + (trackNames.size() / 15 + 1) + ")" : "") +
                "\nOnce you've found the track you want to edit, type it's number in, or if you want to add tracks, say `add`. Otherwise, if you're ready to leave and do something else, type `exit`, or `cancel`.";
        new Questionnaire(results)
                .addQuestion(message, QuestionType.STRING)
                .withoutRepeats()
                .execute(results1 -> {
                    switch (results1.getAnswer(0).toString()) {
                        case "exit":
                            results1.getChannel().sendMessage("Exited.").queue();
                            break;
                        case "next":
                            if (((page[0] + 1) * 15) > tracks.size()) {
                                results1.getChannel().sendMessage("There is no next page!").queue();
                                results1.reExecuteWithoutMessage();
                            } else {
                                ++page[0];
                                results.getChannel().sendMessage("Next page eh? Well, I found these tracks lying around too... \n\n" +
                                        StringUtils.join(trackNames.subList(page[0] * 15, ((page[0] + 1) * 15 <= trackNames.size() ? (page[0] + 1) * 15 : trackNames.size())), "\n") +
                                        "\n" + (tracks.size() > 15 ? "\nTo view the next page, type `next`, or to go back to a previous page, type `back`. (Page " + (page[0] + 1) + "/" + (trackNames.size() / 15 + 1) + ")" : "") +
                                        "\nOnce you've found the track you want to edit, type it's number in, or if you want to add tracks, say `add`. Otherwise, if you're ready to leave and do something else, type `exit`, or `cancel`.").queue();
                                results1.reExecuteWithoutMessage();
                            }
                            break;
                        case "back":
                            if (page[0] - 1 == -1) {
                                results1.getChannel().sendMessage("You're already at the beginning!").queue();
                                results1.reExecuteWithoutMessage();
                            } else {
                                --page[0];
                                results.getChannel().sendMessage("Retracing our steps, are we? Alright, here's what I dug up earlier: \n\n" +
                                        StringUtils.join(trackNames.subList(page[0] * 15, ((page[0] + 1) * 15 <= trackNames.size() ? (page[0] + 1) * 15 : trackNames.size())), "\n") +
                                        "\n" + (tracks.size() > 15 ? "\nTo view the next page, type `next`, or to go back to a previous page, type `back`. (Page " + (page[0] + 1) + "/" + (trackNames.size() / 15 + 1) + ")" : "") +
                                        "\nOnce you've found the track you want to edit, type it's number in, or if you want to add tracks, say `add`. Otherwise, if you're ready to leave and do something else, type `exit`, or `cancel`.").queue();
                                results1.reExecuteWithoutMessage();
                            }
                            break;
                        case "add":
                            new Questionnaire(results1)
                                    .addQuestion("Alright, let's give this playlist some more tracks to hold! All you have to do is get the URL of your favorite YouTube or SoundCloud track, and paste it. Keep pasting URLs as you add them to your playlist. Then, when you're done, say `done`. Make sure you wait for any tracks to be loaded before finishing, otherwise they won't be saved.", QuestionType.STRING)
                                    .execute(results2 -> {
                                        String URL = results2.getAnswer(0).toString();
                                        if (!URL.equalsIgnoreCase("done")) {
                                            KekBot.player.addToPlaylist(results2, URL, playlist);
                                        } else {
                                            profile.save();
                                            new Questionnaire(results2)
                                                    .addYesNoQuestion("Done, I've saved your playlist. Now that we've covered that, do you want to go back to editing your playlist?")
                                                    .execute(results3 -> {
                                                        if (results3.getAnswer(0).toString().equalsIgnoreCase("Yes") || results3.getAnswer(0).toString().equalsIgnoreCase("Y")) {
                                                            editPlaylist(results3, playlist, profile);
                                                        } else {
                                                            results3.getChannel().sendMessage("Exited.").queue();
                                                        }
                                                    });
                                        }
                                    });
                            break;
                        default:
                            int trackNumber;
                            try {
                                trackNumber = Integer.valueOf(results1.getAnswer(0).toString()) - 1;
                            } catch (NumberFormatException e) {
                                results1.getChannel().sendMessage("That doesn't appear to be a valid option... Could you try again?").queue();
                                results1.reExecuteWithoutMessage();
                                return;
                            }
                            if (trackNumber < playlist.getTracks().size() && trackNumber >= 0) {
                                AudioTrackInfo track = playlist.getTracks().get(trackNumber);
                                new Questionnaire(results1)
                                        .addYesNoQuestion("Because moving and replacing tracks has not been implemented yet, all you can do right now is remove.\nDo you want to remove this track?")
                                        .execute(results2 -> {
                                            if (results2.getAnswer(0).toString().equalsIgnoreCase("Yes") || results2.getAnswer(0).toString().equalsIgnoreCase("Y")) {
                                                playlist.removeTrack(track);
                                                profile.save();
                                                if (tracks.size() > 0) {
                                                    new Questionnaire(results2)
                                                            .addYesNoQuestion("Alright. That track is gone. Do you want to to back to editing your playlist?")
                                                            .execute(results3 -> {
                                                                if (results3.getAnswer(0).toString().equalsIgnoreCase("Yes") || results3.getAnswer(0).toString().equalsIgnoreCase("Y")) {
                                                                    editPlaylist(results3, playlist, profile);
                                                                } else {
                                                                    results3.getChannel().sendMessage("Exited.").queue();
                                                                }
                                                            });
                                                } else {
                                                    profile.removePlaylist(playlist);
                                                    profile.save();
                                                    results2.getChannel().sendMessage("Because you deleted all the tracks in this playlist, the playlist has been removed. Exited.").queue();
                                                }
                                            } else {
                                                new Questionnaire(results2)
                                                        .addYesNoQuestion("Alright, I've left the track alone. Do you want to to back to editing your playlist?")
                                                        .execute(results3 -> {
                                                            if (results3.getAnswer(0).toString().equalsIgnoreCase("Yes") || results3.getAnswer(0).toString().equalsIgnoreCase("Y")) {
                                                                editPlaylist(results3, playlist, profile);
                                                            } else {
                                                                results3.getChannel().sendMessage("Exited.").queue();
                                                            }
                                                        });
                                            }
                                        });
                            } else {
                                results1.getChannel().sendMessage("There's no track in that position. Try another one.").queue();
                                results1.reExecuteWithoutMessage();
                            }
                    }
                });
    }
}
