package com.godson.kekbot.objects;

import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PollManager {
    private Map<Guild, PollObject> polls = new HashMap<>();
    private Map<Guild, TimerTask> pollTriggers = new HashMap<>();
    private Timer timer = new Timer();

    public PollManager() {}

    public PollObject createPoll(CommandEvent event, long time, String title, String... options) {
        Guild guild = event.getGuild();
        User creator = event.getAuthor();
        MessageChannel channel = event.getChannel();
        if (!polls.containsKey(guild)) {
            PollObject poll = new PollObject(title, creator).withOptions(options);
            polls.put(guild, poll);

            TimerTask pollResults = new TimerTask() {
                @Override
                public void run() {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < poll.getOptions().length; i++) {
                        builder.append("**").append(poll.getOptions()[i]).append(":** ").append(poll.getVotes()[i]).append("\n");
                    }
                    channel.sendMessage("Time's up! Let's see the results...\n\n" + builder.toString()).queue();
                    polls.remove(guild);
                    pollTriggers.remove(guild);
                }
            };
            timer.schedule(pollResults, time);
            pollTriggers.put(guild, pollResults);
            return poll;

        } else throw new IllegalArgumentException("Attempted to create a poll in a guild which already contains an ongoing poll!");
    }

    public void interruptPoll(Guild guild) {
        if (guildHasPoll(guild)) {
            pollTriggers.get(guild).cancel();
            pollTriggers.get(guild).run();
            pollTriggers.remove(guild);
            polls.remove(guild);
        }
    }

    public void cancelPoll(Guild guild) {
        if (guildHasPoll(guild)) {
            pollTriggers.get(guild).cancel();
            pollTriggers.remove(guild);
            polls.remove(guild);
        }
    }

    public boolean guildHasPoll(Guild guild) {
        return polls.containsKey(guild);
    }

    public PollObject getGuildsPoll(Guild guild) {
        return polls.getOrDefault(guild, null);
    }
}
