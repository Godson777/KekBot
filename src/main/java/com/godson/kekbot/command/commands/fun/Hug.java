package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

import java.io.File;
import java.util.Random;

public class Hug extends Command {

    public Hug() {
        name = "hug";
        description = "Hugs a person.";
        usage.add("hug <@user>");
        category = new Category("Fun");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getMentionedUsers().size() < 1) {
            event.getChannel().sendMessage("You didn't target any users!").queue();
            return;
        }


        File hugs[] = new File("resources/hugs").listFiles();
        Random random = new Random();
        int index = random.nextInt(hugs.length);
        event.getChannel().sendTyping().queue();
        EmbedBuilder builder = new EmbedBuilder();
        MessageBuilder mBuilder = new MessageBuilder();
        builder.setTitle(event.getMentionedUsers().get(0).getName() + " was hugged by " + event.getAuthor().getName() + ".");
        builder.setImage("attachment://" + hugs[index].getName());
        mBuilder.setEmbed(builder.build());
        event.getChannel().sendFile(hugs[index], hugs[index].getName(), mBuilder.build()).queue();
    }
}
