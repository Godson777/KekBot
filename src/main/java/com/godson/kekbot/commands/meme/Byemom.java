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
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class Byemom {
    public static Command byemom = new Command("byemom")
            .withDescription("Creates your own \"OK BYE MOM\" meme.")
            .withCategory(CommandCategory.MEME)
            .withUsage("{p}byemom <message>")
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
                            String[] args = context.getArgs();
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < args.length; i++) {
                                if (!(args[i].equals("") && builder.length() < 1)) builder.append(args[i]);
                                if (i + 1 != args.length) builder.append(" ");
                            }
                            String search = builder.toString();
                            channel.sendTyping().queue();
                            try {
                                BufferedImage template = ImageIO.read(new File("resources/memegen/byemom_template.png"));
                                Graphics2D image = template.createGraphics();
                                URL userAva = new URL(context.getAuthor().getAvatarUrl());
                                URLConnection connection = userAva.openConnection();
                                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                                connection.connect();
                                BufferedImage ava = ImageIO.read(connection.getInputStream());
                                image.drawImage(ava, 523, 12, 80, 80, null);
                                image.drawImage(ava, 73, 338, 128, 128, null);
                                image.rotate(-0.436332);
                                image.setColor(Color.black);
                                Font font = new Font("Ariel", Font.PLAIN, 20);
                                while (font.getStringBounds(search, image.getFontRenderContext()).getWidth() > 372 && font.getSize() > 10) {
                                    font = new Font("Ariel", Font.PLAIN, font.getSize()-1);
                                }
                                image.setFont(font);
                                image.drawString(search, 69, 701);
                                image.dispose();
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                ImageIO.setUseCache(false);
                                ImageIO.write(template, "png", stream);
                                channel.sendFile(stream.toByteArray(), "byemom.png", null).queue();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            channel.sendMessage("You must supply some text for this command!").queue();
                        }
                    } else {
                        channel.sendMessage(KekBot.respond(context, Action.MEME_NOT_APPLIED, "__**Living Meme**__")).queue();
                    }
                }
            });
}
