package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.util.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;

import java.awt.*;
import java.text.DecimalFormat;

public class Stats extends Command {
    private final Runtime runtime = Runtime.getRuntime();
    private final int mb = 1024 * 1024;
    private final int gb = 1024 * 1024 * 1024;
    private final DecimalFormat format = new DecimalFormat("#.##");

    public Stats() {
        name = "stats";
        description = "Prints KekBot's stats.";
        usage.add("stats");
        usage.add("stats nerds");
        category = new Category("General");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        //prepare stats
        long startTime = System.currentTimeMillis() - KekBot.startTime;
        long used = runtime.totalMemory() - runtime.freeMemory();
        long total = runtime.totalMemory();
        long max = runtime.maxMemory();
        long totalChannels = KekBot.jda.getTextChannelCache().size();
        long totalVoiceChannels = KekBot.jda.getVoiceChannelCache().size();
        float usedPercent = (float) ((used * 100) / total);
        float allocatedPercent = (float) ((total * 100) / max);
        long totalGuilds = KekBot.jda.getGuildCache().size();
        long totalUsers = KekBot.jda.getUserCache().size();

        //save for later, might be useful for later.
        //((max / mb) < 1024 ? (total / mb) + "MB / " + (max / mb) + "MB " : (total / mb) < 1024 ? (total / mb) + "MB / " + Float.valueOf(format.format((float) max / gb)) + "GB " : Float.valueOf(format.format((float) total / gb)) + "GB / " + Float.valueOf(format.format((float) max / gb)) + "GB ")


        String[] args = event.getArgs();
        String arg = args.length == 0 ? "" : args[0].toLowerCase();

        //dev version
        if (arg.equals("nerds") || arg.equals("dev")) {
            boolean dev = arg.equals("dev");
            StringBuilder builder = new StringBuilder();
            builder.append("Version: " + KekBot.version + "");
            builder.append("\nLibrary: JDA");
            builder.append("\nJDA Version: " + JDAInfo.VERSION);
            builder.append("\nRAM: " + shortenMemory(used) + " / " + shortenMemory(total) + " (" + usedPercent + "% Used)");
            builder.append("\nAllocated: " + shortenMemory(total) + " / " + shortenMemory(max) + " (" + allocatedPercent + "% Used)");
            builder.append("\nActive for: " + Utils.convertMillisToTime(startTime));
            if (KekBot.shards > 1) {
                builder.append("\nCurrent Shard: " + (event.getJDA().getShardInfo().getShardId() + 1));
                builder.append("\nNumber of Queued Shards: " + KekBot.jda.getShardsQueued());
                builder.append("\nNumber of Running Shards: " + KekBot.jda.getShardsRunning());
                builder.append("\nTotal Number of Shards: " + KekBot.jda.getShardsTotal());
                builder.append("\nChannels (In this Shard): " + event.getJDA().getTextChannelCache().size());
                builder.append("\nVoice Channels (In this Shard): " + event.getJDA().getVoiceChannelCache().size());
                builder.append("\nUsers (In this Shard): " + event.getJDA().getUserCache().size());
                builder.append("\nTotal Servers: " + totalGuilds);
                builder.append("\nTotal Channels: " + totalChannels);
                builder.append("\nTotal Voice Channels: " + totalVoiceChannels);
                builder.append("\nTotal Users: " + totalUsers);
            } else {
                builder.append("\nServers: " + event.getJDA().getGuildCache().size());
                builder.append("\nChannels: " + event.getJDA().getTextChannelCache().size());
                builder.append("\nVoice Channels: " + event.getJDA().getVoiceChannelCache().size());
                builder.append("\nUsers: " + event.getJDA().getUserCache().size());
            }
            builder.append("\nCommands: " + event.getClient().getCommands().size());
            if (dev) {
                builder.append("\nMusic Players: " + KekBot.player.getActivePlayerCount());
                builder.append("\nActive Games: " + KekBot.gamesManager.getActiveGames());
            }

            event.getChannel().sendMessage("```xl\n" + builder.toString() + "```").queue();
            return;
        }

        //normal version
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(event.getSelfUser().getAvatarUrl());
        builder.setTitle("KekBot, your friendly all-in-one meme bot!");
        builder.addField("Online for:", Utils.convertMillisToTime(startTime), false);
        builder.addField("Version:", KekBot.version.toString(), true);
        builder.addField("Number of Commands:", String.valueOf(event.getClient().getCommands().size()), false);
        if (KekBot.shards > 1) {
            long guildsInShard = event.getJDA().getGuildCache().size();
            int currentShard = event.getJDA().getShardInfo().getShardId() + 1;
            builder.addField("Servers:", totalGuilds + " (" + guildsInShard + " in shard)", true);
            builder.addField("Shard:", currentShard + "/" + KekBot.shards, true);
        } else {
            builder.addField("Servers:", String.valueOf(totalGuilds), true);
        }
        builder.setColor(event.getSelfMember().getColor() != null ? event.getSelfMember().getColor() : Color.RED);

        event.getChannel().sendMessage(builder.build()).queue();
    }

    private String shortenMemory(long mem) {
        if ((mem / mb) < 1024) return (mem / mb) + "MB";
        else return Float.valueOf(format.format((float) mem / gb)) + "GB";
    }
}
