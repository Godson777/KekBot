package com.godson.kekbot.command;

import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.KekBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;

import java.util.HashMap;
import java.util.Map;

public class UserStates {

    private Map<String, Map<Guild, UserState>> assignedUsers = new HashMap<>();

    public UserStates() {}

    public void setUserState(User user, Guild guild, UserState state) {
        String userID = user.getId();
        if (!assignedUsers.containsKey(userID)) {
            assignedUsers.put(userID, new HashMap<>());
        }

        Map<Guild, UserState> assignedGuilds = assignedUsers.get(userID);

        if (!assignedGuilds.containsKey(guild)) {
            assignedGuilds.put(guild, state);
            assignedUsers.replace(userID, assignedGuilds);
            CommandRegistry.getForClient(KekBot.client).disableUserInGuild(guild, user);
        } else {
            throw new IllegalArgumentException("Attempted to send user into a new state while already being in another state!");
        }
    }

    public void unsetUserState(User user, Guild guild) {
        String userID = user.getId();
        if (assignedUsers.containsKey(userID)) {
            if (assignedUsers.get(userID).containsKey(guild)) {
                assignedUsers.get(userID).remove(guild);
                CommandRegistry.getForClient(KekBot.client).enableUserInGuild(guild, user);
            }
        }
    }

    public void changeUserState(User user, Guild guild, UserState newState) {
        unsetUserState(user, guild);
        setUserState(user, guild, newState);
    }

    public UserState checkUserState(User user, Guild guild) {
        String userID = user.getId();
        if (assignedUsers.containsKey(userID)) {
            if (assignedUsers.get(userID).containsKey(guild)) {
                return assignedUsers.get(userID).get(guild);
            }
            else return null;
        }
        else return null;
    }

}

