package com.godson.kekbot.objects;

import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PollManager {
    private Map<Guild, Poll> polls = new HashMap<>();
    private Map<Guild, TimerTask> pollTriggers = new HashMap<>();
    private Timer timer = new Timer();

    public PollManager() {}

    public Poll createPoll(CommandEvent event, long time, String title, String... options) {
        Guild guild = event.getGuild();
        User creator = event.getAuthor();
        MessageChannel channel = event.getChannel();
        if (!polls.containsKey(guild)) {
            Poll poll = new Poll(title, creator).withOptions(options);
            polls.put(guild, poll);

            TimerTask pollResults = new TimerTask() {
                @Override
                public void run() {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < poll.getOptions().length; i++) {
                        builder.append("**").append(poll.getOptions()[i]).append(":** ").append(poll.getVotes()[i]).append("\n");
                    }
                    channel.sendMessage(event.getString("poll.finished") + "\n\n" + builder.toString()).queue();
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

    public Poll getGuildsPoll(Guild guild) {
        return polls.getOrDefault(guild, null);
    }

    public static class Poll {
        private User creator;
        private String title;
        private String[] options;
        private int[] votes;
        private Map<User, Integer> castedVotes = new HashMap<>();

        public Poll(String title, User creator) {
            this.title = title;
            this.creator = creator;
        }

        public Poll withOptions(String... options) {
            this.options = options;
            this.votes = new int[options.length];
            return this;
        }

        public void castVote(int option, User user) {
            if (!castedVotes.containsKey(user)) {
                ++votes[option];
                castedVotes.put(user, option);
            } else {
                if (castedVotes.get(user).equals(option)) throw new IllegalArgumentException("User attempted to vote for an option they already voted for!");
                else {
                    --votes[castedVotes.get(user)];
                    ++votes[option];
                    castedVotes.replace(user, option);
                }
            }
        }

        public int[] getVotes() {
            return votes;
        }

        public String getTitle() {
            return title;
        }

        public String[] getOptions() {
            return options;
        }

        public User getCreator() {
            return creator;
        }
    }
}
