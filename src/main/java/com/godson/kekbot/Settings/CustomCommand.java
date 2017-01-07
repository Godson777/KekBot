package com.godson.kekbot.Settings;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandContext;
import com.darichey.discord.api.CommandRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class CustomCommand {
    public static final int TEXT = 0;
    public static final int IMAGE = 1;
    public static final int MENTION = 2;

    private String name;
    private int type;
    private String value;

    public CustomCommand() {}

    public void setName(String name) {
        this.name = name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void register(JDA jda, Guild guild) {
        CommandRegistry registry = CommandRegistry.getForClient(jda);
        switch (type) {
            case 0:
                registry.customRegister(new Command(name).onExecuted(context -> context.getTextChannel().sendMessage(value).queue()), guild);
                break;
            case 1:
                registry.customRegister(new Command(name).onExecuted(context -> {
                    try {
                        context.getTextChannel().sendTyping().queue();
                        URL url = new URL(value);
                        URLConnection connection = url.openConnection();
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                        connection.connect();
                        context.getTextChannel().sendFile(connection.getInputStream(), url.getFile(), null).queue();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }), guild);
                break;
            case 2:
                registry.customRegister(new Command(name).onExecuted(context -> {
                    if (context.getArgs().length > 0) {
                        if (context.getMessage().getMentionedUsers().size() == 1) {
                            context.getTextChannel().sendMessage(value.replace("{}", context.getMessage().getMentionedUsers().get(0).getAsMention())).queue();
                        } else if (context.getMessage().getMentionedUsers().size() == 0) {
                            context.getTextChannel().sendMessage("The user you want to target must be @mentioned.").queue();
                        } else {
                            context.getTextChannel().sendMessage("You @mentioned too many users, you only need to @mention one user for this command.").queue();
                        }
                    } else {
                        context.getTextChannel().sendMessage("You need to @mention someone to use this command.").queue();
                    }
                }), guild);
                break;
        }
    }

    public void register(CommandContext context) {
            register(context.getJDA(), context.getGuild());
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this, this.getClass());
    }

    public void save(Guild guild) {
        File folder = new File("settings/" + guild.getId() + "/commands");
        File cmd = new File("settings/" + guild.getId() + "/commands/" + this.getName() + ".json");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try {
            FileWriter writer = new FileWriter(cmd);
            writer.write(this.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAndRegister(CommandContext context) {
        register(context.getJDA(), context.getGuild());
        save(context.getGuild());
    }

    public void remove(JDA jda, Guild guild) {
        File cmd = new File("settings/" + guild.getId() + "/commands/" + this.getName() + ".json");
        cmd.delete();
        CommandRegistry.getForClient(jda).customUnregister(this.getName(), guild);
    }

    public void remove(CommandContext context) {
        remove(context.getJDA(), context.getGuild());
    }
}

