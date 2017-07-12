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

public class LongLive {
    public static Command longlive = new Command("longlive")
            .withCategory(CommandCategory.MEME)
            .withDescription("LONG LIVE THE KING!")
            .withUsage("{p}longlive <@user>")
            .onExecuted(context -> {
                TextChannel channel = context.getTextChannel();
                Guild server = context.getGuild();
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
                                    BufferedImage template = ImageIO.read(new File("resources/memegen/longlivetheking_template.png"));
                                    Graphics2D image = template.createGraphics();
                                    URL targetAva = new URL(context.getMessage().getMentionedUsers().get(0).getAvatarUrl());
                                    URLConnection connection = targetAva.openConnection();
                                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                                    connection.connect();
                                    BufferedImage target = ImageIO.read(connection.getInputStream());
                                    URL userAva = new URL(context.getAuthor().getAvatarUrl());
                                    URLConnection connection2 = userAva.openConnection();
                                    connection2.setRequestProperty("User-Agent", "Mozilla/5.0");
                                    connection2.connect();
                                    BufferedImage ava = ImageIO.read(connection2.getInputStream());
                                    image.drawImage(ava, 1026, 42, 479, 479, null);
                                    image.drawImage(target, 503, 558, 442, 442, null);
                                    image.dispose();
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    ImageIO.setUseCache(false);
                                    ImageIO.write(template, "png", stream);
                                    channel.sendFile(stream.toByteArray(), "theking.png", null).queue();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else channel.sendMessage("The user you want to target must be in the form of a mention!").queue();
                        } else channel.sendMessage("Who are you targeting?").queue();
                    } else {
                        channel.sendMessage(KekBot.respond(context, Action.MEME_NOT_APPLIED, "__**Living Meme**__")).queue();
                    }
                }
            });
}