package com.godson.kekbot.Objects;

import com.godson.discoin4j.Discoin4J;
import com.godson.discoin4j.exceptions.*;
import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Profile.Profile;
import com.godson.kekbot.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.entities.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DiscoinManager {
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(5);
    private Runnable completeTransactions = () -> {
        try {
            List<Discoin4J.PendingTransaction> transactions = KekBot.discoin.getPendingTransactions();
            for (Discoin4J.PendingTransaction transaction : transactions) {
                try {
                    User user = Utils.findShardUser(String.valueOf(transaction.getUserID()));
                    Profile profile = Profile.getProfile(user);
                    if (transaction.getType() != null)
                        user.openPrivateChannel().queue(c -> c.sendMessage("One of your transactions from Discoin could not be completed entirely. Your " + CustomEmote.printPrice(transaction.getAmount()) + " have been returned. (Transaction ID: " + transaction.getReceipt() + ")").queue());
                    else
                        user.openPrivateChannel().queue(c -> c.sendMessage("Woohoo! You just got paid " + CustomEmote.printPrice(transaction.getAmount()) + " from Discoin! (Transaction ID: " + transaction.getReceipt() + ")").queue());
                    profile.addTopKeks(transaction.getAmount());
                    profile.save();

                } catch (NullPointerException e) {
                    //Since the user wasn't found, we're just going to keep going.
                    try {
                        KekBot.discoin.reverseTransaction(transaction.getReceipt());
                    } catch (TransactionNotFoundException ignored) {
                        //This is nearly impossible to be thrown.
                    }
                }
            }
        } catch (IOException | UnknownErrorException | RejectedException | DiscoinErrorException | UnauthorizedException e) {
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
