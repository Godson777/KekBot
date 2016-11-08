package com.godson.kekbot.Objects;

import net.dv8tion.jda.entities.User;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PollObject {
    private User creator;
    private String title;
    private String[] options;
    private int[] votes;
    private Map<User, Integer> castedVotes = new HashMap<>();

    public PollObject(String title, User creator) {
        this.title = title;
        this.creator = creator;
    }

    public PollObject withOptions(String... options) {
        this.options = options;
        this.votes = new int[options.length];
        return this;
    }

    public void castVote(int option, User user) {
        if (!castedVotes.containsKey(user)) {
            votes[option] += 1;
            castedVotes.put(user, option);
        } else {
            if (castedVotes.get(user).equals(option)) throw new IllegalArgumentException("User attempted to vote for an option they already voted for!");
            else {
                votes[castedVotes.get(user)] -= 1;
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
