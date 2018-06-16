package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public class Jontron extends Command {

    public Jontron() {
        name = "jontron";
        description = "Ech ~Jontron";
        category = CommandCategories.meme;
        usage.add("jontron");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        boolean reboot = (Arrays.stream(event.getArgs()).anyMatch(s -> s.equalsIgnoreCase("--reboot")));

        File jontrons[] = new File(reboot ? "resources/sound/jontron/reboot" : "resources/sound/jontron").listFiles();
        Random random = new Random();
        int index = random.nextInt(jontrons.length);
        Optional<VoiceChannel> voiceChannel = event.getEvent().getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(event.getEvent().getMember())).findFirst();
        if (!voiceChannel.isPresent()) {
            event.getEvent().getChannel().sendMessage(KekBot.respond(Action.GET_IN_VOICE_CHANNEL, event.getLocale())).queue();
        } else {
            KekBot.player.loadAndMeme(event, jontrons[index].getAbsolutePath());
        }
    }
}
