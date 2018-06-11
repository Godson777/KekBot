package com.godson.kekbot.command.commands.weeb;

import me.duncte123.weebJava.models.WeebApi;

public class Lewd extends WeebCommand {

    public Lewd(WeebApi api) {
        super(api);
        name = "lewd";
        description = "For those l-lewd moments...";
        usage.add("lewd");
        type = "lewd";
        message = "command.weeb.lewd";
    }

}
