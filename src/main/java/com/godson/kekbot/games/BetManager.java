package com.godson.kekbot.games;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.LocaleUtils;
import com.godson.kekbot.profile.Profile;
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

    public String addPlayerBet(User user, double bet, String locale) {
        Profile profile = Profile.getProfile(user);
        if (playersEnabled) {
            if (!players.containsKey(user)) {
                if (!profile.canSpend(bet)) {
                    return LocaleUtils.getString("game.bet.notenoughfunds", locale, CustomEmote.printTopKek());
                }
                profile.spendTopKeks(bet);
                profile.save();
                players.put(user, bet);
                playerPot += bet;
                return LocaleUtils.getString("game.bet.player.success", locale);
            } else {
                if (bet > players.get(user)) {
                    if (!profile.canSpend(bet - players.get(user))) {
                        return LocaleUtils.getString("game.bet.notenoughfunds", locale, CustomEmote.printTopKek());
                    }
                    profile.spendTopKeks(bet - players.get(user));
                    profile.save();
                    playerPot += (bet - players.get(user));
                    players.replace(user, bet);
                    return LocaleUtils.getString("game.bet.player.increased", locale);
                } else return LocaleUtils.getString("game.bet.player.decrease", locale);
            }
        } else return LocaleUtils.getString("game.bet.player.error", locale);
    }

    public String addSpectatorBet(User user, int player, double bet, String locale) {
        Profile profile = Profile.getProfile(user);
        if (spectatorsEnabled) {
            if (!spectators.containsKey(user)) {
                if (!profile.canSpend(bet)) {
                    return LocaleUtils.getString("game.bet.notenoughfunds", locale, CustomEmote.printTopKek());
                }
                profile.spendTopKeks(bet);
                profile.save();
                spectators.put(user, new Pair<>(player, bet));
                return LocaleUtils.getString("game.bet.spectator.success", locale);
            } else return LocaleUtils.getString("game.bet.spectator.existing", locale);
        } else return LocaleUtils.getString("game.bet.spectator.error", locale);
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
