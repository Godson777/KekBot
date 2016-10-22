package com.godson.kekbot.command.commands.general;

import com.darichey.discord.Commands4J;
import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.EasyMessage;
import com.godson.kekbot.KekBot;
import sx.blah.discord.Discord4J;

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
                EasyMessage.send(context.getMessage().getChannel(), "```xl\nVersion: " + KekBot.version +
                        "\nLibrary: Discord4J" +
                        "\nDiscord4J Version: " + Discord4J.VERSION +
                        "\nCommands4J Version: 1.1.0-MODDED" +
                        "\nRAM: " + (used / mb) + "MB / " + (total / mb) + "MB " + "(" + usedPercent + "% Used)" +
                        "\nAllocated: " + ((max / mb) < 1024 ? (total / mb) + "MB / " + (max / mb) + "MB " : (total / mb) < 1024 ? (total / mb) + "MB / " + Float.valueOf(format.format((float) max / gb)) + "GB " : Float.valueOf(format.format((float) total / gb) + "GB / " + Float.valueOf(format.format((float) max / gb)) + "GB ")) + "(" + allocatedPercent + "% Used)" +
                        "\nServers: " + KekBot.client.getGuilds().size() +
                        "\nChannels: " + KekBot.client.getChannels().size() +
                        "\nVoice Channels: " + KekBot.client.getVoiceChannels().size() +
                        "\nUsers: " + KekBot.client.getUsers().size() +
                        "\nCommands: " + CommandRegistry.getForClient(KekBot.client).getCommands().size() + "```");
            });
}
