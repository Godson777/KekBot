package com.godson.kekbot.command.commands.weeb;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import me.duncte123.weebJava.models.WeebApi;
import me.duncte123.weebJava.types.NSFWMode;
import net.dv8tion.jda.api.EmbedBuilder;

public class WeebCommand extends Command {

    protected WeebApi api;
    protected String type;
    protected String message;

    protected WeebCommand(WeebApi api) {
        this.api = api;
        category = new Category("Weeb");
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        event.getChannel().sendTyping().queue();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(event.getString(message));
        builder.setImage(api.getRandomImage(type, event.getTextChannel().isNSFW() ? NSFWMode.ALLOW_NSFW : NSFWMode.DISALLOW_NSFW).execute().getUrl());
        builder.setFooter("Powered by Weeb.sh!", null);
        event.getChannel().sendMessage(builder.build()).queue();
    }

    public static class MentionCommand extends WeebCommand {

        MentionCommand(WeebApi api) {
            super(api);
        }

        @Override
        public void onExecuted(CommandEvent event) throws Throwable {
            if (event.getMentionedUsers().size() < 1) {
                event.getChannel().sendMessage(event.getString("command.weeb.nomention")).queue();
                return;
            }


            event.getChannel().sendTyping().queue();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(event.getString(message, event.getMentionedUsers().get(0).getName(), event.getAuthor().getName()));
            builder.setImage(api.getRandomImage(type, event.getTextChannel().isNSFW() ? NSFWMode.ALLOW_NSFW : NSFWMode.DISALLOW_NSFW).execute().getUrl());
            builder.setFooter("Powered by Weeb.sh!", null);
            event.getChannel().sendMessage(builder.build()).queue();
        }
    }
}
