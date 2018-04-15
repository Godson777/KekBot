package com.godson.kekbot.command.commands;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.objects.MarkovChain;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class MarkovTest extends Command {

    private final MarkovChain chain;

    public MarkovTest(MarkovChain chain) {
        name = "markov";
        this.chain = chain;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        event.getChannel().sendMessage(chain.generateSentence(1)).queue();
    }
}
