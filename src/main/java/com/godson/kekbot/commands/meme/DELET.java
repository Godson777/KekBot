package com.godson.kekbot.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
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

public class DELET {
    public static Command delet = new Command("delet")
            .withCategory(CommandCategory.MEME)
            .withDescription("DELETS a user.")
            .withUsage("{p}delet <@user>")
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
                                    Member member = context.getGuild().getMemberById(context.getMessage().getMentionedUsers().get(0).getId());
                                    BufferedImage template = ImageIO.read(new File("DELET_template.png"));
                                    BufferedImage bg = new BufferedImage(template.getWidth(), template.getHeight(), template.getType());
                                    Graphics2D image = bg.createGraphics();
                                    URL targetAva = new URL(member.getUser().getAvatarUrl());
                                    URLConnection connection = targetAva.openConnection();
                                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                                    connection.connect();
                                    BufferedImage target = ImageIO.read(connection.getInputStream());
                                    image.drawImage(target, 40, 147, 40, 40, null);
                                    image.drawImage(template, 0, 0, null);
                                    Font font = new Font("Whitney", Font.BOLD, 12);
                                    image.setColor(member.getColor());
                                    image.setFont(font);
                                    image.drawString(context.getMessage().getMentionedUsers().get(0).getName(), 100, 162);
                                    image.dispose();
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    ImageIO.setUseCache(false);
                                    ImageIO.write(bg, "png", stream);
                                    channel.sendFile(stream.toByteArray(), "png", null).queue();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                channel.sendMessage("The user you want to DELET must be in the form of a mention!").queue();
                            }
                        } else {
                            channel.sendMessage("Who are you going to DELET?").queue();
                        }
                    } else {
                        channel.sendMessage(KekBot.respond(context, Action.MEME_NOT_APPLIED, "__**Living Meme**__")).queue();
                    }
                }
            });
}