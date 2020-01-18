package com.godson.kekbot;

import com.godson.kekbot.util.Utils;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutdownListener extends ListenerAdapter {

    private ExitCode exitCode = null;
    boolean softReboot;
    private ScheduledExecutorService updater = Executors.newSingleThreadScheduledExecutor();
    private String channelID;
    private Integer rebootedShard;

    public ShutdownListener() {
        updater.scheduleAtFixedRate(() -> {
            Version latest = Utils.getLatestVersion(KekBot.version.getBetaVersion() > 0);
            if (latest.isHigherThan(KekBot.version)) {
                KekBot.update();
            }
        }, 0,30, TimeUnit.MINUTES);
    }

    public void setExitCode(ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    public void softReboot(MessageChannel channel) {
        softReboot = true;
        channelID = channel.getId();
        KekBot.player.shutdown("Quick reboot, we'll be right back!");
        KekBot.gamesManager.shutdown("Quick reboot, we'll be right back!");
        KekBot.jda.restart();
    }

    public void softReboot(MessageChannel channel, int shard) {
        softReboot = true;
        channelID = channel.getId();
        handleShard(shard);
        KekBot.jda.restart(shard);
        rebootedShard = shard;
    }

    @Override
    public void onReady(ReadyEvent event) {
        if (KekBot.jda.getShardsQueued() > 0 && rebootedShard == null) return;
        if (softReboot) softReboot = false;
        if (channelID != null) {
            KekBot.jda.getTextChannelById(channelID).sendMessage("Success!").queue();
            channelID = null;
        }
        if (rebootedShard != null) rebootedShard = null;
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        if (softReboot) return;
        System.exit(exitCode.getCode());
    }

    private void handleShard(int shard) {
        KekBot.player.shutdownShard(shard);
        KekBot.gamesManager.shutdownShard(shard);
    }
}
