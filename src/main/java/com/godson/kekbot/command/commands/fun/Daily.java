package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.profile.Profile;

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
        if (profile.getDaily() != null && profile.getDaily().isAfter(event.getMessage().getCreationTime().toInstant())) {
            event.getChannel().sendMessage(event.getString("command.fun.daily.alreadyclaimed", Utils.convertMillisToTime(event.getMessage().getCreationTime().toInstant().until(profile.getDaily(), ChronoUnit.MILLIS)))).queue();
            return;
        }

        profile.setDaily(event.getMessage().getCreationTime().plusDays(1));
        int reward = random.nextInt(20);
        if (reward != 0) profile.addTopKeks(reward);
        else profile.addKXP(10);
        profile.save();
        event.getChannel().sendMessage(event.getString("command.fun.daily.claim", CustomEmote.printPrice(reward)) +
                (reward == 0 ? "\n\n" + event.getString("command.fun.daily.claimxp") : "" )).queue();
    }
}
