package com.godson.kekbot.objects;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.settings.Config;
import com.godson.kekbot.settings.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.apache.commons.lang3.exception.ExceptionUtils;
import twitter4j.*;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TwitterManager extends ListenerAdapter {

    //The following variables pertain to KekBot's markov chain tweets.
    private final MarkovChain chain;
    private final ScheduledExecutorService tweeter = new ScheduledThreadPoolExecutor(2);
    private Instant lastTweet;
    private boolean firstTweet = false;
    public final Twitter twitter = TwitterFactory.getSingleton();
    //A list of statuses we wrote to overwrite KekBot's scheduled markov tweets.
    private final List<Pair<Instant, StatusUpdate>> statuses = new ArrayList<>();
    private final int initialDelay = 60;
    private final int subsequentDelay = 30;
    private final boolean tweeting;
    //This concludes the variables needed for KekBot's markov chain tweets.

    //The following variables pertain to the Twitter feed functionality.
    private Map<Long, Message> currentTweets = new HashMap<>();
    private TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
    FilterQuery filterQuery = new FilterQuery();
    /*
    Despite the name of the variable, this isn't a list of OUR followers, this is a list of accounts WE'RE following,
    along with the guilds that requested us to follow them.
     */
    Map<Long, Set<Long>> twitterFollows = new HashMap<>();
    StatusListener listener = new StatusListener() {
        @Override
        public void onStatus(Status status) {
            //We do need this tho
            if (status.isRetweet()) return;
            if (twitterFollows.keySet().stream().noneMatch(id -> id == status.getUser().getId())) return;
            if (status.getInReplyToScreenName() != null) return;

            for (Long guildID : twitterFollows.get(status.getUser().getId())) {
                if (!Settings.getSettings(guildID.toString()).isTwitterFeedEnabled()) continue;
                TextChannel channel = KekBot.jda.getGuildById(guildID).getTextChannelById(Settings.getSettings(guildID.toString()).getTwitterFeedChannel(Long.toString(status.getUser().getId())));
                if (channel == null) {
                    Settings.getSettings(guildID.toString()).unfollowTwitterAccount(Long.toString(status.getUser().getId())).save();
                } else {
                    channel.sendMessage("New Tweet from " + status.getUser().getName() + ": " + "https://twitter.com/" + status.getUser().getScreenName() + "/status/" + status.getId()).queue();
                }
            }
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            //Ignore
        }

        @Override
        public void onTrackLimitationNotice(int i) {
            //Ignore
        }

        @Override
        public void onScrubGeo(long l, long l1) {
            //Ignore
        }

        @Override
        public void onStallWarning(StallWarning stallWarning) {
            //Ignore
        }

        @Override
        public void onException(Exception e) {
            //Ignore
        }
    };

    public TwitterManager(MarkovChain chain, boolean tweeting) {
        this.chain = chain;
        this.tweeting = tweeting;
        if (tweeting) {
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
    }

    public void registerFollow(long accID, long guildID) {
        int follows = twitterFollows.size();
        if (!twitterFollows.containsKey(accID)) {
            Set<Long> guilds = new HashSet<Long>();
            guilds.add(guildID);
            twitterFollows.put(accID, guilds);
        } else {
            twitterFollows.get(accID).add(guildID);
        }
        if (follows != twitterFollows.size()) {
            filterQuery.follow(twitterFollows.keySet().stream().mapToLong(l -> l).toArray());
            twitterStream.filter(filterQuery);
        }
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
                KekBot.jda.getTextChannelById(Config.getConfig().getTwitterChannel()).sendFile(b, "traceback.txt").content("Failed to send tweet. Traceback: ").queue();
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
                KekBot.jda.getTextChannelById(Config.getConfig().getTwitterChannel()).sendFile(b, "traceback.txt").content("Failed to send tweet. Traceback: ").queue();
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
                KekBot.jda.getTextChannelById(Config.getConfig().getTwitterChannel()).sendFile(b, "traceback.txt").content("Failed to send tweet. Traceback: ").queue();
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

    public User lookupUser(String name) throws TwitterException {
        try {
            return twitter.lookupUsers(name).get(0);
        } catch (TwitterException e) {
            if (e.resourceNotFound()) return null;
            else throw e;
        }
    }

    public User lookupID(String ID) throws TwitterException {
        try {
            return twitter.lookupUsers(Long.parseLong(ID)).get(0);
        } catch (TwitterException e) {
            if (e.resourceNotFound()) return null;
            else throw e;
        }
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
        if (tweeting) tweet("KekBot is shutting down. (Reason: " + reason + ")");
    }

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getShardInfo().getShardId() == event.getJDA().getShardInfo().getShardTotal() - 1) {

            twitterStream.addListener(listener);
            twitterStream.sample();
            filterQuery.follow(958176875108593664L);
            twitterStream.filter(filterQuery);
        }
    }
}
