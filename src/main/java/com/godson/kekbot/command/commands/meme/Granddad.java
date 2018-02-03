package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.Utils;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.io.File;
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
        if (Utils.getResource("sound/granddad").isDirectory()) {
            File granddads[] = Utils.getResource("sound/granddad").listFiles();
            Random random = new Random();
            int index = random.nextInt(granddads.length);
            Optional<VoiceChannel> voiceChannel = event.getEvent().getGuild().getVoiceChannels().stream().filter(c -> c.getMembers().contains(event.getEvent().getMember())).findFirst();
            if (!voiceChannel.isPresent()) {
                event.getEvent().getChannel().sendMessage(KekBot.respond(Action.GET_IN_VOICE_CHANNEL)).queue();
            } else {
                KekBot.player.loadAndMeme(event, granddads[index].getAbsolutePath());
            }
        }
    }
}
