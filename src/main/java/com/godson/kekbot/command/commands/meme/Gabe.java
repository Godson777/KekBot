package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public class Gabe extends Command {

    public Gabe() {
        name = "gabe";
        aliases = new String[]{"bork"};
        description = "\"Bork!\" ~Gabe 2k17 <3";
        usage.add("gabe");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        boolean reboot = (Arrays.stream(event.getArgs()).anyMatch(s -> s.equalsIgnoreCase("--reboot")));

        File gabes[] = Arrays.stream(new File(reboot ? "resources/sound/gabe/reboot" : "resources/sound/gabe").listFiles()).filter(file -> !file.isDirectory()).toArray(File[]::new);
        Random random = new Random();
        int index = random.nextInt(gabes.length);
        Optional<VoiceChannel> voiceChannel = event.getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(event.getMember())).findFirst();
        if (!voiceChannel.isPresent()) {
            event.getChannel().sendMessage(KekBot.respond(Action.GET_IN_VOICE_CHANNEL, event.getLocale())).queue();
        } else {
            KekBot.player.loadAndMeme(event, gabes[index].getAbsolutePath());
        }
    }
}
