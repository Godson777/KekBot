package com.godson.kekbot.command.commands.social;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.settings.Settings;
import com.godson.kekbot.util.Utils;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import twitter4j.TwitterException;
import twitter4j.User;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.godson.kekbot.settings.Settings.getSettings;

public class TwitterCommand extends Command {

    private final String ADD_FOLLOW = "\uD83D\uDCCC";
    private final String LIST_FOLLOWS = "\uD83D\uDCC3";
    private final String REMOVE_FOLLOW = "\uD83D\uDDD1";
    private final String DISABLE_FEED = "\uD83D\uDD15";
    private final String ENABLE_FEED = "\uD83D\uDD14";

    private final String CHECK = "✅";
    private final String X = "❎";

    public TwitterCommand() {
        name = "twitter";
        description = "Lets you create a live feed of tweets sent to your server!";
        usage.add("twitter");
        category = new Category("Social");
        requiredUserPerms = new Permission[]{Permission.MANAGE_SERVER};
        requiredBotPerms = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MANAGE};
    }

    @Override
    public void onExecuted(CommandEvent event) {
        AtomicBoolean enabled = new AtomicBoolean(Settings.getSettings(event.getGuild()).isTwitterFeedEnabled());
        ButtonMenu.Builder menu = new ButtonMenu.Builder();
        menu.setTimeout(5, TimeUnit.MINUTES);
        menu.setEventWaiter(KekBot.waiter);
        menu.setUsers(event.getAuthor());
        menu.setColor(Color.CYAN);
        menu.setDescription(event.getString("social.twitter.intro", ADD_FOLLOW, LIST_FOLLOWS, REMOVE_FOLLOW, (enabled.get() ? DISABLE_FEED : ENABLE_FEED), (enabled.get() ? event.getString("generic.disable") : event.getString("generic.enable")), X));
        menu.setChoices(ADD_FOLLOW, LIST_FOLLOWS, REMOVE_FOLLOW, (enabled.get() ? DISABLE_FEED : ENABLE_FEED), X);
        AtomicInteger choice = new AtomicInteger(0);
        menu.setAction(e -> {
            switch (e.getName()) {
                case ADD_FOLLOW:
                    choice.set(1);
                    break;
                case LIST_FOLLOWS:
                    choice.set(2);
                    break;
                case REMOVE_FOLLOW:
                    choice.set(3);
                    break;
                case DISABLE_FEED:
                    choice.set(4);
                    Settings.getSettings(event.getGuild()).toggleTwitterFeed(false).save();
                    break;
                case ENABLE_FEED:
                    choice.set(4);
                    Settings.getSettings(event.getGuild()).toggleTwitterFeed(true).save();
                    break;
                case X:
                    choice.set(0);
                    break;
            }
        });
        menu.setFinalAction(m -> {
            switch (choice.get()) {
                case 1:
                    //Check if we haven't reached our max feeds yet.
                    if (Settings.getSettings(event.getGuild()).getTwitterFeeds().size() == 3) {
                        AtomicInteger finalChoice = new AtomicInteger(0);
                        ButtonMenu.Builder builder = new ButtonMenu.Builder();
                        builder.setEventWaiter(KekBot.waiter);
                        builder.addChoices(CHECK, X);
                        builder.setTimeout(5, TimeUnit.MINUTES);
                        builder.setColor(Color.CYAN);
                        builder.setDescription(event.getString("social.twitter.maxreached", 3));
                        setReturnActions(finalChoice, builder);
                        setReturnFinalAction(menu, finalChoice, builder);
                        m.clearReactions().queue(succ ->builder.build().display(m));
                        return;
                    }
                    //Delete our message, and start getting the account name from the user.
                    m.delete().queue(s -> Questionnaire.newQuestionnaire(event)
                            .addQuestion(event.getString("social.twitter.addaccount.step.1"), QuestionType.STRING)
                            .asEmbed(Color.CYAN)
                            .execute(results -> {
                                //I hate that I have to surround this with a try/catch loop, but whatever.
                                results.getMessage(0).delete().queue();
                                results.getQuestion().delete().queue();
                                try {
                                    //Check if the name is tied to a valid user.
                                    User toFollow = KekBot.twitterManager.lookupUser(results.getAnswerAsType(0, String.class));
                                    if (toFollow == null) {
                                        //Make a new embedbuilder with the error message so we can use it.
                                        EmbedBuilder error = new EmbedBuilder();
                                        error.setColor(Color.CYAN);
                                        error.setDescription(event.getString("social.twitter.addaccount.notfound"));
                                        results.getQuestion().editMessage(error.build()).queue();
                                        //Now get the user's input again.
                                        results.reExecuteWithoutMessage();
                                    } else {
                                        //Account found, let's check if we already follow it.
                                        if (Settings.getSettings(event.getGuild()).isFollowingTwitterAccount(Long.toString(toFollow.getId()))) {
                                            AtomicInteger finalChoice = new AtomicInteger(0);
                                            ButtonMenu.Builder builder = new ButtonMenu.Builder();
                                            builder.setEventWaiter(KekBot.waiter);
                                            builder.addChoices(CHECK, X);
                                            builder.setTimeout(5, TimeUnit.MINUTES);
                                            builder.setColor(Color.CYAN);
                                            builder.setDescription(event.getString("social.twitter.addaccount.duplicate"));
                                            setReturnActions(finalChoice, builder);
                                            setReturnFinalAction(menu, finalChoice, builder);
                                            builder.build().display(event.getTextChannel());
                                            return;
                                        }
                                        Questionnaire.newQuestionnaire(results)
                                                .asEmbed(Color.CYAN)
                                                .addQuestion(event.getString("social.twitter.addaccount.step.2"), QuestionType.STRING)
                                                .useRawInput()
                                                .execute(results1 -> {
                                                    results1.getMessage(0).delete().queue();
                                                    //Let's check if the channel the user pinged is valid.
                                                    TextChannel channel;
                                                    try {
                                                        channel = Utils.resolveChannelMention(event.getGuild(), results1.getAnswerAsType(0, String.class));
                                                    } catch (IllegalArgumentException e) {
                                                        //The user didn't #mention a channel properly. Build another error, delete the user's message, and edit our existing one.
                                                        EmbedBuilder error = new EmbedBuilder();
                                                        error.setColor(Color.CYAN);
                                                        error.setDescription(event.getString("social.twitter.addaccount.invalidchannel"));
                                                        results1.getQuestion().editMessage(error.build()).queue();
                                                        results1.reExecuteWithoutMessage();
                                                        return;
                                                    }

                                                    if (channel == null) {
                                                        //The channel the user tried to give us doesn't exist. Build another error, delete the user's message, and edit our existing one.
                                                        EmbedBuilder error = new EmbedBuilder();
                                                        error.setColor(Color.CYAN);
                                                        error.setDescription(event.getString("social.twitter.addaccount.channelnotfound"));
                                                        results1.getQuestion().editMessage(error.build()).queue();
                                                        results1.reExecuteWithoutMessage();
                                                        return;
                                                    }

                                                    //We did it, we're done. Take us to the completed message, and prompt the user to continue or exit.
                                                    Settings.getSettings(event.getGuild()).followTwitterAccount(Long.toString(toFollow.getId()), channel.getId()).save();
                                                    KekBot.twitterManager.registerFollow(toFollow.getId(), event.getGuild().getIdLong());


                                                    AtomicInteger finalChoice = new AtomicInteger(0);
                                                    ButtonMenu.Builder builder = new ButtonMenu.Builder();
                                                    builder.setEventWaiter(KekBot.waiter);
                                                    builder.addChoices(CHECK, X);
                                                    builder.setTimeout(5, TimeUnit.MINUTES);
                                                    builder.setColor(Color.CYAN);
                                                    builder.setDescription(event.getString("social.twitter.addaccount.complete"));
                                                    setReturnActions(finalChoice, builder);
                                                    setReturnFinalAction(menu, finalChoice, builder);
                                                    builder.build().display(results1.getQuestion());
                                                });
                                    }
                                } catch (TwitterException e) {
                                    throwException(e, event);
                                }
                            }));
                    break;
                case 2:
                    //Pull the settings from our guild
                    Settings settings = Settings.getSettings(event.getGuild());
                    //Check if we have any accounts followed, throw error if not.
                    if (isFollowingAccounts(event, menu, settings, m)) break;
                    //VROOM VROOM LET'S LIST OUR ACCOUNTS
                    Paginator.Builder list = new Paginator.Builder();
                    list.setEventWaiter(KekBot.waiter);
                    for (String account : settings.getTwitterFeeds().keySet()) {
                        try {
                            //does channel exist? remove entry if not
                            TextChannel channel = event.getGuild().getTextChannelById(settings.getTwitterFeedChannel(account));
                            if (channel == null) {
                                settings.unfollowTwitterAccount(account).save();
                                continue;
                            }
                            User user = KekBot.twitterManager.lookupID(account);
                            //If, for some reason, the account we previously followed got taken down or something, we remove the entry from settings and move on.
                            if (user == null) {
                                settings.unfollowTwitterAccount(account).save();
                                continue;
                            }
                            list.addItems(user.getName() + " (@" + user.getScreenName() + ")\nin: " + channel.getAsMention());
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                    }
                    list.waitOnSinglePage(true);
                    list.setColor(Color.CYAN);
                    list.wrapPageEnds(true);
                    list.useNumberedItems(true);
                    list.setText("");
                    list.setTimeout(5, TimeUnit.MINUTES);
                    list.setFinalAction(me -> me.clearReactions().queue(succ -> menu.build().display(me)));
                    m.clearReactions().queue(succ -> list.build().display(m));
                    break;
                case 3:
                    //Pull the settings from our guild
                    Settings s = Settings.getSettings(event.getGuild());
                    //Check if we have any accounts followed, throw error if not.
                    if (isFollowingAccounts(event, menu, s, m)) break;
                    //VROOM VROOM LET'S LIST OUR ACCOUNTS, wait haven't we done this before?
                    OrderedMenu.Builder builder = new OrderedMenu.Builder();
                    builder.setDescription(event.getString("social.twitter.removeaccount"));
                    builder.setColor(Color.CYAN);
                    builder.setEventWaiter(KekBot.waiter);
                    List<String> feeds = new ArrayList<>(s.getTwitterFeeds().keySet());
                    for (String account : feeds) {
                        try {
                            //does channel exist? remove entry if not
                            TextChannel channel = event.getGuild().getTextChannelById(s.getTwitterFeedChannel(account));
                            if (channel == null) {
                                s.unfollowTwitterAccount(account).save();
                                continue;
                            }
                            User user = KekBot.twitterManager.lookupID(account);
                            //If, for some reason, the account we previously followed got taken down or something, we remove the entry from settings and move on.
                            if (user == null) {
                                s.unfollowTwitterAccount(account).save();
                                continue;
                            }
                            builder.addChoice(user.getName() + " (@" + user.getScreenName() + ")\nin: " + channel.getAsMention());
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                    }
                    builder.useCancelButton(true);
                    builder.setCancel(me -> menu.build().display(event.getTextChannel()));
                    builder.setSelection((me, i) -> {
                        s.unfollowTwitterAccount(feeds.get(i-1)).save();
                        AtomicInteger finalChoice = new AtomicInteger(0);
                        ButtonMenu.Builder complete = new ButtonMenu.Builder();
                        complete.setEventWaiter(KekBot.waiter);
                        complete.addChoices(CHECK, X);
                        complete.setTimeout(5, TimeUnit.MINUTES);
                        complete.setColor(Color.CYAN);
                        complete.setDescription(event.getString("social.twitter.addaccount.complete"));
                        setReturnActions(finalChoice, complete);
                        setReturnFinalAction(menu, finalChoice, complete);
                        complete.build().display(event.getTextChannel());
                    });
                    m.clearReactions().queue(succ -> builder.build().display(m));
                    break;
                case 4:
                    enabled.set(Settings.getSettings(event.getGuild()).isTwitterFeedEnabled());
                    menu.setDescription(event.getString("social.twitter.intro", ADD_FOLLOW, LIST_FOLLOWS, REMOVE_FOLLOW, (enabled.get() ? DISABLE_FEED : ENABLE_FEED), (enabled.get() ? event.getString("generic.disable") : event.getString("generic.enable")), X));
                    menu.setChoices(ADD_FOLLOW, LIST_FOLLOWS, REMOVE_FOLLOW, (enabled.get() ? DISABLE_FEED : ENABLE_FEED), X);
                    m.clearReactions().queue(succ -> menu.build().display(m));
                    break;
                case 0:
                    m.delete().queue();
                    break;
            }
        });
        menu.build().display(event.getTextChannel());
    }

    private boolean isFollowingAccounts(CommandEvent event, ButtonMenu.Builder menu, Settings s, Message m) {
        if (s.getTwitterFeeds().isEmpty()) {
            AtomicInteger finalChoice = new AtomicInteger(0);
            ButtonMenu.Builder builder = new ButtonMenu.Builder();
            builder.setEventWaiter(KekBot.waiter);
            builder.addChoices(CHECK, X);
            builder.setTimeout(5, TimeUnit.MINUTES);
            builder.setColor(Color.CYAN);
            builder.setDescription(event.getString("social.twitter.nofollowedaccounts"));
            setReturnActions(finalChoice, builder);
            setReturnFinalAction(menu, finalChoice, builder);
            m.clearReactions().queue(succ ->builder.build().display(m));
            return true;
        }
        return false;
    }

    //Literally only exists so I don't have duplicate code.
    private void setReturnFinalAction(ButtonMenu.Builder menu, AtomicInteger finalChoice, ButtonMenu.Builder builder) {
        builder.setFinalAction(me -> {
            switch (finalChoice.get()) {
                case 1:
                    me.clearReactions().queue(succ -> menu.build().display(me));
                    break;
                case 2:
                    me.delete().queue();
                    break;
            }
        });
    }

    //Literally only exists so I don't have duplicate code.
    private void setReturnActions(AtomicInteger finalChoice, ButtonMenu.Builder builder) {
        builder.setAction(e -> {
            switch (e.getName()) {
                case CHECK:
                    finalChoice.set(1);
                    break;
                case X:
                    finalChoice.set(2);
                    break;
            }
        });
    }
}
