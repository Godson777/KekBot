package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.util.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.settings.Config;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class Daily extends Command {

    private Random random = new Random();

    public Daily() {
        name = "daily";
        description = "Claims your daily paycheck.";
        usage.add("daily");
        category = new Category("Fun");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        Profile profile = Profile.getProfile(event.getAuthor());
        if (profile.getDaily() != null && profile.getDaily().isAfter(event.getMessage().getTimeCreated().toInstant())) {
            event.getChannel().sendMessage(event.getString("command.fun.daily.alreadyclaimed", Utils.convertMillisToTime(event.getMessage().getTimeCreated().toInstant().until(profile.getDaily(), ChronoUnit.MILLIS)))).queue();
            return;
        }
        //Temporarily outdated, will remake the daily bonus feature in a later update.
        /*if (Config.getConfig().getdBotsListToken() != null) {
            try {
                Document document = Jsoup.connect("https://discordbots.org/api/bots/213151748855037953/check?userId=" + event.getAuthor().getId())
                        .userAgent("Mozilla/5.0").ignoreContentType(true)
                        .header("Authorization", Config.getConfig().getdBotsListToken())
                        .get();
                JsonParser parser = new JsonParser();
                int voted = parser.parse(document.body().text()).getAsJsonObject().get("voted").getAsInt();


                if (voted == 1) {
                    claimDaily(true, profile, event);
                } else {
                    Questionnaire.newQuestionnaire(event)
                            .addYesNoQuestion(event.getString("command.fun.daily.notvoted", "https://discordbots.org/bot/213151748855037953"))
                            .execute(results -> {
                                if (!results.getAnswerAsType(0, boolean.class)) {
                                    event.getChannel().sendMessage(event.getString("command.fun.daily.vote")).queue();
                                    return;
                                }

                                claimDaily(false, profile, event);
                            });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            claimDaily(false, profile, event);
        }*/
        claimDaily(false, profile, event);
    }

    private void claimDaily(boolean voted, Profile profile, CommandEvent event) {
        profile.setDaily(event.getMessage().getTimeCreated().plusDays(1));
        int reward = random.nextInt(20) * (voted ? 2 : 1);
        if (reward != 0) profile.addTopKeks(reward);
        else profile.addKXP(10);
        profile.save();
        event.getChannel().sendMessage(event.getString("command.fun.daily.claim", voted ? reward/2 + " ***x2!*** (" + CustomEmote.printPrice(reward) + ")" : CustomEmote.printPrice(reward)) +
                (reward == 0 ? "\n\n" + event.getString("command.fun.daily.claimxp") : "")).queue();
    }
}
