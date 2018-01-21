package com.godson.kekbot.command;

import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Utils;
import com.godson.kekbot.settings.Config;
import com.godson.kekbot.settings.Settings;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class CommandClient extends ListenerAdapter {

    private final OffsetDateTime start;

    private final String botOwner;
    private final List<String> botAdmins;

    private final String prefix = "$";
    private final HashMap<String, String> customPrefixes;
    private final ArrayList<Command> commands;
    private final HashMap<String, Integer> commandIndex;
    private final HashMap<String, OffsetDateTime> cooldowns;
    //Key = User ID, Value = Disable Type. (0 = Tickets only, 1 = Commands only, 2 = Blocked entirely.)
    private final HashMap<String, Integer> disabledUsers;
    //Key = Guild ID, Value = List of User IDs.
    private final HashMap<String, List<String>> disabledMembers;
    //Key = Channel ID, Value = List of User IDs.
    private final HashMap<String, List<String>> activeQuestionnaires;

    private TextChannel joinLogChannel;
    public TextChannel ticketChannel;

    private final String joinSpeech = "Hi! I'm KekBot! Thanks for inviting me!" + "\n" +
            "Use $help to see a list of commands, and use $prefix to change my prefix!" + "\n" +
            "If you ever need help, join my discord server: " + "https://discord.gg/3nbqavE";

    public CommandClient() {
        start = OffsetDateTime.now();
        commands = new ArrayList<>();
        customPrefixes = new HashMap<>();
        cooldowns = new HashMap<>();
        commandIndex = new HashMap<>();
        disabledUsers = new HashMap<>();
        disabledMembers = new HashMap<>();
        activeQuestionnaires = new HashMap<>();

        Config config = Config.getConfig();
        botOwner = config.getBotOwner();
        botAdmins = config.getBotAdmins();
    }

    public void addCommand(Command command) {
        String name = command.getName();
        synchronized (commandIndex) {
            if (commandIndex.containsKey(name)) throw new IllegalArgumentException("Command added already has a name/alias that was already indexed: \"" + name + "\"!");
            for(String alias : command.getAliases()) {
                if (commandIndex.containsKey(alias)) throw new IllegalArgumentException("Command added already has a name/alias that was already indexed: \"" + name + "\"!");
                commandIndex.put(alias, commands.size());
            }
            commandIndex.put(name, commands.size());
        }

        commands.add(command);
    }


    public ArrayList<Command> getCommands() {
        return commands;
    }

    public void disableUser(String ID, int type) {
        if (!disabledUsers.containsKey(ID)) disabledUsers.put(ID, type);
    }

    public void undisableUser(String ID) {
        if (disabledUsers.containsKey(ID)) disabledUsers.remove(ID);
    }

    public void disableMember(String guildID, String userID) {
        if (!disabledMembers.containsKey(guildID)) disabledMembers.put(guildID, new ArrayList<>());
        disabledMembers.get(guildID).add(userID);
    }

    public void registerQuestionnaire(String channelID, String userID) {
        if (!activeQuestionnaires.containsKey(channelID)) activeQuestionnaires.put(channelID, new ArrayList<>());
        activeQuestionnaires.get(channelID).add(userID);
    }

    public void unregisterQuestionnaire(String channelID, String userID) {
        if (!activeQuestionnaires.containsKey(channelID)) return;
        if (activeQuestionnaires.get(channelID).contains(userID)) activeQuestionnaires.get(channelID).remove(userID);
    }

    public void undisableMember(String guildID, String userID) {
        if (!disabledMembers.containsKey(guildID)) return;
        if (disabledMembers.get(guildID).contains(userID)) disabledMembers.get(guildID).remove(userID);
    }

    public OffsetDateTime getCooldown(String name) {
        return cooldowns.get(name);
    }

    public int getRemainingCooldown(String name) {
        if (cooldowns.containsKey(name)) {
            int time = (int)OffsetDateTime.now().until(cooldowns.get(name), ChronoUnit.SECONDS);
            if (time <= 0) {
                cooldowns.remove(name);
                return 0;
            }
            return time;
        }
        return 0;
    }

    public void applyCooldown(String name, int seconds)
    {
        cooldowns.put(name, OffsetDateTime.now().plusSeconds(seconds));
    }

    public void cleanCooldowns() {
        OffsetDateTime now = OffsetDateTime.now();
        cooldowns.keySet().stream().filter((str) -> (cooldowns.get(str).isBefore(now)))
                .collect(Collectors.toList()).stream().forEach(str -> cooldowns.remove(str));
    }

    public String getPrefix(String guildID) {
        return customPrefixes.getOrDefault(guildID, prefix);
    }

    public boolean isUserDisabled(String userID) {
        return disabledUsers.containsKey(userID) && disabledUsers.get(userID) >= 1;
    }

    public boolean canUseTickets(String userID) {
        return !(disabledUsers.containsKey(userID) && disabledUsers.get(userID) >= 0);
    }
    
    public boolean isMemberDisabled(MessageReceivedEvent event) {
        return event.isFromType(ChannelType.TEXT) && (disabledMembers.containsKey(event.getGuild().getId()) ? disabledMembers.get(event.getGuild().getId()).contains(event.getAuthor().getId()) : activeQuestionnaires.containsKey(event.getChannel().getId()) && activeQuestionnaires.get(event.getChannel().getId()).contains(event.getAuthor().getId()));
    }

    String getBotOwner() {
        return botOwner;
    }

    List<String> getBotAdmins() {
        return botAdmins;
    }

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getShardInfo().getShardId() == KekBot.shards - 1) {
            Config config = Config.getConfig();
            joinLogChannel = KekBot.jda.getTextChannelById(config.getJoinLogChannel());
            ticketChannel = KekBot.jda.getTextChannelById(config.getTicketChannel());
        }

        JDA jda = event.getJDA();
        jda.getGuilds().forEach(guild -> {
            Settings settings = Settings.getSettingsOrNull(guild.getId());
            if (settings == null) {
                settings = new Settings(guild);
                settings.save();
            }
        });
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot())
            return;

        String parts[] = null;
        String rawContent = event.getMessage().getContentRaw().replace("@everyone", "@\u200Beveryone").replace("@here", "@\u200Bhere");
        if (event.isFromType(ChannelType.TEXT)) {
            if (rawContent.startsWith(event.getGuild().getSelfMember().getAsMention()))
                parts = Arrays.copyOf(rawContent.substring(rawContent.indexOf(">")+1).trim().split("\\s+", 2), 2);
        }
        if (event.isFromType(ChannelType.PRIVATE)) {
            parts = Arrays.copyOf(rawContent.split("\\s+", 2), 2);
        }
        if (parts == null && customPrefixes.containsKey(event.getGuild().getId()) && rawContent.toLowerCase().startsWith(customPrefixes.get(event.getGuild().getId())))
            parts = Arrays.copyOf(rawContent.substring(customPrefixes.get(event.getGuild().getId()).length()).trim().split("\\s+", 2), 2);
        if (parts == null && !customPrefixes.containsKey(event.getGuild().getId())&& rawContent.toLowerCase().startsWith(prefix))
            parts = Arrays.copyOf(rawContent.substring(prefix.length()).trim().split("\\s+", 2), 2);

        if (parts != null && !isUserDisabled(event.getAuthor().getId()) && !isMemberDisabled(event)) {
            if (event.isFromType(ChannelType.PRIVATE) || event.getTextChannel().canTalk()) {
                String name = parts[0];
                String[] args = parts[1] == null ? new String[0] : parts[1].split("\\s+");

                final Command command;
                synchronized (commandIndex) {
                    int i = commandIndex.getOrDefault(name.toLowerCase(), -1);
                    command = i != -1? commands.get(i) : null;
                }


                if (command != null) {
                    CommandEvent cevent = new CommandEvent(event, args, this);
                    command.run(cevent);
                }
            }
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        if(event.getGuild().getSelfMember().getJoinDate().plusMinutes(10).isBefore(OffsetDateTime.now()))
            return;

        if (!Config.getConfig().getBlockedUsers().containsKey(event.getGuild().getOwner().getUser().getId()) || (Config.getConfig().getBlockedUsers().containsKey(event.getGuild().getOwner().getUser().getId()) && Config.getConfig().getBlockedUsers().get(event.getGuild().getOwner().getUser().getId()) < 2)) {
            joinLogChannel.sendMessage("Joined server: \"" + event.getGuild().getName() + "\" (ID: " + event.getGuild().getId() + ")").queue();
            Settings settings = new Settings(event.getGuild().getId());
            settings.save();

            for (TextChannel channel : event.getGuild().getTextChannels()) {
                if (channel.canTalk()) {
                    channel.sendMessage(joinSpeech).queue();
                    break;
                }
                //Loop will end if there's not a channel the bot can speak in.
            }
            Utils.sendStats(event.getJDA());
        } else {
            event.getGuild().leave().queue();
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        //todo probably make it so it deletes table entries on rethinkdb too...
        if (!Config.getConfig().getBlockedUsers().containsKey(event.getGuild().getOwner().getUser().getId())) {
            joinLogChannel.sendMessage("Left/Kicked from server: \"" + event.getGuild().getName() + "\" (ID: " + event.getGuild().getId() + ")").queue();
            //File folder = new File("settings/" + event.getGuild().getId());
            //Utils.deleteDirectory(folder);
            Utils.sendStats(event.getJDA());
        }
    }
}
