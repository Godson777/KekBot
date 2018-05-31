package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.commands.weeb.WeebCommand;
import me.duncte123.weebJava.models.WeebApi;

public class Discord extends WeebCommand {

    public Discord(WeebApi api) {
        super(api);
        name = "discord";
        description = "Because even *Discord* is a meme.";
        usage.add("discord");
        type = "discord_memes";
        message = "command.meme.discord";
    }

}
