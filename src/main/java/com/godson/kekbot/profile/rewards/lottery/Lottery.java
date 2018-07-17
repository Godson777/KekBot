package com.godson.kekbot.profile.rewards.lottery;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.util.LocaleUtils;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.util.Utils;
import javafx.util.Pair;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Lottery {
    private int ticketPrice = 10;
    private int ticketMax = 10;
    private int pot = 0;
    private int ticketsCount = 0;
    private long lengthOfLottery = TimeUnit.HOURS.toMillis(4);
    private List<LotteryTicket> ballot = new ArrayList<>();
    private List<Pair<User, Integer>> winners = new ArrayList<>();

    private Random random = new Random();
    private Timer timer = new Timer();
    private TimerTask draw;
    private long nextExecutionTime;

    public Lottery() {
        ballot.add(new LotteryTicket());
        initializeTimer();
    }

    private void initializeTimer() {
        draw = new TimerTask() {
            @Override
            public void run() {
                draw();
            }
        };
        nextExecutionTime = System.currentTimeMillis() + lengthOfLottery;
        timer.scheduleAtFixedRate(draw, lengthOfLottery, lengthOfLottery);
    }

    private boolean canUserPurchaseTicket(User gambler) {
        return ballot.stream().filter(ticket -> !ticket.isJackpot() && ticket.getGambler().equals(gambler)).collect(Collectors.toList()).size() < ticketMax;
    }

    private int getOwnedTickets(User gambler) {
        return this.ballot.stream().filter(ticket -> !ticket.isJackpot() && ticket.getGambler().equals(gambler)).collect(Collectors.toList()).size();
    }

    public String printStats(User gambler, Guild guild) {
        String locale = KekBot.getCommandClient().getLocale(guild.getId());
        return LocaleUtils.getString("lottery.stats", locale, "**" + Utils.convertMillisToTime(nextExecutionTime - System.currentTimeMillis(), locale) + "**",
                "**" + CustomEmote.printPrice(ticketPrice) + "**", "`" + KekBot.getCommandClient().getPrefix(guild.getId()) + "lottery buy`", "**" + CustomEmote.printPrice(pot) + "**",
                "**" + getOwnedTickets(gambler), LocaleUtils.getPluralString(getOwnedTickets(gambler), "amount.tickets", locale) + "**");
    }

    public String addTicket(User gambler, String locale) throws IllegalArgumentException {
        if (!canUserPurchaseTicket(gambler))
            return LocaleUtils.getString("lottery.maxpurchased", locale);

        Profile profile = Profile.getProfile(gambler);
        if (profile.getTopkeks() >= ticketPrice) {
            ballot.add(new LotteryTicket(gambler));
            profile.spendTopKeks(ticketPrice);
            profile.save();
            pot += ticketPrice;
            ticketsCount++;
            return LocaleUtils.getString("lottery.purchasesuccess", locale, 1, LocaleUtils.getString("amount.tickets.single", locale), CustomEmote.printPrice(ticketPrice));
        } else return LocaleUtils.getString("lottery.nofunds", CustomEmote.printPrice(ticketPrice), CustomEmote.printPrice(profile.getTopkeks()));
    }

    public String addTicket(User gambler, int tickets, String locale) throws IllegalArgumentException {
        int ownedTickets = getOwnedTickets(gambler);
        if (tickets > ticketMax - ownedTickets)
            tickets = ticketMax - ownedTickets;

        if (!canUserPurchaseTicket(gambler) || tickets == 0)
            return LocaleUtils.getString("lottery.maxpurchased", locale);

        Profile profile = Profile.getProfile(gambler);
        if (profile.getTopkeks() >= ticketPrice*tickets) {
            for (int i = 0; i < tickets; i++) ballot.add(new LotteryTicket(gambler));
            profile.spendTopKeks(ticketPrice*tickets);
            profile.save();
            pot += ticketPrice*tickets;
            ticketsCount += tickets;
            if (tickets == 1) return LocaleUtils.getString("lottery.purchasesuccess", locale, 1, LocaleUtils.getString("amount.tickets.single", locale), CustomEmote.printPrice(ticketPrice));
            else return LocaleUtils.getString("lottery.purchasesuccess", locale, tickets, LocaleUtils.getPluralString(tickets, "amount.tickets", locale), CustomEmote.printPrice(ticketPrice*tickets));
        } else return LocaleUtils.getString("lottery.nofundsmass", locale, tickets, CustomEmote.printPrice(ticketPrice), tickets, CustomEmote.printPrice(ticketPrice*tickets), CustomEmote.printPrice(profile.getTopkeks()));
    }

    private void draw() {
        draw(false, false);
    }

    private void draw(boolean forceDraw, boolean forceJackpot) {
        if (ballot.size() - 1 > 0) {
            LotteryTicket ticket;
            if (forceJackpot) ticket = ballot.get(0);
            else ticket = ballot.get(random.nextInt(ballot.size()));
            if (ticket.isJackpot()) {
                Set<User> users = ballot.stream().filter(lotteryTicket -> !lotteryTicket.isJackpot()).map(LotteryTicket::getGambler).collect(Collectors.toSet());

                String announcement = (forceDraw ? "Woah! The lottery was drawn early!\n\n" : "") + "***JACKPOT!*** All the money spent towards purchasing tickets has gone *back* into the pot for another round! This means that everyone can go back and buy more tickets, in an attempt to score a larger reward!" +
                        "\nThere were a total of " + (users.size()) + " users buying a total of " + ticketsCount + (ticketsCount > 1 ? " tickets." : " ticket.");

                users.forEach(user -> user.openPrivateChannel().queue(c -> c.sendMessage(announcement).queue()));
                cleanLottery();
                return;
            }
            User winner = ticket.getGambler();

            Profile profile = Profile.getProfile(winner);
            profile.addTopKeks(pot);
            profile.save();

            Set<User> losers = ballot.stream().filter(lotteryTicket -> !lotteryTicket.isJackpot() && !lotteryTicket.getGambler().equals(winner)).map(LotteryTicket::getGambler).collect(Collectors.toSet());
            String announcement = (forceDraw ? "Woah! The lottery was drawn early!\n\n" : "") + "With " + getOwnedTickets(winner) + (getOwnedTickets(winner) > 1 ? " tickets, " : " ticket, ") + winner.getName() + "#" + winner.getDiscriminator() + " has won this round of the lottery. " +
                    "Earning a total of " + CustomEmote.printPrice(pot) + " from the pot." +
                    "\nThere were a total of " + (losers.size() + 1) + " " + (losers.size() + 1 == 1 ? "users" : "user") + " buying a total of " + ticketsCount + (ticketsCount > 1 ? " tickets." : " ticket.");

            losers.forEach(user -> user.openPrivateChannel().queue(c -> c.sendMessage(announcement + "\n\nYou may not have won this round, but don't let that stop you from trying again!").queue()));
            winner.openPrivateChannel().queue(c -> c.sendMessage(announcement + "\n\nWoohoo! I've already went ahead and added the funds to your account! Congrats!").queue());
            addWinner(winner, pot);
            pot = 0;
        }
        cleanLottery();
    }

    private void addWinner(User user, int earned) {
        if (winners.size() == 5) {
            winners.remove(4);
            winners.add(0, new Pair<>(user, earned));
        } else winners.add(0, new Pair<>(user, earned));
    }

    public String listWinners(String locale) {
        if (winners.size() == 0) return LocaleUtils.getString("lottery.nowinners", locale);
        else {
            StringBuilder builder = new StringBuilder().append(LocaleUtils.getString("lottery.pastwinners", locale, winners.size())).append(" winners.\n\n");
            for (int i = 0; i < winners.size(); i++) {
                User user = winners.get(i).getKey();
                builder.append(i + 1).append(". ")
                        .append(user.getName()).append("#").append(user.getDiscriminator())
                        .append(" - ").append(CustomEmote.printPrice(winners.get(i).getValue()))
                        .append("\n");
            }
            return builder.toString();
        }
    }

    private void cleanLottery() {
        ticketsCount = 0;
        ballot.clear();
        ballot.add(new LotteryTicket());
        nextExecutionTime = System.currentTimeMillis() + lengthOfLottery;
    }

    public void forceDraw(boolean forceJackpot) {
        draw(true, forceJackpot);
        draw.cancel();
        timer.cancel();
        timer.purge();
        timer = new Timer();
        initializeTimer();
    }

    public void emergencyDraw() {
        ballot.remove(0);
        forceDraw(false);
    }

    private class LotteryTicket {
        private User gambler;
        private boolean jackpot;

        LotteryTicket(User gambler) {
            this.gambler = gambler;
            jackpot = false;
        }

        LotteryTicket() {
            jackpot = true;
        }

        private User getGambler() {
            return gambler;
        }

        private boolean isJackpot() {
            return jackpot;
        }
    }
}
