package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

import java.io.File;
import java.util.Random;

public class Kiss extends Command {

    public Kiss() {
        name = "kiss";
        description = "Kisses a person.";
        usage.add("kiss <@user>");
        category = new Category("Fun");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getMessage().getMentionedUsers().size() < 1) {
            event.getChannel().sendMessage("You didn't target any users!").queue();
            return;
        }


        File kisses[] = new File("resources/kisses").listFiles();
        Random random = new Random();
        int index = random.nextInt(kisses.length);
        event.getChannel().sendTyping().queue();
        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder mBuilder = new MessageBuilder();
        builder.setTitle(event.getMessage().getMentionedUsers().get(0).getName() + " was kissed by " + event.getAuthor().getName() + ".");
        builder.setImage("attachment://" + kisses[index].getName());
        mBuilder.setEmbed(builder.build());
        event.getChannel().sendFile(kisses[index], kisses[index].getName(), mBuilder.build()).queue();
    }
}
