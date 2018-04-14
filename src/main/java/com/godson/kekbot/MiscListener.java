package com.godson.kekbot;

import com.godson.kekbot.exceptions.ChannelNotFoundException;
import com.godson.kekbot.exceptions.MessageNotFoundException;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.settings.Settings;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.update.GenericGuildUpdateEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.rmi.CORBA.Util;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class MiscListener extends ListenerAdapter {

    SimpleDateFormat ft2 = new SimpleDateFormat("[HH:mm:ss]: ");
    Timer timer;

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getShardInfo().getShardId() == KekBot.shards-1) {
            //Set timer to change "game" every 10 minutes.
            if (timer == null) {
                timer = new Timer();
                timer.schedule(new GameStatus(), 0, TimeUnit.MINUTES.toMillis(10));
            }
            //Announce Ready
            System.out.println("KekBot is ready to roll!");
        }
        //Send stats to the important sites.
        Utils.sendStats(event.getJDA());

    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        //First we're gonna check if there's at least one invite on here.
        if (event.getMessage().getInvites().size() > 0) {
            //Then, we're gonna check if anti-ad is enabled.
            if (Settings.getSettings(event.getGuild()).isAntiAdEnabled()) {
                //Check if user doesn't have permission to manage messages.
                if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                    //Now, check for permission to manage messages.
                    if (event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                        event.getMessage().delete().queue();
                    }
                    event.getChannel().sendMessage(event.getMessage().getAuthor().getAsMention() + ", no advertising!").queue();
                }
            }
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        //out.println(ft2.format(System.currentTimeMillis()) + event.getMember().getEffectiveName() + " has joined " + event.getGuild().getName() + ".");

        //Load settings into a variable.
        Settings settings = Settings.getSettings(event.getGuild());

        String welcomeChannel = settings.getWelcomeChannel();
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
        if (welcomeChannel != null && settings.getWelcomeMessage() != null) {
            //Check if the welcome channel still exists.
            if (event.getGuild().getTextChannelById(welcomeChannel) != null) {
                //We can now welcome the user into our server.
                event.getGuild().getTextChannelById(welcomeChannel).sendMessage(settings.getWelcomeMessage()
                        .replace("{mention}", event.getMember().getAsMention())
                        .replace("{name}", event.getMember().getEffectiveName())).queue();
            } else {
                //The channel doesn't exist, so we'll disable welcome messages.
                settings.setWelcomeChannel(null);
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
        String welcomeChannel = settings.getWelcomeChannel();

        //Check if the farewell channel/message are set.
        if (welcomeChannel != null && settings.getFarewellMessage() != null) {
            //Check if the welcome channel still exists.
            if (event.getGuild().getTextChannelById(welcomeChannel) != null) {
                //We can now say goodbye to the user as they leave.
                event.getGuild().getTextChannelById(welcomeChannel).sendMessage(settings.getFarewellMessage()
                        .replace("{mention}", event.getMember().getAsMention())
                        .replace("{name}", event.getMember().getEffectiveName())).queue();
            } else {
                //The channel doesn't exist, so we'll disable farewell messages.
                settings.setWelcomeChannel(null);
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
