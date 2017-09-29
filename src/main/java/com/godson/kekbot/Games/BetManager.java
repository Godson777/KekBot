package com.godson.kekbot.Games;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.Profile.Profile;
import javafx.util.Pair;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BetManager {
    private Map<User, Pair<Integer, Double>> spectators = new HashMap<>();
    private Map<User, Double> players = new HashMap<>();
    private final double spectatorMultiplier = 1.35;
    private double playerPot;
    private boolean spectatorsEnabled;
    private boolean playersEnabled;

    public BetManager(boolean playersEnabled, boolean spectatorsEnabled) {
        this.playersEnabled = playersEnabled;
        this.spectatorsEnabled = spectatorsEnabled;
    }

    public String addPlayerBet(User user, double bet) {
        if (playersEnabled) {
            if (!players.containsKey(user)) {
                players.put(user, bet);
                return "Your bet has been added to the pot.";
            } else {
                if (bet > players.get(user)) {
                    players.replace(user, bet);
                    return "Your bet was increased.";
                } else return "You cannot lower your bet.";
            }
        } else return "This game does not support player bets.";
    }

    public String addSpectatorBet(User user, int player, double bet) {
        if (spectatorsEnabled) {
            if (!spectators.containsKey(user)) {
                spectators.put(user, new Pair<>(player, bet));
                return "Your bet has been accepted.";
            } else return "You cannot edit your bet once it's been made.";
        } else return "This game does not support spectator bets.";
    }

    public Double declareWinners(Game game, List<Integer> winnerIDs) {
        //Give each spectator their earnings.
        spectators.forEach((user, bet) -> {
            if (winnerIDs.contains(bet.getKey())) {
                double earnings = bet.getValue() * spectatorMultiplier;
                Profile profile = Profile.getProfile(user);
                profile.addTopKeks(earnings);
                user.openPrivateChannel().queue(c -> c.sendMessage("Your bet in " + game.getChannel().getAsMention() + " for " + game.players.get(bet.getKey() - 1) + " paid off! You've been given " + CustomEmote.printPrice(earnings) + " from your bet!").queue());
                profile.save();
            }
        });

        //Return the pot's contents to go to the winning player.
        double temp = playerPot;
        playerPot = 0;
        return temp;
    }

    public void declareTie() {
        spectators.forEach((user, bet) -> {
            Profile profile = Profile.getProfile(user);
            profile.addTopKeks(bet.getValue());
            profile.save();
        });
        players.forEach((user, bet) -> {
            Profile profile = Profile.getProfile(user);
            profile.addTopKeks(bet);
            profile.save();
        });
        playerPot = 0;
    }

    public boolean hasPlayerBets() {
        return players.size() > 0;
    }
}
