package com.godson.kekbot.objects;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.settings.Config;
import net.dv8tion.jda.core.MessageBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TwitterManager {

    private final MarkovChain chain;
    private final ScheduledExecutorService tweeter = new ScheduledThreadPoolExecutor(2);
    private OffsetDateTime lastTweet;

    public TwitterManager(MarkovChain chain) {
        this.chain = chain;
        tweeter.scheduleAtFixedRate(this::tweet, 60, 30, TimeUnit.MINUTES);
        tweet("KekBot has started up. Please wait an hour before expecting more high quality™ tweets.\n\n" + Instant.now().toString());
    }

    private void tweet() {
        Twitter twitter = TwitterFactory.getSingleton();
        try {
            twitter.updateStatus(chain.generateSentence(1));
        } catch (TwitterException e) {
            String endl = System.getProperty("line.separator");
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
        Twitter twitter = TwitterFactory.getSingleton();
        try {
            twitter.updateStatus(message);
        } catch (TwitterException e) {
            String endl = System.getProperty("line.separator");
            String s = KekBot.respond(Action.EXCEPTION_THROWN) + endl + endl + ExceptionUtils.getStackTrace(e);
            try {
                byte[] b = s.getBytes("UTF-8");
                KekBot.jda.getTextChannelById(Config.getConfig().getTwitterChannel()).sendFile(b, "traceback.txt", new MessageBuilder("Failed to send tweet. Traceback: ").build()).queue();
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void shutdown(String reason) {
        tweeter.shutdown();
        tweet("KekBot is shutting down. (Reason: " + reason + ")");
    }



}
