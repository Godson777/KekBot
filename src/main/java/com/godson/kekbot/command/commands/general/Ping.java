package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.TimeZone;

public class Ping extends Command {

    public Ping() {
        name = "ping";
        description = "Returns with the bot's ping.";
        category = new Category("General");
        usage.add("ping");
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        event.getChannel().sendMessage("Pinging... ").queue(m -> m.editMessage("\uD83C\uDFD3 Pong! `" + event.getMessage().getCreationTime().until(m.getCreationTime(), ChronoUnit.MILLIS)+ "ms`" +
                "\n\uD83D\uDC93 Heartbeat: `" + event.getJDA().getPing() + "ms`").queue());
    }
}
