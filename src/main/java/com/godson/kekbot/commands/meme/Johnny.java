package com.godson.kekbot.commands.meme;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Utils;
import net.dv8tion.jda.core.entities.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Johnny {
    public static Command johnny = new Command("johnny")
            .withCategory(CommandCategory.MEME)
            .withDescription("HEREEEE'S JOHNNY!")
            .withUsage("{p}johnny <@user>")
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
                                User user = context.getMessage().getMentionedUsers().get(0);
                                BufferedImage target = Utils.getUserAvatarImage(user);
                                BufferedImage ava = Utils.getUserAvatarImage(context.getAuthor());
                                try {
                                    BufferedImage template = ImageIO.read(new File("resources/memegen/johnny_template.png"));
                                    BufferedImage bg = new BufferedImage(template.getWidth(), template.getHeight(), template.getType());
                                    Graphics2D image = bg.createGraphics();
                                    image.drawImage(ava, 111, 218, 283, 282, null);
                                    image.drawImage(template, 0, 0, null);
                                    image.drawImage(target, 250, -8, 81, 81, null);
                                    image.dispose();
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    ImageIO.setUseCache(false);
                                    ImageIO.write(bg, "png", stream);
                                    channel.sendFile(stream.toByteArray(), "jahnny.png", null).queue();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                channel.sendMessage("The user you want to target must be in the form of a mention!").queue();
                            }
                        } else {
                            channel.sendMessage("Who are you targeting?").queue();
                        }
                    } else {
                        channel.sendMessage(KekBot.respond(context, Action.MEME_NOT_APPLIED, "__**Living Meme**__")).queue();
                    }
                }
            });
}