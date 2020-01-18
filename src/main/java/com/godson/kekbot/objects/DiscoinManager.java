package com.godson.kekbot.objects;

import com.godson.discoin4j.Discoin4J;
import com.godson.discoin4j.exceptions.*;
import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.profile.Profile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DiscoinManager {
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(5);
    String url = "https://dash.discoin.zws.im/#/";
    private Runnable completeTransactions = () -> {
        try {
            List<Discoin4J.Transaction> transactions = KekBot.discoin.getPendingTransactions("KEK");
            for (Discoin4J.Transaction transaction : transactions) {
                try {
                    User user = KekBot.jda.getUserById(transaction.getUser());
                    Profile profile = Profile.getProfile(user);
                    KekBot.discoin.handleTransaction(transaction);
                    user.openPrivateChannel().queue(c -> {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setColor(Color.GREEN);
                        builder.setTitle("Discoin Transaction Recieved!");
                        builder.addField("Amount", transaction.getAmount() + " " + transaction.getFrom().getId() + " -> " + transaction.getPayout() + CustomEmote.printTopKek(), false);
                        builder.addField("Transaction ID", "[" + transaction.getId() + "](" + url + "transactions/" + transaction.getId() + ")", false);
                        c.sendMessage(builder.build()).queue();
                    });
                    //user.openPrivateChannel().queue(c -> c.sendMessage("Woohoo! You just got paid " + CustomEmote.printPrice(transaction.getPayout()) + " from Discoin! (Transaction ID: " + transaction.getId() + ")").queue());
                    profile.addTopKeks(transaction.getPayout());
                    profile.save();
                } catch (NullPointerException e) {
                    //Since the user wasn't found, we're just going to make a reverse transaction that way the user gets their money back.
                    KekBot.discoin.handleTransaction(transaction);
                    KekBot.discoin.makeTransaction(transaction.getUser(), transaction.getPayout(), transaction.getFrom().getId());
                }
            }
        } catch (IOException | UnauthorizedException | GenericErrorException e) {
            e.printStackTrace();
        }
    };

    public DiscoinManager() {
        service.scheduleAtFixedRate(completeTransactions, 10, 1, TimeUnit.MINUTES);
    }

    public void test() {
        service.shutdown();
        completeTransactions.run();
        service = new ScheduledThreadPoolExecutor(5);
        service.scheduleAtFixedRate(completeTransactions, 1, 1, TimeUnit.MINUTES);
    }
}
