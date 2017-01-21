package com.godson.kekbot;

import com.darichey.discord.api.CommandContext;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.EventWaiter.EventWaiter;
import com.godson.kekbot.Games.TicTacToe;
import com.godson.kekbot.Music.MusicPlayer;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Objects.PollManager;
import com.godson.kekbot.commands.admin.*;
import com.godson.kekbot.commands.fun.*;
import com.godson.kekbot.commands.general.*;
import com.godson.kekbot.commands.meme.*;
import com.godson.kekbot.commands.music.*;
import com.godson.kekbot.commands.music.Queue;
import com.godson.kekbot.commands.owner.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class KekBot {
    public static JDA[] jdas;
    public static final String version;
    public static PollManager manager = new PollManager();
    public static long startTime = System.currentTimeMillis();
    public static EventWaiter waiter = new EventWaiter();
    private static Map<Action, List<String>> responses = new HashMap<>();
    public static MusicPlayer player = new MusicPlayer();

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

    public static void main(String[] args) throws LoginException, InterruptedException, RateLimitedException {
        String token = GSONUtils.getConfig().getToken();
        int shards = GSONUtils.getConfig().getShards();
        if (shards == 0) {
            System.out.println("You must enter the number of shards in your \"config.json\"! Please go back and specify it before launching.");
            System.exit(0);
        }
        jdas = new JDA[shards];

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

                CommandRegistry.getForClient(jda).registerAll(Help.help, Purge.purge, Say.say, Granddad.granddad, TicketCommand.ticket, Lenny.lenny,
                        Shrug.shrug, Credits.credits, Avatar.avatar, TagCommand.tagCommand, AddAllowedUser.addAllowedUser, AddGame.addGame, Triggered.triggered, Gril.gril,
                        Salt.salt, JustRight.justRight, GetInvite.getInvite, Ban.ban, Kick.kick, Prefix.prefix, AutoRole.autoRole, Announce.announce,
                        Broadcast.broadcast, Stats.stats, Google.google, Lmgtfy.lmgtfy, Bots.bots, Shutdown.shutdown, UrbanDictionary.UrbanDictionary,
                        Emojify.emojify, AllowedUsers.allowedUsers, CoinFlip.coinFlip, Roll.roll, ListServers.listServers, Strawpoll.strawpoll, Poll.poll,
                        Poll.vote, AddRole.addRole, RemoveRole.removeRole, Quote.quote, Support.support, Eval.eval, Byemom.byemom, Queue.queue,
                        Skip.skip, Playlist.playlist, Song.song, Stop.stop, Volume.volume, Host.host, Music.music, Invite.invite, Erase.erase, Johnny.johnny,
                        LongLive.longlive, BlockUser.blockUser, CustomCMD.customCMD, DELET.delet, AddPatron.addPatron, RemovePatron.removePatron, Poosy.poosy, Whereis.test,
                        EightBall.eightBall, Pick.pick);
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

    public static String replacePrefix(Guild guild, String contents) {
        return contents.replace("{p}",
                (CommandRegistry.getForClient(guild.getJDA()).getPrefixForGuild(guild) != null
                        ? CommandRegistry.getForClient(guild.getJDA()).getPrefixForGuild(guild) : "$"));
    }

    public static String convertMillisToHMmSs(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        return (hours > 0 ? hours + ":" : "") +
                (minutes > 0 ? minutes + ":" : "0:") +
                (seconds > 0 ? (seconds > 9 ? seconds : "0" + seconds) : "00");
    }

    public static String convertMillisToTime(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) -
                TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        return (days != 0 ? days + (days > 1 ? " Days, " : " Day, ") : "") +
                (hours != 0 ? hours + (hours > 1 ? " Hours, " : " Hour, ") : "") + minutes +
                (minutes != 1 ? " Minutes and " : " Minute and ") + seconds + (seconds != 1 ? " Seconds." : " Second.");
    }

    public static String songTimestamp(long current, long length) {
        long currentHours = TimeUnit.MILLISECONDS.toHours(current);
        long currentMinutes = TimeUnit.MILLISECONDS.toMinutes(current) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(current));
        long currentSeconds = TimeUnit.MILLISECONDS.toSeconds(current) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(current));
        long lengthHours = TimeUnit.MILLISECONDS.toHours(length);
        long lengthMinutes = TimeUnit.MILLISECONDS.toMinutes(length) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(length));
        long lengthSeconds = TimeUnit.MILLISECONDS.toSeconds(length) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(length));
        return (currentHours > 0 ? currentHours + ":" : (lengthHours > 0 ? "0:" : "")) +
                (currentMinutes > 0 ? (currentMinutes > 9 ? currentMinutes : (lengthMinutes > 9 ? "0" + currentMinutes : currentMinutes)) : (lengthMinutes > 9 || lengthHours > 0 ? "00" : (lengthMinutes > 0 ? "0" : ""))) + ":" +
                (currentSeconds > 0 ? (currentSeconds > 9 ? currentSeconds : "0" + currentSeconds) : "00") +
                "/" +
                (lengthHours > 0 ? lengthHours + ":" : "") +
                (lengthMinutes > 0 ? lengthMinutes : (lengthHours > 0 ? "00" : "")) + ":" +
                (lengthSeconds > 9 ? lengthSeconds : "0" + lengthSeconds);
    }

    public static String removeWhitespaceEdges(String string) {
        if (string.matches(".*\\w.*")) {
            if (string.startsWith(" ")) string = string.replaceFirst("([ ]+)", "");
            if (string.endsWith(" ")) string = string.replaceAll("([ ]+$)", "");
        } else string = "";
        return string;
    }

}
