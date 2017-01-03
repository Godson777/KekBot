package com.godson.kekbot.commands.general;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.KekBot;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;

import java.text.DecimalFormat;

public class Stats {
    public static Command stats = new Command("stats")
            .withCategory(CommandCategory.GENERAL)
            .withDescription("Prints KekBot's stats.")
            .withUsage("{p}stats")
            .onExecuted(context -> {
                Runtime runtime = Runtime.getRuntime();
                int mb = 1024 * 1024;
                int gb = 1024 * 1024 * 1024;
                long used = runtime.totalMemory() - runtime.freeMemory();
                long total = runtime.totalMemory();
                long max = runtime.maxMemory();
                float usedPercent = (float) ((used * 100) / total);
                float allocatedPercent = (float) ((total * 100) / max);
                DecimalFormat format = new DecimalFormat("#.##");
                int totalGuilds = 0;
                int totalChannels = 0;
                int totalVoiceChannels = 0;
                long totalUsers = 0;
                long startTime = System.currentTimeMillis() - KekBot.startTime;
                /*startTime /= 1000;
                long days = startTime / 86400;
                startTime %= 86400;
                long hours = startTime / 3600;
                startTime %= 3600;
                long minutes = startTime / 60;
                startTime %= 60;
                String time = (days != 0 ? days + (days > 1 ? " Days, " : " Day, ") : "") +
                        (hours != 0 ? hours + (hours > 1 ? " Hours, " : " Hour, ") : "") + minutes +
                        (minutes != 1 ? " Minutes and " : " Minute and ") + startTime + (startTime != 1 ? " Seconds." : " Second.");*/
                for (JDA jda : KekBot.jdas) {
                    totalGuilds += jda.getGuilds().size();
                    totalChannels += jda.getTextChannels().size();
                    totalVoiceChannels += jda.getVoiceChannels().size();
                    totalUsers += jda.getUsers().size();
                }
                context.getTextChannel().sendMessage(
                        "```xl\nVersion: " + KekBot.version +
                        "\nLibrary: JDA" +
                        "\nJDA Version: " + JDAInfo.VERSION +
                        "\nCommands4J Version: 1.1.0-MODDED" +
                        "\nRAM: " + (used / mb) + "MB / " + (total / mb) + "MB " + "(" + usedPercent + "% Used)" +
                        "\nAllocated: " + ((max / mb) < 1024 ? (total / mb) + "MB / " + (max / mb) + "MB " : (total / mb) < 1024 ? (total / mb) + "MB / " + Float.valueOf(format.format((float) max / gb)) + "GB " : Float.valueOf(format.format((float) total / gb)) + "GB / " + Float.valueOf(format.format((float) max / gb)) + "GB ") + "(" + allocatedPercent + "% Used)" +
                        "\nActive for: " + KekBot.convertMillisToTime(startTime) +
                        //"\nShards: " + KekBot.jdas.length +
                        "\nServers: " + context.getJDA().getGuilds().size() +
                        //"\nTotal Servers: " + totalGuilds +
                        "\nChannels: " + context.getJDA().getTextChannels().size() +
                        //"\nTotal Channels: " + totalChannels +
                        "\nVoice Channels: " + context.getJDA().getVoiceChannels().size() +
                        //"\nTotal Voice Channels: " + totalVoiceChannels +
                        "\nUsers: " + context.getJDA().getUsers().size() +
                        //"\nTotal Users: " + totalUsers +
                        "\nCommands: " + CommandRegistry.getForClient(context.getJDA()).getCommands().size() + "```").queue();
            });
}
