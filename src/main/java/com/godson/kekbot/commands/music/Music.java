package com.godson.kekbot.commands.music;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Optional;

public class Music {
    public static Command music = new Command("music")
            .withDescription("Lists all the music commands.")
            .withCategory(CommandCategory.GENERAL)
            .withUsage("{p}music")
            .onExecuted(context -> {
                context.getTextChannel().sendMessage(KekBot.replacePrefix(context.getGuild(), "Music Commands: " +
                        "\n{p}queue - **Queues a music track.**" +
                        "\n{p}skip - **Skips a track. (Host Only)**" +
                        "\n{p}song - **Gets the current song info.**" +
                        "\n{p}playlist - **Lists all the tracks that are in the queue.**" +
                        "\n{p}volume - **Sets the volume. (Host Only)**" +
                        "\n{p}host - **Makes someone else the \"Host\". (Host Only)**" +
                        "\n{p}stop - **Stops the current music session. (Host Only)**" +
                        "\n{p}pause - **Pauses the current music session. (Host Only)**" +
                        "\n{p}repeat - **Toggles repeat mode, switched from OFF, SINGLE, and MULTI. (Host Only)**" +
                        "\n" +
                        "\nAll \"Host Only\" commands can also be executed by a user with `Administrator` permissions.")).queue();
            });
}
