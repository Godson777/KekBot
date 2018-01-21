package com.godson.kekbot;

import com.godson.kekbot.exceptions.ChannelNotFoundException;
import com.godson.kekbot.exceptions.MessageNotFoundException;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.settings.Settings;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class Listener extends ListenerAdapter {

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
        //Prepare some per-server stuff.
        JDA jda = event.getJDA();
            jda.getGuilds().forEach(guild -> {
                Settings settings = Settings.getSettings(guild);

                if (settings == null) {
                    //settings = new Settings().setName(guild.getName());
                    //settings.save();
                }

                /*if (GSONUtils.numberOfCCommands(guild) > 0) {
                    List<CustomCommand> commands = GSONUtils.getCCommands(guild);
                    for (CustomCommand command : commands) {
                        try {
                            command.register(jda, guild);
                        } catch (IllegalArgumentException e) {
                            //ignore
                        }
                    }
                }*/
            });

    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        out.println(ft2.format(System.currentTimeMillis()) + event.getMember().getEffectiveName() + " has joined " + event.getGuild().getName() + ".");

        Settings settings = Settings.getSettings(event.getGuild());

        if (settings.getAutoRoleID() != null) {
            try {
                event.getGuild().getController().addRolesToMember(event.getMember(), event.getGuild().getRoleById(settings.getAutoRoleID())).reason("Auto-Role").queue();
            } catch (PermissionException e) {
                event.getGuild().getTextChannels().get(0).sendMessage("Unable to automatically set role due to not having the **Manage Roles** permission.").queue();
            } catch (NullPointerException e) {
                event.getGuild().getTextChannels().get(0).sendMessage("I can no longer find the rank in which I was to automatically assign!").queue();
            }
        }

        if (settings.welcomeEnabled()) {
            try {
                settings.getWelcomeChannel(event.getGuild()).sendMessage(settings.getWelcomeMessage().replace("{mention}", event.getMember().getAsMention()).replace("{name|", event.getMember().getEffectiveName())).queue();
            } catch (MessageNotFoundException e) {
                //settings.setWelcomeMessage(null).toggleWelcome(false).save(event.getGuild());
                for (TextChannel channel : event.getGuild().getTextChannels()) {
                    try {
                        channel.sendMessage("**WARNING:** KekBot could not find this server's welcome message! Please set it using `$announce welcome message <message>`!").queue();
                        break;
                    } catch (PermissionException er) {
                        //¯\_(ツ)_/¯
                    }
                }
            } catch (ChannelNotFoundException | NullPointerException e) {
                //settings.setWelcomeChannel(null).toggleWelcome(false).save(event.getGuild());
                for (TextChannel channel : event.getGuild().getTextChannels()) {
                    try {
                        channel.sendMessage("**WARNING:** KekBot could not find this server's welcome channel! Please set it using `$announce welcome channel <#channel>`!").queue();
                        break;
                    } catch (PermissionException er) {
                        //¯\_(ツ)_/¯
                    }
                }
            }
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        out.println(ft2.format(System.currentTimeMillis()) + event.getMember().getEffectiveName() + " has left " + event.getGuild().getName() + ".");
        Settings settings = Settings.getSettings(event.getGuild());

        if (settings.farewellEnabled()) {
            try {
                settings.getFarewellChannel(event.getGuild()).sendMessage(settings.getFarewellMessage().replace("{mention}", event.getMember().getAsMention()).replace("{name}", event.getMember().getEffectiveName())).queue();
            } catch (MessageNotFoundException e) {
                //settings.setFarewellMessage(null).toggleFarewell(false).save(event.getGuild());
                for (TextChannel channel : event.getGuild().getTextChannels()) {
                    try {
                        channel.sendMessage("**WARNING:** KekBot could not find this server's welcome message! Please set it using `$announce welcome message <message>`!").queue();
                        break;
                    } catch (PermissionException er) {
                        //¯\_(ツ)_/¯
                    }
                }
            } catch (ChannelNotFoundException | NullPointerException e) {
                //settings.setFarewellChannel(null).toggleFarewell(false).save(event.getGuild());
                for (TextChannel channel : event.getGuild().getTextChannels()) {
                    try {
                        channel.sendMessage("**WARNING:** KekBot could not find this server's welcome channel! Please set it using `$announce welcome channel <#channel>`!").queue();
                        break;
                    } catch (PermissionException er) {
                        //¯\_(ツ)_/¯
                    }
                }
            }
        }
    }
}
