package com.godson.kekbot;

import com.darichey.discord.api.CommandContext;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.EventWaiter.EventWaiter;
import com.godson.kekbot.Games.GamesManager;
import com.godson.kekbot.Music.MusicPlayer;
import com.godson.kekbot.Objects.WaifuManager;
import com.godson.kekbot.Profile.BackgroundManager;
import com.godson.kekbot.Profile.Rewards.Lottery.Lottery;
import com.godson.kekbot.Shop.BackgroundShop;
import com.godson.kekbot.Shop.TokenShop;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Objects.PollManager;
import com.godson.kekbot.commands.admin.*;
import com.godson.kekbot.commands.fun.*;
import com.godson.kekbot.commands.general.*;
import com.godson.kekbot.commands.meme.*;
import com.godson.kekbot.commands.music.*;
import com.godson.kekbot.commands.music.Queue;
import com.godson.kekbot.commands.owner.*;
import com.godson.kekbot.commands.test;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class KekBot {
    //Seting configs, and resources.
    private static int shards = GSONUtils.getConfig().getShards();
    public static JDA[] jdas = new JDA[shards];
    public static final String version;
    public static long startTime = System.currentTimeMillis();
    public static BufferedImage genericAvatar;
    private static Map<Action, List<String>> responses = new HashMap<>();


    //ALL THE MANAGERS.
    public static PollManager manager = new PollManager();
    public static EventWaiter waiter = new EventWaiter();
    public static MusicPlayer player = new MusicPlayer();
    public static GamesManager gamesManager = new GamesManager();
    public static TokenShop tokenShop = new TokenShop();
    public static BackgroundManager backgroundManager = new BackgroundManager();
    public static BackgroundShop backgroundShop = new BackgroundShop();
    public static WaifuManager waifuManager = new WaifuManager();
    public static Lottery lottery = new Lottery();

    static {
        //TODO: Remove this later in favor of hardcoding the version, instead of relying on a .properties file.
        InputStream stream = KekBot.class.getClassLoader().getResourceAsStream("kekbot.properties");
        java.util.Properties properties = new java.util.Properties();
        try {
            properties.load(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        version = properties.getProperty("kekbot.version");

        try {
            genericAvatar = ImageIO.read(new File("resources/discordGeneric.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws LoginException, InterruptedException, RateLimitedException {
        String token = GSONUtils.getConfig().getToken();

        if (shards == 0) {
            System.out.println("You must enter the number of shards in your \"config.json\"! Please go back and specify it before launching.");
            System.exit(0);
        }

        if (token == null) {
            System.out.println("Token was not specified in \"config.json\"! Please go back and specify one before launching!");
        } else {
            if (shards > 1) {
                for (int i = 0; i < shards; i++) {
                    jdas[i] = new JDABuilder(AccountType.BOT).setToken(token).useSharding(i, shards).buildAsync();
                }
            } else {
                jdas[0] = new JDABuilder(AccountType.BOT).setToken(token).buildAsync();
            }
            for (JDA jda : jdas) {
                jda.addEventListener(new Listener());
                jda.addEventListener(waiter);
                jda.addEventListener(gamesManager);

                CommandRegistry.getForClient(jda).registerAll(Help.help, Purge.purge, Say.say, Granddad.granddad, TicketCommand.ticket, Lenny.lenny,
                        Shrug.shrug, Credits.credits, Avatar.avatar, TagCommand.tagCommand, AddAllowedUser.addAllowedUser, AddGame.addGame, Triggered.triggered, Gril.gril,
                        Salt.salt, JustRight.justRight, GetInvite.getInvite, Ban.ban, Kick.kick, Prefix.prefix, AutoRole.autoRole, Announce.announce,
                        Broadcast.broadcast, Stats.stats, Google.google, Lmgtfy.lmgtfy, Bots.bots, Shutdown.shutdown, UrbanDictionary.UrbanDictionary,
                        Emojify.emojify, AllowedUsers.allowedUsers, CoinFlip.coinFlip, Roll.roll, ListServers.listServers, Strawpoll.strawpoll, Poll.poll,
                        Poll.vote, AddRole.addRole, RemoveRole.removeRole, Quote.quote, Support.support, Eval.eval, Byemom.byemom, Queue.queue,
                        Skip.skip, Playlist.playlist, Song.song, Stop.stop, Volume.volume, Host.host, Music.music, Pause.pause, VoteSkip.voteskip, Repeat.repeat, Invite.invite,
                        Erase.erase, Johnny.johnny, LongLive.longlive, BlockUser.blockUser, DELET.delet, AddPatron.addPatron, RemovePatron.removePatron,
                        Poosy.poosy, EightBall.eightBall, Pick.pick, GameCommand.game, ProfileCommand.profile, FullWidth.fullwidth, ShopCommand.shop, MyPlaylist.myPlaylist,
                        Rip.rip, RateWaifu.rateWaifu, Gabe.gabe, Changelog.changelog, LotteryCommand.lottery);
            }

            for (Action action : Action.values()) {
                List<String> responses = GSONUtils.getResponder(action).getResponses();
                KekBot.responses.put(action, responses);
            }
        }
    }

    public static String respond(CommandContext context, Action action, Object... blanks) {
        String[] toReplace = {"{user.mention}", "{user.name}", "{}", "{1}", "{2}", "{3}", "{4}"};
        String[] replacements = {context.getAuthor().getAsMention(), context.getAuthor().getName(), "%s", "%1$s", "%2$s", "%3$s", "%4$s"};
        try {
            return String.format(StringUtils.replaceEach(responses.get(action).get(new Random().nextInt(responses.get(action).size())), toReplace, replacements), blanks);
        } catch (IllegalArgumentException e) {
            return action.name();
        }
    }

    public static String respond(Action action, Object... blanks) {
        String[] toReplace = {"{}", "{1}", "{2}", "{3}", "{4}"};
        String[] replacements = {"%s", "%1$s", "%2$s", "%3$s", "%4$s"};
        try {
            return String.format(StringUtils.replaceEach(responses.get(action).get(new Random().nextInt(responses.get(action).size())), toReplace, replacements), blanks);
        } catch (IllegalArgumentException e) {
            return action.name();
        }
    }

    public static void addResponse(Action action, String response) {
        GSONUtils.getResponder(action).addResponse(response).save();
        if (responses.containsKey(action)) responses.get(action).add(response);
        else responses.put(action, new ArrayList<>()).add(response);
    }

    //TODO: This may wind up being depreciated in a later revision of The Fun Update, in favor of a "insertPrefix", which merely returns the prefix as a string, instead of going through an entire string just to replace one or two instances of "{p}".
    public static String replacePrefix(Guild guild, String contents) {
        return contents.replace("{p}",
                (CommandRegistry.getForClient(guild.getJDA()).getPrefixForGuild(guild) != null
                        ? CommandRegistry.getForClient(guild.getJDA()).getPrefixForGuild(guild) : "$"));
    }

    public static String insertPrefix(Guild guild) {
        return (CommandRegistry.getForClient(guild.getJDA()).getPrefixForGuild(guild) != null
                ? CommandRegistry.getForClient(guild.getJDA()).getPrefixForGuild(guild) : "$");
    }

}
