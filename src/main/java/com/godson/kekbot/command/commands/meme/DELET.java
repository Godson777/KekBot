package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.util.Utils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.CommandEvent;
import me.duncte123.weebJava.configs.ImageConfig;
import me.duncte123.weebJava.types.NSFWMode;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class DELET extends Command {

    public DELET() {
        name = "delet";
        description = "DELETS a user.";
        usage.add("delet <@user>");
        category = CommandCategories.meme;
    }


    @Override
    public void onExecuted(CommandEvent event) throws Throwable {
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage(event.getString("command.meme.delet.noargs")).queue();
            return;
        }

        if (!isMention(event.combineArgs()) && event.combineArgs().equals("this")) {
            event.getChannel().sendTyping().queue();
            EmbedBuilder builder = new EmbedBuilder();
            ImageConfig image = new ImageConfig.Builder().setType("delet_this").setNsfwMode(event.getTextChannel().isNSFW() ? NSFWMode.ALLOW_NSFW : NSFWMode.DISALLOW_NSFW).build();
            builder.setImage(KekBot.weebApi.getRandomImage(image).execute().getUrl());
            builder.setFooter("Powered by Weeb.sh!", null);
            event.getChannel().sendMessageEmbeds(builder.build()).queue();
            return;
        }

        if (!isMention(event.combineArgs()) || event.getMentionedUsers().size() < 1) {
            event.getChannel().sendMessage(event.getString("command.meme.delet.nomention")).queue();
            return;
        }


        event.getChannel().sendTyping().queue();
        Member member = event.getGuild().getMemberById(event.getMentionedUsers().get(0).getId());
        BufferedImage target = Utils.getUserAvatarImage(member.getUser());
        try {
            BufferedImage template = ImageIO.read(new File("resources/memegen/DELET_template.png"));
            BufferedImage bg = new BufferedImage(template.getWidth(), template.getHeight(), template.getType());
            Graphics2D image = bg.createGraphics();
            image.drawImage(target, 40, 147, 40, 40, null);
            image.drawImage(template, 0, 0, null);
            Font font = new Font("Whitney", Font.BOLD, 12);
            image.setColor(member.getColor());
            image.setFont(font);
            image.drawString(event.getMentionedUsers().get(0).getName(), 100, 162);
            image.dispose();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.flush();
            ImageIO.setUseCache(false);
            ImageIO.write(bg, "png", stream);
            event.getChannel().sendFile(stream.toByteArray(), "delet.png").queue();
            stream.close();
        } catch (IOException e) {
            throwException(e, event, "Image generation problem.");
        }
    }
}
