package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class SRS extends WeebCommand {

    public SRS(WeebApi api) {
        super(api);
        name = "srs";
        description = "bruh u srs";
        usage.add("srs");
        type = "stare";
        message = "command.weeb.srs";
    }

}
