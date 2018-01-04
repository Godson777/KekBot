package com.godson.kekbot.command.commands.fun;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

public class Avatar extends Command {

    //Adds this to the URL so the avatar sent is larger.
    private final String size = "?size=1024";

    public Avatar() {
        name = "avatar";
        aliases = new String[]{"ava"};
        description = "Sends a larger version of the specified user's avatar. If there is no user specified, it'll send your avatar.";
        usage.add("avatar <user name>");
        usage.add("avatar <@user>");
        category = new Category("Fun");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length > 0) {
            if (isMention(event.combineArgs())) {
                event.getChannel().sendMessage(event.getEvent().getMessage().getMentionedUsers().get(0).getAvatarUrl() + size).queue();
            } else {
                List<Member> search = event.getGuild().getMembersByName(event.combineArgs(), true);
                if (search.size() == 0) search = event.getGuild().getMembersByNickname(event.combineArgs(), true);
                if (search.size() > 0) event.getChannel().sendMessage(search.get(0).getUser().getAvatarUrl() + size).queue();
                else event.getChannel().sendMessage("I couldn't find a user with that name/nickname!").queue();
            }
        } else {
            event.getChannel().sendMessage(event.getEvent().getMessage().getAuthor().getAvatarUrl() + size).queue();
        }
    }
}
