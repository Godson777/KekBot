package com.godson.kekbot.Settings;

import com.godson.kekbot.Objects.PollObject;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class PollManager {
    private Map<Guild, PollObject> polls = new HashMap<>();
    private Timer timer = new Timer();

    public PollManager() {}

    public PollObject createPoll(Guild guild, TextChannel channel, String title, User creator, String... options) {
        if (!polls.containsKey(guild)) {
            PollObject poll = new PollObject(title, creator).withOptions(options);
            polls.put(guild, poll);

            TimerTask pollResults = new TimerTask() {
                @Override
                public void run() {
                    channel.sendMessageAsync("Time's up! Let's see the results...", msg -> {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < poll.getOptions().length; i++) {
                            builder.append("**").append(poll.getOptions()[i]).append(":** ").append(poll.getVotes()[i]).append("\n");
                        }
                        msg.updateMessage("Time's up! Let's see the results...\n\n" + builder.toString());
                    });
                    polls.remove(guild);
                }
            };

            timer.schedule(pollResults, TimeUnit.SECONDS.toMillis(20));
            return poll;

        } else throw new IllegalArgumentException("Attempted to create a poll in a guild which already contains an ongoing poll!");
    }

    public boolean guildHasPoll(Guild guild) {
        return polls.containsKey(guild);
    }

    public PollObject getGuildsPoll(Guild guild) {
        if (polls.containsKey(guild)) return polls.get(guild);
        else return null;
    }
}
