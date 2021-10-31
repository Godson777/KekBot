package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public class Granddad extends Command {

    public Granddad() {
        name = "granddad";
        description = "FLEEENSTONES!?";
        usage.add("granddad");
        category = CommandCategories.meme;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        boolean reboot = (Arrays.stream(event.getArgs()).anyMatch(s -> s.equalsIgnoreCase("--reboot")));

        File[] granddads = Arrays.stream(new File(reboot ? "resources/sound/granddad/reboot" : "resources/sound/granddad").listFiles()).filter(file -> !file.isDirectory()).toArray(File[]::new);
        Random random = new Random();
        int index = random.nextInt(granddads.length);
        Optional<VoiceChannel> voiceChannel = event.getEvent().getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(event.getEvent().getMember())).findFirst();
        if (!voiceChannel.isPresent()) {
            event.getEvent().getChannel().sendMessage(KekBot.respond(Action.GET_IN_VOICE_CHANNEL, event.getLocale())).queue();
        } else {
            KekBot.player.loadAndMeme(event, granddads[index].getAbsolutePath());
        }
    }
}
