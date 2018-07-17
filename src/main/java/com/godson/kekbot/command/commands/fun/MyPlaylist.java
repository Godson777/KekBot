package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.util.LocaleUtils;
import com.godson.kekbot.util.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.menu.PagedSelectionMenu;
import com.godson.kekbot.music.Playlist;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.jagrosh.jdautilities.menu.Paginator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MyPlaylist extends Command {

    public MyPlaylist() {
        name = "myplaylist";
        aliases = new String[]{"myplist"};
        description = "Allows the user to make, view and edit their own playlists.";
        usage.add("myplaylist");
        category = new Category("Fun");
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        Profile profile = Profile.getProfile(event.getAuthor());
        Questionnaire.newQuestionnaire(event)
                .addChoiceQuestion(event.getString("command.fun.myplaylist.intro", "**create**", "**remove**", "**edit**", "**view**", "`cancel`") +
                        (profile.getPlaylists().size() > 0 ? "" : "\n" + event.getString("command.fun.myplaylist.intro.empty", "**remove**", "**edit**", "**view**", "**create**")), "create", "remove", "edit", "view")
                .withCustomErrorMessage(event.getString("command.fun.myplaylist.invalidchoice"))
                .withoutRepeats()
                .includeCancel(false)
                .execute(results -> {
                    switch (results.getAnswer(0).toString()) {
                        case "create":
                            Questionnaire.newQuestionnaire(results)
                                    .addQuestion(event.getString("command.fun.myplaylist.create.intro"), QuestionType.STRING)
                                    .execute(results1 -> {
                                        String playlistName = results1.getAnswer(0).toString();
                                        if (profile.getPlaylists().stream().anyMatch(playlist -> playlist.getName().equalsIgnoreCase(playlistName))) {
                                            event.getChannel().sendMessage(CustomEmote.think() + " " + event.getString("command.fun.myplaylist.create.exists")).queue();
                                            results1.reExecuteWithoutMessage();
                                        } else if (playlistName.equalsIgnoreCase("exit")) {
                                            event.getChannel().sendMessage(event.getString("command.fun.myplaylist.create.invalidname")).queue();
                                            results1.reExecuteWithoutMessage();
                                        } else if (playlistName.length() > 50) {
                                            event.getChannel().sendMessage(event.getString("command.fun.myplaylist.create.toolong")).queue();
                                            results1.reExecuteWithoutMessage();
                                        } else {
                                            Playlist playlist = new Playlist(playlistName);
                                            Questionnaire.newQuestionnaire(results1)
                                                    .addQuestion(event.getString("command.fun.myplaylist.create.awaittracks", "`" + playlistName + "`")
                                                            + event.getString("command.fun.myplaylist.awaittracks", "`done`"), QuestionType.STRING)
                                                    .execute(results2 -> {
                                                        String URL = results2.getAnswer(0).toString();
                                                        if (!URL.equalsIgnoreCase("done")) {
                                                            KekBot.player.addToPlaylist(results2, URL, playlist);
                                                        } else {
                                                            if (playlist.getTracks().size() > 0) {
                                                                Questionnaire.newQuestionnaire(results2)
                                                                        .addYesNoQuestion(event.getString("command.fun.myplaylist.create.hidequestion"))
                                                                        .execute(results3 -> {
                                                                            if (results3.getAnswerAsType(0, boolean.class)) playlist.setHidden(true);
                                                                            profile.addPlaylist(playlist);
                                                                            profile.save();
                                                                            event.getChannel().sendMessage(event.getString("command.fun.myplaylist.create.success", "`" + event.getPrefix() + "music queue playlist " + playlist.getName() + "`")).queue();
                                                                        });

                                                            } else {
                                                                event.getChannel().sendMessage(event.getString("command.fun.myplaylist.create.empty")).queue();
                                                                results2.reExecuteWithoutMessage();
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                            break;
                        case "remove":
                            if (profile.getPlaylists().size() == 0) {
                                event.getChannel().sendMessage(event.getString("command.fun.myplaylist.noplaylists", "*create*")).queue();
                            } else {
                                Questionnaire.newQuestionnaire(results)
                                        .addQuestion(event.getString("command.fun.myplaylist.modify.intro", "*view*"), QuestionType.STRING)
                                        .includeCancel(false)
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
                                                        event.getChannel().sendMessage(event.getString("command.fun.myplaylist.invalidplaylist")).queue();
                                                    }
                                                    break;
                                            }
                                        });
                            }
                            break;
                        case "edit":
                            if (profile.getPlaylists().size() == 0) {
                                event.getChannel().sendMessage(event.getString("command.fun.myplaylist.noplaylists", "*create*")).queue();
                            } else {
                                Questionnaire.newQuestionnaire(results)
                                        .addQuestion(event.getString("command.fun.myplaylist.modify.intro", "*view*"), QuestionType.STRING)
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
                                                        event.getChannel().sendMessage(event.getString("command.fun.myplaylist.invalidplaylist")).queue();
                                                    }
                                                    break;
                                            }
                                        });
                            }
                            break;
                        case "view":
                            if (profile.getPlaylists().size() == 0) event.getChannel().sendMessage(event.getString("command.fun.myplaylist.noplaylists", "*create*")).queue();
                            else listPlaylists(results, 0, profile);
                            break;
                    }
                });
    }

    private void listPlaylists(Questionnaire.Results results, int mode, Profile profile) {
        Questionnaire.newQuestionnaire(results)
                .withoutRepeats()
                .addYesNoQuestion(LocaleUtils.getString("command.fun.myplaylist.view.intro", KekBot.getGuildLocale(results.getGuild())))
                .execute(results1 -> {
                    List<Playlist> playlists;
                    if (results1.getAnswerAsType(0, boolean.class)) {
                        playlists = profile.getPlaylists();
                    } else {
                        playlists = profile.getPlaylists().stream().filter(playlist -> !playlist.isHidden()).collect(Collectors.toList());
                    }
                    List<String> playlistNames = playlists.stream().map(playlist -> "`" + playlist.getName() + "` - (" + Utils.convertMillisToTime(playlist.getTotalLength()) + ")" + (playlist.isHidden() ? " ***(HIDDEN)***" : "")).collect(Collectors.toList());
                    switch (mode) {
                        case 0:
                            Paginator.Builder list = new Paginator.Builder();
                            list.addItems(playlistNames.toArray(new String[playlistNames.size()]));
                            list.setUsers(results.getUser());
                            list.setEventWaiter(KekBot.waiter);
                            list.waitOnSinglePage(true);
                            list.showPageNumbers(true);
                            list.setItemsPerPage(10);
                            list.setText(LocaleUtils.getString("command.fun.myplaylist.view.list", KekBot.getGuildLocale(results.getGuild())));
                            KekBot.getCommandClient().registerQuestionnaire(results.getChannel().getId(), results.getUser().getId());
                            list.setFinalAction(m -> {
                                KekBot.getCommandClient().unregisterQuestionnaire(results.getChannel().getId(), results.getUser().getId());
                                m.clearReactions().queue();
                            });
                            list.build().display(results.getChannel());
                            break;
                        case 1:
                        case 2:
                            PagedSelectionMenu.Builder dList = new PagedSelectionMenu.Builder();
                            dList.addChoices(playlistNames.toArray(new String[playlistNames.size()]));
                            dList.setUsers(results.getUser());
                            dList.setEventWaiter(KekBot.waiter);
                            dList.showPageNumbers(true);
                            dList.setText(LocaleUtils.getString("command.fun.myplaylist.view.list", KekBot.getGuildLocale(results.getGuild())));
                            KekBot.getCommandClient().registerQuestionnaire(results.getChannel().getId(), results.getUser().getId());
                            dList.setFinalAction(m -> {
                                KekBot.getCommandClient().unregisterQuestionnaire(results.getChannel().getId(), results.getUser().getId());
                                m.clearReactions().queue();
                            });
                            dList.setItemsPerPage(10);
                            dList.setSelectionAction((m, i) -> {
                                m.clearReactions().queue();
                                if (mode == 1) confirmDelete(results, playlists.get(i-1), profile);
                                else editPlaylist(results, playlists.get(i-1), profile);
                            });
                            dList.build().display(results.getChannel());
                    }
                });
    }

    private void confirmDelete(Questionnaire.Results results, Playlist playlist, Profile profile) {
        Questionnaire.newQuestionnaire(results)
                .addYesNoQuestion(LocaleUtils.getString("command.fun.myplaylist.remove.confirm", KekBot.getGuildLocale(results.getGuild()), "`" + playlist.getName() + "`"))
                .execute(results1 -> {
                    if (results1.getAnswerAsType(0, boolean.class)) {
                        profile.removePlaylist(playlist);
                        profile.save();
                        results1.getChannel().sendMessage(LocaleUtils.getString("command.fun.myplaylist.remove.success", KekBot.getGuildLocale(results.getGuild()))).queue();
                    } else results1.getChannel().sendMessage(LocaleUtils.getString("command.fun.myplaylist.remove.cancelled", KekBot.getGuildLocale(results.getGuild()))).queue();
                });
    }

    private void editPlaylist(Questionnaire.Results results, Playlist playlist, Profile profile) {
        List<Playlist.KAudioTrackInfo> tracks = playlist.getTracks();
        List<String> trackNames = new ArrayList<>();
        trackNames.add("Add new track");
        trackNames.addAll(tracks.stream().map(track -> "`" + track.title + "` - (" + Utils.convertMillisToTime(track.length) + ")").collect(Collectors.toList()));
        PagedSelectionMenu.Builder dList = new PagedSelectionMenu.Builder();
        dList.addChoices(trackNames.toArray(new String[trackNames.size()]));
        dList.setUsers(results.getUser());
        dList.setEventWaiter(KekBot.waiter);
        dList.showPageNumbers(true);
        dList.setText(LocaleUtils.getString("command.fun.myplaylist.edit.list", KekBot.getGuildLocale(results.getGuild())));
        KekBot.getCommandClient().registerQuestionnaire(results.getChannel().getId(), results.getUser().getId());
        dList.setFinalAction(m -> {
            KekBot.getCommandClient().unregisterQuestionnaire(results.getChannel().getId(), results.getUser().getId());
            m.clearReactions().queue();
        });
        dList.setItemsPerPage(10);
        dList.setSelectionAction((m, i) -> {
            m.clearReactions().queue();
            if (i-1 == 0) {
                Questionnaire.newQuestionnaire(results)
                        .addQuestion(LocaleUtils.getString("command.fun.myplaylist.edit.add", KekBot.getGuildLocale(results.getGuild()))
                                + LocaleUtils.getString("command.fun.myplaylist.awaittracks", KekBot.getGuildLocale(results.getGuild()), "`done`"), QuestionType.STRING)
                        .execute(results2 -> {
                            String URL = results2.getAnswer(0).toString();
                            if (!URL.equalsIgnoreCase("done")) {
                                KekBot.player.addToPlaylist(results2, URL, playlist);
                            } else {
                                profile.save();
                                Questionnaire.newQuestionnaire(results2)
                                        .addYesNoQuestion(LocaleUtils.getString("command.fun.myplaylist.edit.success", KekBot.getGuildLocale(results.getGuild())))
                                        .execute(results3 -> {
                                            if (results3.getAnswerAsType(0, boolean.class)) {
                                                editPlaylist(results3, playlist, profile);
                                            } else {
                                                results3.getChannel().sendMessage(LocaleUtils.getString("command.fun.myplaylist.exited", KekBot.getGuildLocale(results.getGuild()))).queue();
                                            }
                                        });
                            }
                        });
            } else {
                int trackNumber = i - 2;
                Playlist.KAudioTrackInfo track = playlist.getTracks().get(trackNumber);
                Questionnaire.newQuestionnaire(results)
                        .addYesNoQuestion(LocaleUtils.getString("command.fun.myplaylist.edit.remove.intro", KekBot.getGuildLocale(results.getGuild())))
                        .execute(results1 -> {
                            if (results1.getAnswerAsType(0, boolean.class)) {
                                playlist.removeTrack(track);
                                profile.save();
                                if (tracks.size() > 0) {
                                    Questionnaire.newQuestionnaire(results1)
                                            .addYesNoQuestion(LocaleUtils.getString("command.fun.myplaylist.edit.remove.syccess", KekBot.getGuildLocale(results.getGuild())))
                                            .execute(results2 -> {
                                                if (results2.getAnswerAsType(0, boolean.class)) {
                                                    editPlaylist(results2, playlist, profile);
                                                } else {
                                                    results2.getChannel().sendMessage(LocaleUtils.getString("command.fun.myplaylist.exited", KekBot.getGuildLocale(results.getGuild()))).queue();
                                                }
                                            });
                                } else {
                                    profile.removePlaylist(playlist);
                                    profile.save();
                                    results1.getChannel().sendMessage(LocaleUtils.getString("command.fun.myplaylist.edit.remove.empty", KekBot.getGuildLocale(results.getGuild()))).queue();
                                }
                            } else {
                                Questionnaire.newQuestionnaire(results1)
                                        .addYesNoQuestion(LocaleUtils.getString("command.fun.myplaylist.edit.remove.cancelled", KekBot.getGuildLocale(results.getGuild())))
                                        .execute(results2 -> {
                                            if (results2.getAnswerAsType(0, boolean.class)) {
                                                editPlaylist(results2, playlist, profile);
                                            } else {
                                                results2.getChannel().sendMessage(LocaleUtils.getString("command.fun.myplaylist.exited", KekBot.getGuildLocale(results.getGuild()))).queue();
                                            }
                                        });
                            }
                        });
            }
        });
        dList.build().display(results.getChannel());
    }
}
