package com.godson.kekbot.objects;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.settings.Config;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.utils.tuple.Pair;
import org.apache.commons.lang3.exception.ExceptionUtils;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TwitterManager {

    private final MarkovChain chain;
    private final ScheduledExecutorService tweeter = new ScheduledThreadPoolExecutor(2);
    private Instant lastTweet;
    private boolean firstTweet = false;
    private final Twitter twitter = TwitterFactory.getSingleton();
    private final List<Pair<Instant, StatusUpdate>> statuses = new ArrayList<>();

    private final int initialDelay = 60;
    private final int subsequentDelay = 30;

    public TwitterManager(MarkovChain chain) {
        this.chain = chain;
        tweeter.schedule(() -> {
            tweeter.scheduleAtFixedRate(() -> {
                Instant now = Instant.now();
                Optional<Pair<Instant, StatusUpdate>> status = statuses.stream().filter(s -> now.isAfter(s.getLeft())).findFirst();
                if (status.isPresent()) {
                    tweet(status.get().getRight());
                    statuses.remove(status.get());
                } else {
                    tweet();
                }
            }, 0, subsequentDelay, TimeUnit.MINUTES);
            firstTweet = true;
        }, initialDelay, TimeUnit.MINUTES);
        lastTweet = Instant.now();
        tweet("KekBot has started up. Please wait an hour before expecting more high quality™ tweets.\n\n" + Instant.now().toString());
    }

    private void tweet() {
        String status = chain.generateSentence(1);
        try {
            twitter.updateStatus(status);
            lastTweet = Instant.now();
        } catch (TwitterException e) {
            String endl = System.getProperty("line.separator");

            //If somehow, the tweet we're posting is a duplicate, we'll just generate a new sentence.
            if (e.getStatusCode() == 403 && e.getErrorCode() == 187) {
                tweet();
                return;
            }

            //If we get timed out, we try the same message again.
            if (e.getStatusCode() == -1) {
                tweet(status);
                return;
            }

            //Otherwise, throw everything into a traceback.txt file, and send it to chat for monitoring.
            String s = KekBot.respond(Action.EXCEPTION_THROWN) + endl + endl + ExceptionUtils.getStackTrace(e);
            try {
                byte[] b = s.getBytes("UTF-8");
                KekBot.jda.getTextChannelById(Config.getConfig().getTwitterChannel()).sendFile(b, "traceback.txt", new MessageBuilder("Failed to send tweet. Traceback: ").build()).queue();
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void tweet(String message) {
        try {
            twitter.updateStatus(message);
            lastTweet = Instant.now();
        } catch (TwitterException e) {
            String endl = System.getProperty("line.separator");

            //If we get timed out, we try the same message again.
            if (e.getStatusCode() == -1) {
                tweet(message);
                return;
            }

            //Otherwise, throw everything into a traceback.txt file, and send it to chat for monitoring.
            String s = KekBot.respond(Action.EXCEPTION_THROWN) + endl + endl + ExceptionUtils.getStackTrace(e);
            try {
                byte[] b = s.getBytes("UTF-8");
                KekBot.jda.getTextChannelById(Config.getConfig().getTwitterChannel()).sendFile(b, "traceback.txt", new MessageBuilder("Failed to send tweet. Traceback: ").build()).queue();
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void tweet(StatusUpdate update) {
        try {
            twitter.updateStatus(update);
            lastTweet = Instant.now();
        } catch (TwitterException e) {
            String endl = System.getProperty("line.separator");

            //If we get timed out, we try the same message again.
            if (e.getStatusCode() == -1) {
                tweet(update);
                return;
            }

            //Otherwise, throw everything into a traceback.txt file, and send it to chat for monitoring.
            String s = KekBot.respond(Action.EXCEPTION_THROWN) + endl + endl + ExceptionUtils.getStackTrace(e);
            try {
                byte[] b = s.getBytes("UTF-8");
                KekBot.jda.getTextChannelById(Config.getConfig().getTwitterChannel()).sendFile(b, "traceback.txt", new MessageBuilder("Failed to send tweet. Traceback: ").build()).queue();
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }
    }

    public Instant calculateOverride(int toSkip) {
        Instant estimate = lastTweet;
        for (int i = 0; i < toSkip; i++) {
            if (i == 0 && !firstTweet) {
                estimate = estimate.plus(initialDelay, ChronoUnit.MINUTES);
                continue;
            }
            estimate = estimate.plus(subsequentDelay, ChronoUnit.MINUTES);
        }
        return estimate;
    }

    /**
     *
     * @param time The time we'll be tweeting the status.
     * @param status The status we'll be tweeting.
     * @throws IllegalArgumentException In the event that a status already exists for this time slot.
     */
    public void overrideTweet(Instant time, StatusUpdate status) {
        if (statuses.stream().noneMatch(s -> s.getLeft().equals(time))) statuses.add(Pair.of(time, status));
        else throw new IllegalArgumentException("no");
    }

    public boolean isOverriden(Instant time) {
        return statuses.stream().anyMatch(p -> p.getLeft().equals(time));
    }

    public void shutdown(String reason) {
        tweeter.shutdown();
        tweet("KekBot is shutting down. (Reason: " + reason + ")");
    }



}
