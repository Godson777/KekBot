package com.godson.kekbot.objects;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.settings.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.tuple.Pair;
import org.apache.commons.lang3.exception.ExceptionUtils;
import twitter4j.*;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TwitterManager extends ListenerAdapter {

    private final MarkovChain chain;
    private final ScheduledExecutorService tweeter = new ScheduledThreadPoolExecutor(2);
    private Instant lastTweet;
    private boolean firstTweet = false;
    private final Twitter twitter = TwitterFactory.getSingleton();
    private final List<Pair<Instant, StatusUpdate>> statuses = new ArrayList<>();

    private Map<Long, Message> currentTweets = new HashMap<>();
    TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
    long[] ids = new long[]{958176875108593664L, 610103342L, 2996678026L, 624995324L, 1475679589L, 845418771896524801L};
    StatusListener listener = new StatusListener() {
        @Override
        public void onStatus(Status status) {
            //We do need this tho
            if (status.isRetweet()) return;
            if (Arrays.stream(ids).noneMatch(id -> id == status.getUser().getId())) return;

            EmbedBuilder builder = new EmbedBuilder();
            builder.setThumbnail(status.getUser().getProfileImageURL());
            builder.setColor(Color.RED);
            builder.setTitle("New tweet by: " + status.getUser().getName(), "https://twitter.com/" + status.getUser().getScreenName() + "/status/" + status.getId());
            builder.setAuthor("@" + status.getUser().getScreenName());
            builder.setTimestamp(Instant.now());
            if (status.getMediaEntities().length > 0) builder.setImage(status.getMediaEntities()[0].getMediaURL());
            builder.setDescription(status.getText());

            KekBot.jda.getTextChannelById("327379946794254338").sendMessage(builder.build()).queue(m -> currentTweets.put(status.getId(), m));
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            //We may not need this
            if (currentTweets.containsKey(statusDeletionNotice.getStatusId())) {
                Message m = currentTweets.get(statusDeletionNotice.getStatusId());
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.GRAY);
                builder.setTimestamp(m.getCreationTime());
                builder.setTitle("This tweet was removed from Twitter.");
                m.editMessage(builder.build()).queue();
                currentTweets.remove(statusDeletionNotice.getStatusId());
            }
        }

        @Override
        public void onTrackLimitationNotice(int i) {
            //We may not need this
        }

        @Override
        public void onScrubGeo(long l, long l1) {
            //We may not need this
        }

        @Override
        public void onStallWarning(StallWarning stallWarning) {
            //We may not need this
        }

        @Override
        public void onException(Exception e) {
            //We may not need this
        }
    };
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
            String s = KekBot.respond(Action.EXCEPTION_THROWN, KekBot.getCommandClient().getDefaultLocale()) + endl + endl + ExceptionUtils.getStackTrace(e);
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
            String s = KekBot.respond(Action.EXCEPTION_THROWN, KekBot.getCommandClient().getDefaultLocale()) + endl + endl + ExceptionUtils.getStackTrace(e);
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
            String s = KekBot.respond(Action.EXCEPTION_THROWN, KekBot.getCommandClient().getDefaultLocale()) + endl + endl + ExceptionUtils.getStackTrace(e);
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
        twitterStream.shutdown();
        tweet("KekBot is shutting down. (Reason: " + reason + ")");
    }

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getShardInfo().getShardId() == event.getJDA().getShardInfo().getShardTotal() - 1) {

            twitterStream.addListener(listener);
            twitterStream.sample();
            FilterQuery filterQuery = new FilterQuery();
            filterQuery.follow(ids);
            twitterStream.filter(filterQuery);
        }
    }
}
