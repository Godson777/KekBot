package com.godson.kekbot.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class Erase {
    public static Command erase = new Command("erase")
            .withCategory(CommandCategory.MEME)
            .withDescription("For really big mistakes.")
            .withUsage("{p}erase <@user>")
            .onExecuted(context -> {
                Guild server = context.getGuild();
                TextChannel channel = context.getTextChannel();
                List<Role> checkForMeme = server.getRolesByName("Living Meme", true);
                if (checkForMeme.size() == 0) {
                    channel.sendMessage(KekBot.respond(context, Action.MEME_NOT_FOUND, "__**Living Meme**__")).queue();
                } else {
                    Role meme = checkForMeme.get(0);
                    if (server.getSelfMember().getRoles().contains(meme)) {
                        if (context.getArgs().length > 0) {
                            if (context.getMessage().getMentionedUsers().size() > 0) {
                                channel.sendTyping().queue();
                                try {
                                    BufferedImage template = ImageIO.read(new File("mistake_template.png"));
                                    Graphics2D image = template.createGraphics();
                                    URL userAva = new URL(context.getMessage().getMentionedUsers().get(0).getAvatarUrl());
                                    URLConnection connection = userAva.openConnection();
                                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                                    connection.connect();
                                    BufferedImage ava = ImageIO.read(connection.getInputStream());
                                    image.drawImage(ava, 368, 375, 277, 270, null);
                                    image.dispose();
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    ImageIO.setUseCache(false);
                                    ImageIO.write(template, "png", stream);
                                    channel.sendFile(stream.toByteArray(), "erase.png", null).queue();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else channel.sendMessage("The user you wanna erase must be mentioned.").queue();
                        } else channel.sendMessage("Oh, alright. Guess we're not erasing anyone then...").queue();
                    } else {
                        channel.sendMessage(KekBot.respond(context, Action.MEME_NOT_APPLIED, "__**Living Meme**__")).queue();
                    }
                }
            });
}
