package com.godson.kekbot;

import com.godson.kekbot.settings.Settings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class MiscListener extends ListenerAdapter {

    private Timer timer;
    public GameStatus gameStatus = new GameStatus();

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getShardInfo().getShardId() == KekBot.shards-1) {
            //Set timer to change "game" every 10 minutes.
            if (timer == null) {
                timer = new Timer();
                timer.schedule(gameStatus, 0, TimeUnit.MINUTES.toMillis(10));
            }
            //Announce Ready
            System.out.println("KekBot is ready to roll!");
        }
        //Send stats to the important sites.
        Utils.sendStats(event.getJDA());

    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        advertiseCheck(event.getMessage(), event.getGuild(), event.getMember(), event.getChannel());
    }

    private void advertiseCheck(Message message, Guild guild, Member member, TextChannel channel) {
        //First we're gonna check if there's at least one invite on here.
        if (message.getInvites().size() > 0) {
            //Then, we're gonna check if anti-ad is enabled.
            if (Settings.getSettings(guild).isAntiAdEnabled()) {
                //Check if user doesn't have permission to manage messages.
                if (!member.hasPermission(Permission.MESSAGE_MANAGE)) {
                    //Now, check for permission to manage messages.
                    if (guild.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                        message.delete().queue();
                    }
                    channel.sendMessage(LocaleUtils.getString("antiad.caught", "en_l33t", message.getAuthor().getAsMention())).queue();
                }
            }
        }
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
                event.getGuild().getTextChannels().get(0).sendMessage("Unable to automatically set role due to not having the `Manage Roles` permission, fix my permissions, then re-enable auto-role with the `settings` command.").queue();
                settings.save();
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
