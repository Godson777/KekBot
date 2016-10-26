package com.godson.kekbot;

import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.command.UserStates;
import com.godson.kekbot.command.commands.admin.*;
import com.godson.kekbot.command.commands.fun.*;
import com.godson.kekbot.command.commands.general.*;
import com.godson.kekbot.command.commands.meme.*;
import com.godson.kekbot.command.commands.owner.*;
import com.godson.kekbot.command.commands.owner.Shutdown;
import com.godson.kekbot.command.commands.ping;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import org.apache.commons.io.FileUtils;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class KekBot {
    public static JDA client;
    public static final String version;
    public static UserStates states = new UserStates();

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
        String token = null;
        File tokenFile = new File("token.txt");
        if (tokenFile.exists()) {
            try {
                token = FileUtils.readFileToString(new File("token.txt"), "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            client = new JDABuilder().setBotToken(token).buildAsync();
            client.addEventListener(new Listener());

            CommandRegistry.getForClient(client).register(ping.test);
            CommandRegistry.getForClient(client).register(Help.help);
            CommandRegistry.getForClient(client).register(Purge.purge);
            CommandRegistry.getForClient(client).register(Say.say);
            CommandRegistry.getForClient(client).register(Granddad.granddad);
            CommandRegistry.getForClient(client).register(Ticket.ticket);
            CommandRegistry.getForClient(client).register(Lenny.lenny);
            CommandRegistry.getForClient(client).register(Shrug.shrug);
            CommandRegistry.getForClient(client).register(Credits.credits);
            CommandRegistry.getForClient(client).register(Avatar.avatar);
            CommandRegistry.getForClient(client).register(Tag.tag);
            CommandRegistry.getForClient(client).register(AddAllowedUser.addAllowedUser);
            CommandRegistry.getForClient(client).register(AddGame.addGame);
            CommandRegistry.getForClient(client).register(Triggered.triggered);
            CommandRegistry.getForClient(client).register(Gril.gril);
            CommandRegistry.getForClient(client).register(Salt.salt);
            CommandRegistry.getForClient(client).register(JustRight.justRight);
            CommandRegistry.getForClient(client).register(Zombo.zombo);
            CommandRegistry.getForClient(client).register(GetInvite.getInvite);
            CommandRegistry.getForClient(client).register(Ban.ban);
            CommandRegistry.getForClient(client).register(Kick.kick);
            CommandRegistry.getForClient(client).register(Prefix.prefix);
            CommandRegistry.getForClient(client).register(AutoRole.autoRole);
            CommandRegistry.getForClient(client).register(Announce.announce);
            CommandRegistry.getForClient(client).register(Broadcast.broadcast);
            CommandRegistry.getForClient(client).register(Stats.stats);
            CommandRegistry.getForClient(client).register(Google.google);
            CommandRegistry.getForClient(client).register(Lmgtfy.lmgtfy);
            CommandRegistry.getForClient(client).register(Bots.bots);
            CommandRegistry.getForClient(client).register(Shutdown.shutdown);
            CommandRegistry.getForClient(client).register(UrbanDictionary.UrbanDictionary);
            CommandRegistry.getForClient(client).register(Emojify.emojify);
            CommandRegistry.getForClient(client).register(AllowedUsers.allowedUsers);
            CommandRegistry.getForClient(client).register(CoinFlip.coinFlip);
            CommandRegistry.getForClient(client).register(Roll.roll);
            CommandRegistry.getForClient(client).register(ListServers.listServers);
        } else {
            System.out.println("Token not found! (Make sure the filename is \"token.txt\", it's case sensitive.");
        }
    }


}
