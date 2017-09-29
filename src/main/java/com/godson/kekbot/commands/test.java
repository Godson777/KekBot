package com.godson.kekbot.commands;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Listener;
import com.godson.kekbot.Music.Playlist;
import com.godson.kekbot.Profile.Badge;
import com.godson.kekbot.Profile.Profile;
import com.godson.kekbot.Profile.Token;
import com.godson.kekbot.commands.admin.*;
import com.godson.kekbot.commands.fun.*;
import com.godson.kekbot.commands.general.*;
import com.godson.kekbot.commands.meme.*;
import com.godson.kekbot.commands.music.*;
import com.godson.kekbot.commands.owner.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.Optional;

public class test {
    public static Command test = new Command("test")
            .onExecuted(context -> {
                //Empty for now.
            });
}
