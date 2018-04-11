package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

import java.io.File;
import java.util.Random;

public class Slap extends Command {

    public Slap() {
        name = "slap";
        description = "Slaps a person.";
        usage.add("slap <@user>");
        category = new Category("Fun");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getMentionedUsers().size() < 1) {
            event.getChannel().sendMessage("You didn't target any users!").queue();
            return;
        }


        File slaps[] = new File("resources/slaps").listFiles();
        Random random = new Random();
        int index = random.nextInt(slaps.length);
        event.getChannel().sendTyping().queue();
        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder mBuilder = new MessageBuilder();
        builder.setTitle(event.getMentionedUsers().get(0).getName() + " was slapped by " + event.getAuthor().getName() + ".");
        builder.setImage("attachment://" + slaps[index].getName());
        mBuilder.setEmbed(builder.build());
        event.getChannel().sendFile(slaps[index], slaps[index].getName(), mBuilder.build()).queue();
    }
}
