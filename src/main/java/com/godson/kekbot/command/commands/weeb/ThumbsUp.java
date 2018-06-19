package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class ThumbsUp extends WeebCommand {

    public ThumbsUp(WeebApi api) {
        super(api);
        name = "thumbsup";
        description = "Because you definitely need virtual thumbs";
        usage.add("thumbsup");
        type = "thumbsup";
        message = "command.weeb.thumbsup";
    }

}
