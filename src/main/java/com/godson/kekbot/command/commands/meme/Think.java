package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.commands.weeb.WeebCommand;
import me.duncte123.weebJava.models.WeebApi;

public class Think extends WeebCommand {

    public Think(WeebApi api) {
        super(api);
        name = "think";
        description = "Pfft, thinking emotes are so 2017.";
        usage.add("think");
        type = "thinking";
        message = "command.meme.think";
        category = CommandCategories.meme;
    }

}
