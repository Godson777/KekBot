package com.godson.kekbot;

import com.godson.kekbot.settings.Config;
import com.godson.kekbot.settings.Settings;
import com.godson.kekbot.util.LocaleUtils;
import com.godson.kekbot.util.Utils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import twitter4j.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MiscListener extends ListenerAdapter {

    private Timer timer;
    public GameStatus gameStatus = new GameStatus();

    private Twitter twitter;
    private Pattern twitterPattern = Pattern.compile("twitter.com/\\w+/status/(\\d+)");

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getShardInfo().getShardId() == KekBot.shards-1) {
            //Set timer to change "game" every 10 minutes.
            if (timer == null) {
                timer = new Timer();
                timer.schedule(gameStatus, 0, TimeUnit.MINUTES.toMillis(10));
            }

            //Get our local Twitter instance ready:
            if (Config.getConfig().usingTwitter()) {
                twitter = new TwitterFactory(KekBot.twitterConfig.build()).getInstance();
            }

            //Announce Ready
            System.out.println("KekBot is ready to roll!");
        }
        //Send stats to the important sites.
        Utils.sendStats(event.getJDA());

    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        KekBot.chain.addDictionary(event.getMessage().getContentStripped());
        advertiseCheck(event.getMessage(), event.getGuild(), event.getMember(), event.getChannel());
        if (twitter != null) twitterCheck(event);
    }

    private void advertiseCheck(Message message, Guild guild, Member member, TextChannel channel) {
        //First we're gonna check if there's at least one invite on here.
        if (!message.getInvites().isEmpty()) {
            //Then, we're gonna check if anti-ad is enabled.
            if (!Settings.getSettings(guild).isAntiAdEnabled()) return;

            //Check if user doesn't have permission to manage messages.
            if (!member.hasPermission(Permission.MESSAGE_MANAGE)) {
                //Now, check for permission to manage messages.
                if (guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                    message.delete().queue();
                }
                channel.sendMessage(LocaleUtils.getString("antiad.caught", KekBot.getGuildLocale(guild), message.getAuthor().getAsMention())).queue();
            }
        }
    }

    private void twitterCheck(GuildMessageReceivedEvent event) {
        //Are we allowed to start?
        if (!Settings.getSettings(event.getGuild()).isTweetFinishEnabled()) return;

        //Get our matches.
        List<String> links = findMatches(twitterPattern, event.getMessage().getContentRaw());
        //Do we have any matches?
        if (links.isEmpty()) return;

        //Process the matches and post the remaining images in chat.
        StringBuilder builder = new StringBuilder();
        for (String link : links) {
            try {
                Status status = twitter.showStatus(Long.valueOf(link.substring(link.lastIndexOf("/") + 1)));
                MediaEntity[] images = status.getMediaEntities();

                //Do we have more than one image?
                if (images.length < 2) return;

                //Iterate through every image, and add them to the StringBuilder.
                for (int i = 1; i < images.length; i++) {
                    if (images[i].getType().equals("photo")) {
                        builder.append("\n");
                        builder.append(images[i].getMediaURLHttps());
                    }
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        //Finally, send the message.
        event.getChannel().sendMessage(builder).queue();
    }

    private List<String> findMatches(Pattern pattern, String string) {
        List<String> matches = new ArrayList<>();
        Matcher m = pattern.matcher(string);
        while (m.find()) {
            matches.add(m.group());
        }
        return matches;
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        advertiseCheck(event.getMessage(), event.getGuild(), event.getMember(), event.getChannel());
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        //out.println(ft2.format(System.currentTimeMillis()) + event.getMember().getEffectiveName() + " has joined " + event.getGuild().getName() + ".");

        //Load settings into a variable.
        Settings settings = Settings.getSettings(event.getGuild());

        String welcomeChannel = settings.getAnnounceSettings().getWelcomeChannelID();
        TextChannel errorChannel = Utils.findAvailableTextChannel(event.getGuild());

        //Check if the server has auto role
        if (settings.getAutoRoleID() != null) {
            //Check if the bot has perms to manage roles
            if (event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                //Check if the role even exists before we continue
                if (event.getGuild().getRoleById(settings.getAutoRoleID()) != null) {
                    //Check if the role is lower than the bot's highest role in the hierarchy.
                    if (Utils.checkHierarchy(event.getGuild().getRoleById(settings.getAutoRoleID()), event.getGuild().getSelfMember())) {
                        //Finally, we give the role to the user.
                        event.getGuild().getController().addRolesToMember(event.getMember(), event.getGuild().getRoleById(settings.getAutoRoleID())).reason("Auto-Role").queue();
                    } else {
                        //We can't touch the role, so we'll turn off auto role.
                        settings.setAutoRoleID(null);
                        settings.save();
                        if (errorChannel != null) errorChannel.sendMessage("Specified role is higher than any of mine, lower the rank hierarchy, then re-enable auto-role with the `settings` command.").queue();
                    }
                } else {
                    //The role doesn't exist, so we'll turn off auto role.
                    settings.setAutoRoleID(null);
                    settings.save();
                    if (errorChannel != null) errorChannel.sendMessage("Unable to find the role previously specified (was it deleted?), you'll have to set auto-role again with the `settings` command.").queue();
                }
            } else {
                //The bot doesn't have manage role perms, so we'll turn off auto role.
                settings.setAutoRoleID(null);
                settings.save();
                if (errorChannel != null) event.getGuild().getTextChannels().get(0).sendMessage("Unable to automatically set role due to not having the `Manage Roles` permission, fix my permissions, then re-enable auto-role with the `settings` command.").queue();
            }
        }

        //Check if the welcome channel/message are set.
        if (welcomeChannel != null && settings.getAnnounceSettings().getWelcomeMessage() != null) {
            //Check if the welcome channel still exists.
            if (event.getGuild().getTextChannelById(welcomeChannel) != null) {
                //We can now welcome the user into our server.
                event.getGuild().getTextChannelById(welcomeChannel).sendMessage(settings.getAnnounceSettings().getWelcomeMessage()
                        .replace("{mention}", event.getMember().getAsMention())
                        .replace("{name}", event.getMember().getEffectiveName())).queue();
            } else {
                //The channel doesn't exist, so we'll disable welcome messages.
                settings.getAnnounceSettings().setWelcomeChannel(null);
                settings.save();
                if (errorChannel != null) errorChannel.sendMessage("Unable to find previously specified welcome channel, you'll have to set the channel again with the `settings` command.").queue();
            }
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        //out.println(ft2.format(System.currentTimeMillis()) + event.getMember().getEffectiveName() + " has left " + event.getGuild().getName() + ".");

        //Load settings into a variable.
        Settings settings = Settings.getSettings(event.getGuild());

        TextChannel errorChannel = Utils.findAvailableTextChannel(event.getGuild());
        String welcomeChannel = settings.getAnnounceSettings().getWelcomeChannelID();

        //Check if the farewell channel/message are set.
        if (welcomeChannel != null && settings.getAnnounceSettings().getFarewellMessage() != null) {
            //Check if the welcome channel still exists.
            if (event.getGuild().getTextChannelById(welcomeChannel) != null) {
                //We can now say goodbye to the user as they leave.
                event.getGuild().getTextChannelById(welcomeChannel).sendMessage(settings.getAnnounceSettings().getFarewellMessage()
                        .replace("{mention}", event.getMember().getAsMention())
                        .replace("{name}", event.getMember().getEffectiveName())).queue();
            } else {
                //The channel doesn't exist, so we'll disable farewell messages.
                settings.getAnnounceSettings().setWelcomeChannel(null);
                settings.save();
                if (errorChannel != null) errorChannel.sendMessage("Unable to find previously specified welcome channel, you'll have to set the channel again with the `settings` command.").queue();
            }
        }
    }

    public void shutdown() {
        timer.cancel();
        timer.purge();
    }
}
