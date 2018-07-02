package com.godson.kekbot;

import com.godson.discoin4j.Discoin4J;
import com.godson.kekbot.command.commands.MarkovTest;
import com.godson.kekbot.command.commands.admin.*;
import com.godson.kekbot.command.commands.botowner.*;
import com.godson.kekbot.command.commands.botowner.botadmin.AddGame;
import com.godson.kekbot.command.commands.botowner.botadmin.Reboot;
import com.godson.kekbot.command.commands.botowner.botadmin.Responses;
import com.godson.kekbot.command.commands.botowner.botadmin.Takeover;
import com.godson.kekbot.command.commands.meme.*;
import com.godson.kekbot.command.commands.weeb.*;
import com.godson.kekbot.games.GamesManager;
import com.godson.kekbot.music.MusicPlayer;
import com.godson.kekbot.objects.DiscoinManager;
import com.godson.kekbot.objects.MarkovChain;
import com.godson.kekbot.objects.TakeoverManager;
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
import com.rethinkdb.gen.exc.ReqlDriverError;
import com.rethinkdb.net.Connection;
import me.duncte123.weebJava.WeebApiBuilder;
import me.duncte123.weebJava.models.WeebApi;
import me.duncte123.weebJava.types.TokenType;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import org.apache.commons.lang3.StringUtils;
import org.discordbots.api.client.DiscordBotListAPI;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class KekBot {
    //Seting configs, and resources.
    public static int shards = Config.getConfig().getShards();
    public static ShardManager jda;
    public static final Version version = new Version(1, 6, 0, 1);
    public static final long startTime = System.currentTimeMillis();
    public static BufferedImage genericAvatar;
    private static final Map<Action, List<String>> responses = new HashMap<>();
    public static final RethinkDB r = RethinkDB.r;
    public static Connection conn;
    private static final Random random = new Random();
    private static final CommandClient client = new CommandClient();
    public static final MiscListener listener = new MiscListener();
    public static final MarkovChain chain = new MarkovChain();
    public static Icon pfp;
    public static boolean dev;
    public static WeebApi weebApi = new WeebApiBuilder(TokenType.WOLKETOKENS, "KekBot/1.5.1").setToken(Config.getConfig().getWeebToken()).build();
    public static DiscordBotListAPI dbl;


    //ALL THE MANAGERS.
    public static final EventWaiter waiter = new EventWaiter(Executors.newSingleThreadScheduledExecutor(), false);
    public static final MusicPlayer player = new MusicPlayer();
    public static final GamesManager gamesManager = new GamesManager();
    public static BackgroundManager backgroundManager = new BackgroundManager();
    public static TokenShop tokenShop;
    public static BackgroundShop backgroundShop;
    public static final Lottery lottery = new Lottery();
    public static Discoin4J discoin;
    public static DiscoinManager discoinManager;
    public static TwitterManager twitterManager;
    private static TakeoverManager takeoverManager;
    public static ShutdownListener shutdownListener = new ShutdownListener();

    //Misc stuff
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    //Some variables that need to be setup with a try statement
    static {
        try {
            tokenShop = new TokenShop();
            backgroundShop = new BackgroundShop();
            genericAvatar = ImageIO.read(new File("resources/discordGeneric.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws LoginException {
        boolean beta = false;
        boolean dev = false;

        if (Config.getConfig().getdBotsListToken() != null) dbl = new DiscordBotListAPI.Builder().token(Config.getConfig().getdBotsListToken()).build();
        else dbl = null;

        for (String arg : args) {
            if (arg.equalsIgnoreCase("--beta")) beta = true;
            if (arg.equalsIgnoreCase("--dev") && !beta) {
                dev = true;
                KekBot.dev = true;
            }
        }


        try {
            if (beta) pfp = Icon.from(new File("resources/pfpBeta.png"));
            else if (dev) pfp = Icon.from(new File("resources/pfpDev.png"));
            else pfp = Icon.from(new File("resources/pfp.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(ExitCode.GENERIC_ERROR.getCode());
        }

        if (dev) twitterManager = null;
        else twitterManager = new TwitterManager(chain);
        Config config = Config.getConfig();
        try {
            conn = r.connection().user(config.getDbUser(), config.getDbPassword()).connect();
        } catch (ReqlDriverError e) {
            System.exit(ExitCode.DB_OFFLINE.getCode());
        }
        if (beta && (boolean) r.dbList().contains(config.getBetaDatabase()).run(conn)) conn.use(config.getBetaDatabase());
        else if (r.dbList().contains(config.getDatabase()).run(conn)) conn.use(config.getDatabase());
        else {
            System.out.println("Database could not be found, are you sure you typed the name correctly?");
            System.exit(ExitCode.SHITTY_CONFIG.getCode());
        }
        takeoverManager = new TakeoverManager();
        String token = beta ? config.getBetaToken() : config.getToken();
        if (config.getDcoinToken() != null && !beta) {
            discoin = new Discoin4J(config.getDcoinToken());
            discoinManager = new DiscoinManager();
        }
        if (beta) {
            shards = 1;
            client.setPrefix("$$");
            twitterManager = null;
        }
        else {
            if (shards == 0) {
                System.out.println("You must enter the number of shards in your \"config.json\"! Please go back and specify it before launching.");
                System.exit(ExitCode.SHITTY_CONFIG.getCode());
            }
        }

        if (token == null) {
            System.out.println("Token was not specified in \"config.json\"! Please go back and specify one before launching!");
            System.exit(ExitCode.SHITTY_CONFIG.getCode());
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
            //client.addCommand(new RIP());
            client.addCommand(new Quote());
            client.addCommand(new UDCommand());
            client.addCommand(new ShopCommand());
            client.addCommand(new Music());
            client.addCommand(new Pay());
            client.addCommand(new Daily());

            //Weeb Commands
            client.addCommand(new Slap(weebApi));
            client.addCommand(new Hug(weebApi));
            client.addCommand(new Kiss(weebApi));
            client.addCommand(new Punch(weebApi));
            client.addCommand(new Awoo(weebApi));
            client.addCommand(new Cuddle(weebApi));
            client.addCommand(new Lick(weebApi));
            client.addCommand(new Lewd(weebApi));
            client.addCommand(new Neko(weebApi));
            client.addCommand(new Pout(weebApi));
            client.addCommand(new Shrug(weebApi));
            client.addCommand(new Pat(weebApi));
            client.addCommand(new Cry(weebApi));
            client.addCommand(new Dance(weebApi));
            client.addCommand(new Nom(weebApi));
            client.addCommand(new Poke(weebApi));
            client.addCommand(new OwO(weebApi));
            client.addCommand(new Sleepy(weebApi));
            client.addCommand(new Smug(weebApi));
            client.addCommand(new SRS(weebApi));
            client.addCommand(new ThumbsUp(weebApi));
            client.addCommand(new Wag(weebApi));
            client.addCommand(new Dab(weebApi));
            client.addCommand(new Deredere(weebApi));
            client.addCommand(new Tickle(weebApi));
            client.addCommand(new Bite(weebApi));

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
            client.addCommand(new Doubt());
            client.addCommand(new Kaede());
            client.addCommand(new Magik());
            client.addCommand(new Think(weebApi));
            client.addCommand(new Discord(weebApi));
            client.addCommand(new Kirb());
            client.addCommand(new YouTried());



            client.addCommand(new TestCommand());
            client.addCommand(new MarkovTest(chain));


            //Bot Owner and Bot Admin commands.
            client.addCommand(new Responses());
            client.addCommand(new BotAdmin());
            client.addCommand(new BotMod());
            client.addCommand(new AddGame());
            client.addCommand(new Shutdown());
            client.addCommand(new Patreon());
            client.addCommand(new GetInvite());
            client.addCommand(new Eval());
            client.addCommand(new Tweet());
            client.addCommand(new Takeover(takeoverManager));
            client.addCommand(new BlockUser());
            client.addCommand(new Reboot());


            jda = new DefaultShardManagerBuilder().setToken(token).addEventListeners(waiter, client, gamesManager, listener, player, shutdownListener).setShardsTotal(shards).build();
            if (twitterManager != null) jda.addEventListener(twitterManager);

            if (!takeoverManager.isTakeoverActive()) {
                for (Action action : Action.values()) {
                    List<String> responses = Responder.getResponder(action).getResponses();
                    KekBot.responses.put(action, responses);
                }
            }
        }
    }

    public static CommandClient getCommandClient() {
        return client;
    }

    /**
     * Responds to an action, with optional blanks being filled by the values given.
     * @param action The action to respond to.
     * @param locale The locale the guild is using.
     * @param blanks The values that will fill the blanks.
     * @return The response.
     */
    public static String respond(Action action, String locale, String... blanks) {
        String[] toReplace = {"{}", "{1}", "{2}", "{3}", "{4}"};
        String[] replacements = {"%s", "%1$s", "%2$s", "%3$s", "%4$s"};
        if (locale.equals(client.getDefaultLocale())) {
            try {
                return String.format(StringUtils.replaceEach(responses.get(action).get(random.nextInt(responses.get(action).size())), toReplace, replacements), blanks);
            } catch (IllegalArgumentException e) {
                return String.format(StringUtils.replaceEach(LocaleUtils.getString(action.getUnlocalizedMessage(), locale), toReplace, replacements), blanks);
            }
        } else return String.format(StringUtils.replaceEach(LocaleUtils.getString(action.getUnlocalizedMessage(), locale), toReplace, replacements), blanks);
    }

    /**
     * Adds a "response" to KekBot's list of "responses" for that action.
     * @param action The action KekBot will respond to.
     * @param response The response.
     */
    public static void addResponse(Action action, String response) {
        Responder.getResponder(action).addResponse(response).save();
        if (responses.containsKey(action)) responses.get(action).add(response);
        else {
            List<String> strings = new ArrayList<>();
            strings.add(response);
            responses.put(action, strings);
        }
    }

    public static List<String> getResponses(Action action) {
        return responses.getOrDefault(action, null);
    }

    public static void removeResponse(Action action, int response) {
        if (responses.containsKey(action) && (response < responses.get(action).size() && response > -1)) {
            responses.get(action).remove(response);

            if (!takeoverManager.isTakeoverActive()) Responder.getResponder(action).removeResponse(response).save();
            else {
                TakeoverManager.Takeover takeover = takeoverManager.getCurrentTakeover();
                takeover.getResponses().get(action).remove(response);
                takeover.save();
            }
        }
    }

    public static void takeoverReponses(Map<Action, List<String>> tResponses) {
        responses.clear();
        responses.putAll(tResponses);
    }

    public static void resetResponses() {
        if (!responses.isEmpty()) responses.clear();
        for (Action action : Action.values()) {
            List<String> responses = Responder.getResponder(action).getResponses();
            KekBot.responses.put(action, responses);
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

    public static String getGuildLocale(Guild guild) {
        return client.getLocale(guild.getId());
    }

    /**
     * Prepares some objects for shutting down.
     * @param reason
     */
    public static void shutdown(String reason) {
        jda.removeEventListener(client);
        KekBot.player.shutdown(reason);
        KekBot.gamesManager.shutdown(reason);
        lottery.emergencyDraw();
        listener.shutdown();
        if (twitterManager != null) twitterManager.shutdown(reason);
        waiter.shutdown();
        KekBot.schedule(() -> {
            for (JDA jda : KekBot.jda.getShards()) jda.shutdown();
        }, 1L, TimeUnit.MINUTES);
    }

    public static void schedule(Runnable task, Long delay, TimeUnit timeUnit) {
        scheduler.schedule(task, delay, timeUnit);
    }

    public static ScheduledFuture<?> scheduleRepeat(Runnable task, long startDelay, long repeatDelay) {
        return scheduler.scheduleWithFixedDelay(task, startDelay, repeatDelay, TimeUnit.MILLISECONDS);
    }

}
