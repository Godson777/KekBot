package com.godson.kekbot.Settings;

import com.darichey.discord.api.CommandContext;
import com.godson.kekbot.Objects.PollObject;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PollManager {
    private Map<Guild, PollObject> polls = new HashMap<>();
    private Timer timer = new Timer();

    public PollManager() {}

    public PollObject createPoll(CommandContext context, long time, String title, String... options) {
        Guild guild = context.getGuild();
        User creator = context.getAuthor();
        TextChannel channel = context.getTextChannel();
        if (!polls.containsKey(guild)) {
            PollObject poll = new PollObject(title, creator).withOptions(options);
            polls.put(guild, poll);

            TimerTask pollResults = new TimerTask() {
                @Override
                public void run() {
                    channel.sendMessage("Time's up! Let's see the results...").queue(msg -> {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < poll.getOptions().length; i++) {
                            builder.append("**").append(poll.getOptions()[i]).append(":** ").append(poll.getVotes()[i]).append("\n");
                        }
                        msg.editMessage("Time's up! Let's see the results...\n\n" + builder.toString()).queue();
                    });
                    polls.remove(guild);
                }
            };

            timer.schedule(pollResults, time);
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
