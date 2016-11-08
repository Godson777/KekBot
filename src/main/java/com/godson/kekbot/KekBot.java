package com.godson.kekbot;

import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.Settings.PollManager;
import com.godson.kekbot.Settings.UserStates;
import com.godson.kekbot.command.commands.admin.*;
import com.godson.kekbot.command.commands.fun.*;
import com.godson.kekbot.command.commands.general.*;
import com.godson.kekbot.command.commands.meme.*;
import com.godson.kekbot.command.commands.owner.*;
import com.godson.kekbot.command.commands.owner.Shutdown;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;

public class KekBot {
    public static JDA[] jdas = new JDA[1];
    public static final String version;
    public static UserStates states = new UserStates();
    public static PollManager manager = new PollManager();
    public static long startTime = System.currentTimeMillis();

    static {
        InputStream stream = KekBot.class.getClassLoader().getResourceAsStream("kekbot.properties");
        java.util.Properties properties = new java.util.Properties();

        try {
            properties.load(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        version = properties.getProperty("kekbot.version");
    }

    public static void main(String[] args) throws LoginException, InterruptedException {
        String token = GSONUtils.getConfig().getToken();

        if (token == null) {
            System.out.println("Token was not specified in \"config.json\"! Please go back and specify one before launching!");
        } else {
            jdas[0] = new JDABuilder().setBotToken(token).buildAsync();
            for (JDA jda : jdas) {
                jda.addEventListener(new Listener());

                CommandRegistry.getForClient(jda).registerAll(Help.help, Purge.purge, Say.say, Granddad.granddad, TicketCommand.ticket, Lenny.lenny,
                        Shrug.shrug, Credits.credits, Avatar.avatar, TagCommand.tagCommand, AddAllowedUser.addAllowedUser, AddGame.addGame, Triggered.triggered, Gril.gril,
                        Salt.salt, JustRight.justRight, Zombo.zombo, GetInvite.getInvite, Ban.ban, Kick.kick, Prefix.prefix, AutoRole.autoRole, Announce.announce,
                        Broadcast.broadcast, Stats.stats, Google.google, Lmgtfy.lmgtfy, Bots.bots, Shutdown.shutdown, UrbanDictionary.UrbanDictionary,
                        Emojify.emojify, AllowedUsers.allowedUsers, CoinFlip.coinFlip, Roll.roll, ListServers.listServers, Strawpoll.strawpoll, Poll.poll,
                        Poll.vote, Poosy.destroyer, AddRole.addRole, RemoveRole.removeRole, Quote.quote);
            }
        }
    }


}
