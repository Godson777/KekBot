package com.godson.kekbot.games;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.util.LocaleUtils;
import com.godson.kekbot.profile.Profile;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Game {
    private String gameName;
    private int minNumberOfPlayers = 0;
    private int maxNumberOfPlayers;
    private boolean hasAI;
    private boolean isReady = false;
    private boolean reachedMinimum = false;
    boolean isTranslatable = true;
    boolean canQuit = true;
    private boolean betsEnabled;
    public List<User> players = new ArrayList<>();
    private List<Integer> winnerIDs = new ArrayList<>();
    private Map<User, Integer> playerNumber = new HashMap<>();
    public TextChannel channel;
    private BetManager bets;
    //This'll only be used in games that require the multiplier. See Snail Race and Russian Roulette as good examples.
    double multiplier = 1;

    public Game(int minNumberOfPlayers, int maxNumberOfPlayers, boolean hasAI, TextChannel channel, String gameName, boolean betsEnabled) {
        this.minNumberOfPlayers = minNumberOfPlayers;
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.hasAI = hasAI;
        this.channel = channel;
        this.gameName = gameName;
        this.betsEnabled = betsEnabled;
        bets = new BetManager(betsEnabled, betsEnabled);
    }

    public Game(int minNumberOfPlayers, int maxNumberOfPlayers, boolean hasAI, TextChannel channel, String gameName, boolean playerBetsEnabled, boolean spectatorBetsEnabled) {
        this.minNumberOfPlayers = minNumberOfPlayers;
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.hasAI = hasAI;
        this.channel = channel;
        this.gameName = gameName;
        if (playerBetsEnabled || spectatorBetsEnabled) betsEnabled = true;
        bets = new BetManager(playerBetsEnabled, spectatorBetsEnabled);
    }

    public Game(int maxNumberOfPlayers, boolean hasAI, TextChannel channel, String gameName, boolean betsEnabled) {
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.hasAI = hasAI;
        this.channel = channel;
        this.gameName = gameName;
        this.betsEnabled = betsEnabled;
        bets = new BetManager(betsEnabled, betsEnabled);
    }

    public Game(int maxNumberOfPlayers, boolean hasAI, TextChannel channel, String gameName, boolean playerBetsEnabled, boolean spectatorBetsEnabled) {
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.hasAI = hasAI;
        this.channel = channel;
        this.gameName = gameName;
        if (playerBetsEnabled || spectatorBetsEnabled) betsEnabled = true;
        bets = new BetManager(playerBetsEnabled, spectatorBetsEnabled);
    }

    public abstract void startGame();

    public void acceptInputFromMessage(Message message) {
        //This is left intentionally blank, and is only used if a game requires input.
    }

    public abstract String getRules();

    /**
     * Adds a player into the game.
     * @param player The user to add.
     */
    public void addPlayer(User player) {
        if (players.size() < maxNumberOfPlayers) {
            players.add(player);
            playerNumber.put(player, players.size());
            if (players.size() > minNumberOfPlayers) reachedMinimum = true;
        }
    }

    public void removePlayer(User player) {
        if (players.contains(player)) {
            int position = getPlayerNumber(player)-1;
            players.remove(player);
            playerNumber.remove(player);
            for (int i = position; i < players.size(); i++) {
                playerNumber.replace(players.get(i), i+1);
            }
            if (players.size() < minNumberOfPlayers) reachedMinimum = false;
        } else throw new NullPointerException("Player not in game.");
    }

    public int getPlayerNumber(User player) {
        if (players.contains(player)) {
            return playerNumber.get(player);
        } else {
            return 0;
        }
    }

    public void endGame(User winner) {
        for (User player : players) {
            Profile profile = Profile.getProfile(player);
            if (player.equals(winner)) {
                //profile.wonGame();
            } else {
                //profile.lostGame();
            }
            profile.save();
        }
        KekBot.gamesManager.closeGame(channel);
    }

    public void endGame(User winner, double topkeks, int KXP) {
        StringBuilder builder = new StringBuilder();
        for (User player : players) {
            Profile profile = Profile.getProfile(player);
            if (player.equals(winner)) {
                if (!betsEnabled) {
                    profile.wonGame(Precision.round(topkeks * multiplier, 2), KXP);
                    if (topkeks > 0 && KXP > 0) builder.append(stateEarnings(winner, topkeks, KXP, (multiplier > 1 ? new Bonus(Precision.round(topkeks * (multiplier - 1), 2), multiplier + "x Multiplier") : null))).append("\n");
                } else {
                    double betEarnings = bets.declareWinners(this, winnerIDs);
                    profile.wonGame(Precision.round((topkeks + betEarnings) * multiplier, 2), KXP);
                    //if (bets.hasPlayerBets() || multiplier > 1) builder.append(stateEarnings(winner, topkeks, KXP, )).append("\n");
                    builder.append(stateEarnings(winner, topkeks * multiplier, KXP, (bets.hasPlayerBets() ? new Bonus(betEarnings, "Won Bet") : null), (multiplier > 1 ? new Bonus(Precision.round(topkeks * (multiplier - 1), 2), multiplier + "x Multiplier") : null))).append("\n");
                }
                profile.save();
            } else {
                //Do nothing for now. This will be changed later.
                //profile.lostGame();
                //profile.save();
            }
        }
        channel.sendMessage(builder.toString()).queue();
        KekBot.gamesManager.closeGame(channel);
    }

    /**
     * Ends the game, and gives the appropriate earnings to the winner(s).
     * This is called whenever a game ends with more than one winner. This usually applies in games that allow more than one winner, like Snail Race.
     * @param winners The list of users who won the game.
     * @param baseTopkeks The base amount of topkeks to give.
     * @param baseKXP The base amount of KXP to give.
     */
    public void endGame(List<User> winners, double baseTopkeks, int baseKXP) {
        StringBuilder builder = new StringBuilder();
        //Setting this up for the "lose count" later.
        for (User player : players) {
            //Get player's profile.
            if (winners.contains(player)) break;
            else ; //lose++
        }
        for (int i = 0; i < winners.size(); i++) {
            Profile profile = Profile.getProfile(winners.get(i));
            double topkeks = baseTopkeks + (players.size() - i);
            int KXP = baseKXP + (players.size() - i);
            if (winners.get(i).equals(winners.get(0))) {
                if (!betsEnabled) {
                    profile.wonGame(Precision.round(topkeks * multiplier, 2), KXP);
                    if (!(topkeks == 0 && KXP == 0)) builder.append(stateEarnings(winners.get(i), topkeks, KXP, (multiplier > 1 ? new Bonus(Precision.round(topkeks * (multiplier - 1), 2), multiplier + "x Multiplier") : null))).append("\n");
                } else {
                    double betEarnings = bets.declareWinners(this, winnerIDs);
                    profile.wonGame(Precision.round((baseTopkeks * multiplier) + (players.size() - i) + betEarnings, 2), KXP);
                    //if (bets.hasPlayerBets()) builder.append(stateEarnings(winners.get(i), topkeks, KXP, betEarnings, "Won Bet")).append("\n");
                    //else builder.append(stateEarnings(winners.get(i), topkeks, KXP)).append("\n");
                    builder.append(stateEarnings(winners.get(i), topkeks, KXP, (bets.hasPlayerBets() ? new Bonus(betEarnings, "Won Bet") : null), (multiplier > 1 ? new Bonus(Precision.round(topkeks * (multiplier - 1), 2), multiplier + "x Multiplier") : null))).append("\n");

                }
            } else {
                profile.wonGame(Precision.round(topkeks * multiplier, 2), KXP);
                builder.append(stateEarnings(winners.get(i), topkeks, KXP, (multiplier > 1 ? new Bonus(Precision.round(topkeks * (multiplier - 1), 2), multiplier + "x Multiplier") : null))).append("\n");
            }
            profile.save();
        }
        if (!builder.toString().isEmpty()) channel.sendMessage(builder.toString()).queue();
        KekBot.gamesManager.closeGame(channel);
    }

    public void endTie() {
        endTie(0, 0);
    }

    public void endTie(double topkeks, int KXP) {
        StringBuilder builder = new StringBuilder();
        if (topkeks > 0 && KXP > 0) {
            for (User player : players) {
                Profile profile = Profile.getProfile(player);
                profile.tieGame(Precision.round(topkeks * multiplier, 2), KXP);
                builder.append(stateEarnings(player, topkeks, KXP, (multiplier > 1 ? new Bonus(Precision.round(topkeks * (multiplier - 1), 2), multiplier + "x Multiplier") : null))).append("\n");
                profile.save();
            }
            channel.sendMessage(builder.toString()).queue();
        }
        if (betsEnabled) bets.declareTie();
        KekBot.gamesManager.closeGame(channel);
    }

    protected String stateEarnings(User user, double topkeks, int KXP) {
        return user.getAsMention() + ", you've earned " +
                (topkeks > 0 ? CustomEmote.printPrice(topkeks) : "") +
                (topkeks > 0 && KXP > 0 ? ", and " : "") +
                (KXP > 0 ? KXP + " KXP" : "") +
                (KXP <= 0 && topkeks <= 0 ? "nothing" : "") + "!";
    }

    private String stateEarnings(User user, double topkeks, int KXP, Bonus... bonuses) {
        StringBuilder builder = new StringBuilder();
        double total = 0;
        builder.append(user.getAsMention() + ", you've earned " +
                (topkeks > 0 ? CustomEmote.printPrice(topkeks) : "") +
                (topkeks > 0 && KXP > 0 ? ", and " : "") +
                (KXP > 0 ? KXP + " KXP" : "") +
                (KXP <= 0 && topkeks <= 0 ? "nothing" : "") + "!");
        for (Bonus bonus : bonuses) {
            if (bonus == null) continue;
            builder.append(" (" + bonus.reason + "! +" + CustomEmote.printPrice(bonus.amount) + ") ");
            total += bonus.amount;
        }
        if (total > 0) builder.append("\nTotal Earnings: " + CustomEmote.printPrice(Precision.round(topkeks + total, 2)));

        return builder.toString();
    }

    public void endGame() {
        KekBot.gamesManager.closeGame(channel);
    }

    public TextChannel getChannel() {
        return channel;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean canQuit() {
        return canQuit;
    }

    public void ready() {
        if (hasMinimum()) {
            if (players.size() >= minNumberOfPlayers) {
                startGame();
                isReady = true;
            } else {
                if (hasAI) {
                    startGame();
                    isReady = true;
                }
            }
        } else {
            if (players.size() == maxNumberOfPlayers) {
                startGame();
                isReady = true;
            } else {
                if (hasAI) {
                    startGame();
                    isReady = true;
                }
            }
        }
    }

    public boolean hasRoomForPlayers() {
        return maxNumberOfPlayers > players.size();
    }

    public boolean reachedMinimumPlayers() {
        return reachedMinimum;
    }

    public boolean hasMinimumPlayers() {
        return players.size() >= minNumberOfPlayers;
    }

    public void minimumReached() {
        reachedMinimum = true;
    }

    public boolean hasMinimum() {
        return minNumberOfPlayers != 0;
    }

    public boolean hasAI() {
        return hasAI;
    }

    public boolean areBetsAllowed() {
        return betsEnabled;
    }

    public BetManager getBets() {
        return bets;
    }

    public void addWinner(User winner) {
        winnerIDs.add(getPlayerNumber(winner));
    }

    public List<Integer> getWinnerIDs() {
        return winnerIDs;
    }

    public String getGameName() {
        return gameName;
    }

    public int getMaxNumberOfPlayers() {
        return maxNumberOfPlayers;
    }

    public int getMinNumberOfPlayers() {
        return minNumberOfPlayers;
    }

    public boolean isTranslatable() {
        return isTranslatable;
    }

    public String getString(String unlocalizedMessage, Object... objects) {
        return LocaleUtils.getString(unlocalizedMessage, KekBot.getCommandClient().getLocale(channel.getGuild().getId()), objects);
    }

    protected class Bonus {
        private double amount;
        private String reason;

        Bonus(double amount, String reason) {
            this.amount = amount;
            this.reason = reason;
        }
    }
}
