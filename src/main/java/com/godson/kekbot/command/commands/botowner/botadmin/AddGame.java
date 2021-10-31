package com.godson.kekbot.command.commands.botowner.botadmin;

import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.settings.Config;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AddGame extends Command {

    public AddGame() {
        name = "addgame";
        category = new Category("Bot Admin");
        commandPermission = CommandPermission.ADMIN;
    }

    @Override
    public void onExecuted(CommandEvent event) {
        Config config = Config.getConfig();
        if (config.getBotAdmins().contains(event.getMessage().getAuthor().getId()) || event.getMessage().getAuthor().getId().equals(config.getBotOwner())) {
            TextChannel channel = event.getTextChannel();
            if (event.getArgs().length > 0) {
                String game = event.combineArgs();
                try {
                    List<String> games = FileUtils.readLines(new File("games.txt"), "utf-8");
                    if (!games.contains(game)) {
                        try {
                            FileUtils.writeStringToFile(new File("games.txt"), "\n" + game, "utf-8", true);
                            channel.sendMessage("Added __**" + game + "**__ to the list of games.").queue();
                        } catch (IOException | PermissionException e) {
                            e.printStackTrace();
                        }
                    } else {
                        channel.sendMessage("__**" + game + "**__ is already in my list of games!").queue();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else channel.sendMessage("Failed to add game, due to the lack of a game you were supposed to give.").queue();
        }
    }
}
