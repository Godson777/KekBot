package com.godson.kekbot;

import com.godson.discoin4j.Discoin4J;
import com.godson.kekbot.command.commands.MarkovTest;
import com.godson.kekbot.command.commands.admin.*;
import com.godson.kekbot.command.commands.botowner.*;
import com.godson.kekbot.command.commands.botowner.botadmin.AddGame;
import com.godson.kekbot.command.commands.botowner.botadmin.Responses;
import com.godson.kekbot.command.commands.meme.*;
import com.godson.kekbot.games.GamesManager;
import com.godson.kekbot.music.MusicPlayer;
import com.godson.kekbot.objects.DiscoinManager;
import com.godson.kekbot.objects.MarkovChain;
import com.godson.kekbot.objects.TwitterManager;
import com.godson.kekbot.profile.BackgroundManager;
import com.godson.kekbot.profile.rewards.lottery.Lottery;
import com.godson.kekbot.responses.Responder;
import com.godson.kekbot.settings.Config;
import com.godson.kekbot.shop.BackgroundShop;
import com.godson.kekbot.shop.TokenShop;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.command.CommandClient;
import com.godson.kekbot.command.commands.TestCommand;
import com.godson.kekbot.command.commands.fun.*;
import com.godson.kekbot.command.commands.general.*;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Guild;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class KekBot {
    //Seting configs, and resources.
    public static int shards = Config.getConfig().getShards();
    public static ShardManager jda;
    public static final String version = "1.5-BETA2";
    public static final long startTime = System.currentTimeMillis();
    public static BufferedImage genericAvatar;
    private static final Map<Action, List<String>> responses = new HashMap<>();
    public static final RethinkDB r = RethinkDB.r;
    public static Connection conn;
    private static final Random random = new Random();
    private static final CommandClient client = new CommandClient();
    private static final MiscListener listener = new MiscListener();
    public static final MarkovChain chain = new MarkovChain();


    //ALL THE MANAGERS.
    public static final EventWaiter waiter = new EventWaiter();
    public static final MusicPlayer player = new MusicPlayer();
    public static final GamesManager gamesManager = new GamesManager();
    public static BackgroundManager backgroundManager = new BackgroundManager();
    public static TokenShop tokenShop;
    public static BackgroundShop backgroundShop;
    static {
        try {
            tokenShop = new TokenShop();
            backgroundShop = new BackgroundShop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static final Lottery lottery = new Lottery();
    public static Discoin4J discoin;
    public static DiscoinManager discoinManager;
    public static final TwitterManager twitterManager = new TwitterManager(chain);

    static {

        try {
            genericAvatar = ImageIO.read(new File("resources/discordGeneric.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws LoginException {
        boolean beta = false;
        for (String arg : args) if (arg.equalsIgnoreCase("--beta")) beta = true;
        Config config = Config.getConfig();
        conn = r.connection().user(config.getDbUser(), config.getDbPassword()).connect();
        if (beta && (boolean) r.dbList().contains(config.getBetaDatabase()).run(conn)) conn.use(config.getBetaDatabase());
        else if (r.dbList().contains(config.getDatabase()).run(conn)) conn.use(config.getDatabase());
        else {
            System.out.println("Database could not be found, are you sure you typed the name correctly?");
            System.exit(0);
        }
        String token = beta ? config.getBetaToken() : config.getToken();
        if (config.getDcoinToken() != null && !beta) {
            discoin = new Discoin4J(config.getDcoinToken());
            discoinManager = new DiscoinManager();
        }
        if (beta) {
            shards = 1;
            client.setPrefix("$$");
        }
        else {
            if (shards == 0) {
                System.out.println("You must enter the number of shards in your \"config.json\"! Please go back and specify it before launching.");
                System.exit(0);
            }
        }

        if (token == null) {
            System.out.println("Token was not specified in \"config.json\"! Please go back and specify one before launching!");
        } else {

            client.addCommand(new Prefix());

            //Admin Commands
            client.addCommand(new Role());
            client.addCommand(new Purge());
            client.addCommand(new Kick());
            client.addCommand(new Ban());
            client.addCommand(new SettingsCommand());
            client.addCommand(new GetRole());

            //Fun Commands
            client.addCommand(new Avatar());
            client.addCommand(new CoinFlip());
            client.addCommand(new EightBall());
            client.addCommand(new FullWidth());
            client.addCommand(new Balance());
            client.addCommand(new Lenny());
            client.addCommand(new LotteryCommand());
            client.addCommand(new GameCommand());
            client.addCommand(new RateWaifu());
            client.addCommand(new Emojify());
            client.addCommand(new Roll());
            client.addCommand(new ProfileCommand());
            client.addCommand(new Pick());
            client.addCommand(new RIP());
            client.addCommand(new Quote());
            client.addCommand(new UDCommand());
            client.addCommand(new ShopCommand());
            client.addCommand(new Music());
            client.addCommand(new Pay());
            client.addCommand(new Daily());
            client.addCommand(new Slap());
            client.addCommand(new Hug());
            client.addCommand(new Kiss());

            //General Commands
            client.addCommand(new Help());
            client.addCommand(new Stats());
            client.addCommand(new Poll(client));
            client.addCommand(new Strawpoll());
            client.addCommand(new GoogleSearch());
            client.addCommand(new LMGTFY());
            client.addCommand(new Changelog());
            client.addCommand(new Credits());
            client.addCommand(new Invite());
            client.addCommand(new TicketCommand());
            client.addCommand(new TagCommand());
            client.addCommand(new MyPlaylist());
            client.addCommand(new Support());
            client.addCommand(new Ping());

            //Meme Commands
            client.addCommand(new Granddad());
            client.addCommand(new Gril());
            client.addCommand(new JustRight());
            client.addCommand(new Byemom());
            client.addCommand(new Erase());
            client.addCommand(new Gabe());
            client.addCommand(new Poosy());
            client.addCommand(new LongLive());
            client.addCommand(new Johnny());
            client.addCommand(new DELET());
            client.addCommand(new GayBabyJail());
            client.addCommand(new Jontron());
            client.addCommand(new LuigiThumb());
            client.addCommand(new SwitchSetup());
            client.addCommand(new Gru());
            client.addCommand(new Lean());



            client.addCommand(new TestCommand());
            client.addCommand(new MarkovTest(chain));


            //Bot Owner and Bot Admin commands.
            client.addCommand(new Responses());
            client.addCommand(new BotAdmin());
            client.addCommand(new AddGame());
            client.addCommand(new Shutdown());
            client.addCommand(new Patreon());
            client.addCommand(new GetInvite());
            client.addCommand(new Eval());
            client.addCommand(new Tweet());




            jda = new DefaultShardManagerBuilder().setToken(token).addEventListeners(waiter, client, gamesManager, listener, player).setShardsTotal(shards).build();

            /*if (shards > 1) {
                for (int i = 0; i < shards; i++) {
                    jdas[i] = builder.useSharding(i, shards).buildAsync();
                }
            } else {
                jdas[0] = builder.buildAsync();
            }*/

            //for (JDA jda : jdas) {
                /*CommandRegistry.getForClient(jda).registerAll(Help.help, Purge.purge, Say.say, Granddad.granddad, TicketCommand.ticket, Lenny.lenny,
                        Shrug.shrug, Credits.credits, Avatar.avatar, TagCommand.tagCommand, AddAllowedUser.addAllowedUser, AddGame.addGame, Triggered.triggered, Gril.gril,
                        Salt.salt, JustRight.justRight, GetInvite.getInvite, Ban.ban, Kick.kick, Prefix.prefix, AutoRole.autoRole, Announce.announce,
                        Broadcast.broadcast, Stats.stats, Google.google, Lmgtfy.lmgtfy, Bots.bots, Shutdown.shutdown, UrbanDictionary.UrbanDictionary,
                        Emojify.emojify, AllowedUsers.allowedUsers, CoinFlip.coinFlip, Roll.roll, ListServers.listServers, Strawpoll.strawpoll, Poll.poll,
                        Poll.vote, AddRole.addRole, RemoveRole.removeRole, Quote.quote, Support.support, Eval.eval, Byemom.byemom, Queue.queue,
                        Skip.skip, Playlist.playlist, Song.song, Stop.stop, Volume.volume, Host.host, Music.music, Pause.pause, VoteSkip.voteskip, Repeat.repeat, Invite.invite,
                        Erase.erase, Johnny.johnny, LongLive.longlive, BlockUser.blockUser, DELET.delet, AddPatron.addPatron, RemovePatron.removePatron,
                        Poosy.poosy, EightBall.eightBall, Pick.pick, GameCommand.game, ProfileCommand.profile, FullWidth.fullwidth, ShopCommand.shop, MyPlaylist.myPlaylist,
                        Rip.rip, RateWaifu.rateWaifu, Gabe.gabe, Changelog.changelog, LotteryCommand.lottery, Shuffle.shuffle, Balance.balanace, SlotMachine.slotMachine,
                        Pay.pay);*/
            //}

            for (Action action : Action.values()) {
                List<String> responses = Responder.getResponder(action).getResponses();
                KekBot.responses.put(action, responses);
            }
        }
    }

    public static CommandClient getCommandClient() {
        return client;
    }

    /**
     * Responds to an action, with optional blanks being filled by the values given.
     * @param action The action to respond to.
     * @param blanks The values that will fill the blanks.
     * @return The response.
     */
    public static String respond(Action action, Object... blanks) {
        String[] toReplace = {"{}", "{1}", "{2}", "{3}", "{4}"};
        String[] replacements = {"%s", "%1$s", "%2$s", "%3$s", "%4$s"};
        try {
            return String.format(StringUtils.replaceEach(responses.get(action).get(random.nextInt(responses.get(action).size())), toReplace, replacements), blanks);
        } catch (IllegalArgumentException e) {
            return action.name();
        }
    }

    /**
     * Adds a "response" to KekBot's list of "responses" for that action.
     * @param action The action KekBot will respond to.
     * @param response The response.
     */
    public static void addResponse(Action action, String response) {
        Responder.getResponder(action).addResponse(response).save();
        if (responses.containsKey(action)) responses.get(action).add(response);
        else responses.put(action, new ArrayList<>()).add(response);
    }

    public static List<String> getResponses(Action action) {
        return responses.getOrDefault(action, null);
    }

    public static void removeResponse(Action action, int response) {
        if (responses.containsKey(action) && (response < responses.get(action).size() && response > -1)) {
            responses.get(action).remove(response);
            Responder.getResponder(action).removeResponse(response).save();
        }
    }

    /**
     * Only used when a certain area of code can't reach the command event.
     * @param guild The guild.
     * @return The guild's prefix.
     */
    public static String getGuildPrefix(Guild guild) {
        return client.getPrefix(guild.getId());
    }

    /**
     * Prepares some objects for shutting down.
     * @param reason
     */
    public static void shutdown(String reason) {
        jda.removeEventListener(client);
        KekBot.player.shutdown(reason);
        KekBot.gamesManager.shutdown(reason);
        lottery.forceDraw(false);
        listener.shutdown();
        twitterManager.shutdown(reason);
    }

}
