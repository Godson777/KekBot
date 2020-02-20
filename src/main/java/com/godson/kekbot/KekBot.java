package com.godson.kekbot;

import com.godson.discoin4j.Discoin4J;
import com.godson.kekbot.command.commands.MarkovTest;
import com.godson.kekbot.command.commands.admin.*;
import com.godson.kekbot.command.commands.botowner.*;
import com.godson.kekbot.command.commands.botowner.botadmin.*;
import com.godson.kekbot.command.commands.meme.*;
import com.godson.kekbot.command.commands.social.TwitterCommand;
import com.godson.kekbot.command.commands.weeb.*;
import com.godson.kekbot.games.GamesManager;
import com.godson.kekbot.music.MusicPlayer;
import com.godson.kekbot.objects.DiscoinManager;
import com.godson.kekbot.objects.MarkovChain;
import com.godson.kekbot.objects.TakeoverManager;
import com.godson.kekbot.objects.TwitterManager;
import com.godson.kekbot.profile.item.BackgroundManager;
import com.godson.kekbot.profile.rewards.lottery.Lottery;
import com.godson.kekbot.responses.Responder;
import com.godson.kekbot.settings.Config;
import com.godson.kekbot.settings.Settings;
import com.godson.kekbot.shop.BackgroundShop;
import com.godson.kekbot.shop.TokenShop;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.command.CommandClient;
import com.godson.kekbot.command.commands.TestCommand;
import com.godson.kekbot.command.commands.fun.*;
import com.godson.kekbot.command.commands.general.*;
import com.godson.kekbot.util.LocaleUtils;
import com.godson.kekbot.util.Utils;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.exc.ReqlDriverError;
import com.rethinkdb.net.Connection;
import me.duncte123.weebJava.WeebApiBuilder;
import me.duncte123.weebJava.models.WeebApi;
import me.duncte123.weebJava.types.TokenType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.entities.Icon;
import org.apache.commons.lang3.StringUtils;
import org.discordbots.api.client.DiscordBotListAPI;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import twitter4j.conf.ConfigurationBuilder;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

public class KekBot {
    //Seting configs, and resources.
    public static int shards = Config.getConfig().getShards();
    public static ShardManager jda;
    public static final Version version = new Version(1, 6, 1, 1);
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
    public static WeebApi weebApi;
    public static DiscordBotListAPI dbl;
    private static HttpServer server;

    //Twitter config
    public static ConfigurationBuilder twitterConfig;


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

    // Base URI the Grizzly HTTP server will listen on
    private static HttpServer startServer(int mode) {
        String BASE_URI = Config.getConfig().getAPIip();
        if (mode == 1) BASE_URI = Config.getConfig().getAPIip() + "test/";
        if (mode == 2) BASE_URI = "http://localhost:8081/myapp/";
        final ResourceConfig rc = new ResourceConfig().packages("com.godson.kekbot.api");

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws LoginException {
        //Fuck you Linux, we're using UTF-8 whether you like it or not.
        System.setProperty("file.encoding", "UTF-8");

        int mode = 0;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--beta")) mode = 1;
            if (arg.equalsIgnoreCase("--dev") && mode != 1) {
                mode = 2;
                KekBot.dev = true;
            }
        }
        server = startServer(mode);
        setupOptionalResources(mode);
        startBot(mode);
    }

    private static void setupOptionalResources(int mode) {
        //Load config
        Config config = Config.getConfig();

        if (config.getdBotsListToken() != null) dbl = new DiscordBotListAPI.Builder().token(config.getdBotsListToken()).build();
        if (config.getWeebToken() != null) weebApi = new WeebApiBuilder(TokenType.WOLKETOKENS, "KekBot/" + version.toString()).setToken(config.getWeebToken()).build();
        if (config.usingTwitter()) {
            System.out.println("Using Twitter.");
            /*
            TODO: make this a config value, that way we can use twitter to follow tweets, but not to send tweets.
            Mostly useful to those who wish to run their own instance of KekBot.
             */
            if (mode == 0) {
                //We can use twitter, but if we want to avoid tweeting, we run with either --dev or --beta.
                System.out.println("Not Beta/Dev build, checking for auto-tweet keys...");
                if (config.getTwConsumerKey() == null) {
                    System.out.println("Twitter Consumer Key not specified in \"config.json\"! Please go back and specify this value before launching!");
                    System.exit(ExitCode.SHITTY_CONFIG.getCode());
                    return;
                }
                if (config.getTwConsumerSecret() == null) {
                    System.out.println("Twitter Consumer Key Secret not specified in \"config.json\"! Please go back and specify this value before launching!");
                    System.exit(ExitCode.SHITTY_CONFIG.getCode());
                    return;
                }
                if (config.getTwAccessToken() == null) {
                    System.out.println("Twitter Access Token not specified in \"config.json\"! Please go back and specify this value before launching!");
                    System.exit(ExitCode.SHITTY_CONFIG.getCode());
                    return;
                }
                if (config.getTwAccessTokenSecret() == null) {
                    System.out.println("Twitter Access Token Secret not specified in \"config.json\"! Please go back and specify this value before launching!");
                    System.exit(ExitCode.SHITTY_CONFIG.getCode());
                    return;
                }
                System.out.println("All values found!");
                twitterConfig = new ConfigurationBuilder()
                        .setOAuthConsumerKey(config.getTwConsumerKey())
                        .setOAuthConsumerSecret(config.getTwConsumerSecret())
                        .setOAuthAccessToken(config.getTwAccessToken())
                        .setOAuthAccessTokenSecret(config.getTwAccessTokenSecret());
            }
        }

        if (config.getDcoinToken() != null && mode == 0) {
            discoin = new Discoin4J(config.getDcoinToken());
            discoinManager = new DiscoinManager();
        }
    }

    private static void startBot(int mode) throws LoginException {
        //Modes: 0 = stable, 1 = beta, 2 = dev.

        switch (mode) {
            default:
                try {
                    pfp = Icon.from(new File("resources/pfp.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                try {
                    pfp = Icon.from(new File("resources/pfpBeta.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    pfp = Icon.from(new File("resources/pfpDev.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

        //Load config
        Config config = Config.getConfig();

        //Load twitter
        if (config.usingTwitter()) {
            //We can use twitter, but if we want to avoid tweeting, we run with either --dev or --beta.
            twitterManager = new TwitterManager(chain, mode == 0);
        }

        String token = mode == 1 ? config.getBetaToken() : config.getToken();

        try {
            conn = r.connection().user(config.getDbUser(), config.getDbPassword()).connect();
        } catch (ReqlDriverError e) {
            System.out.println("There was an error logging into rethinkdb, are you sure that it's on, or that you typed the info correctly?");
            System.exit(ExitCode.DB_OFFLINE.getCode());
        }
        if (mode == 1) {
            if (config.getBetaDatabase() == null)  {
                System.out.println("There was no database to use provided. Make sure \"betaDatabase\" is in your config.json.");
                System.exit(ExitCode.SHITTY_CONFIG.getCode());
            }
            if (!(boolean) r.dbList().contains(config.getBetaDatabase()).run(conn)) {
                r.dbCreate(config.getBetaDatabase()).run(conn);
                System.out.println("Database wasn't found, so it was created.");
            }
            conn.use(config.getBetaDatabase());
            verifyTables();
        } else {
            if (config.getDatabase() == null)  {
                System.out.println("There was no database to use provided. Make sure \"database\" is in your config.json.");
                System.exit(ExitCode.SHITTY_CONFIG.getCode());
            }
            if (!(boolean) r.dbList().contains(config.getDatabase()).run(conn)) {
                r.dbCreate(config.getDatabase()).run(conn);
                System.out.println("Database wasn't found, so it was created.");
            }
            conn.use(config.getDatabase());
            verifyTables();
        }

        //Load takeovers
        takeoverManager = new TakeoverManager();

        if (token == null) {
            System.out.println("Token was not specified in \"config.json\"! Please go back and specify one before launching!");
            System.exit(ExitCode.SHITTY_CONFIG.getCode());
        } else {

            if (mode == 1) {
                shards = 1;
                client.setPrefix("$$");
            } else {
                if (shards == 0) {
                    System.out.println("You must enter the number of shards in your \"config.json\"! Please go back and specify it before launching.");
                    System.exit(ExitCode.SHITTY_CONFIG.getCode());
                }
            }


            jda = new DefaultShardManagerBuilder().setToken(token).setShardsTotal(shards).build();
            if (twitterManager != null) jda.addEventListener(twitterManager);
            jda.addEventListener(waiter, client, gamesManager, listener, player);
            if (mode != 2) jda.addEventListener(shutdownListener);

            if (!takeoverManager.isTakeoverActive()) {
                for (Action action : Action.values()) {
                    List<String> responses = Responder.getResponder(action).getResponses();
                    KekBot.responses.put(action, responses);
                }
            }
        }

        //Loading all commands.
        client.addCommand(new Prefix());

        //Admin Commands
        client.addCommand(new Role());
        client.addCommand(new Purge());
        client.addCommand(new Kick());
        client.addCommand(new Ban());
        client.addCommand(new SettingsCommand(config.usingTwitter()));
        client.addCommand(new GetRole());

        //Fun Commands
        client.addCommand(new Avatar());
        client.addCommand(new CoinFlip());
        client.addCommand(new EightBall());
        client.addCommand(new FullWidth());
        client.addCommand(new Balance());
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

        //Weeb Commands (Only if weeb.sh token was supplied.)
        if (weebApi != null) {
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
            client.addCommand(new Stare(weebApi));
            client.addCommand(new ThumbsUp(weebApi));
            client.addCommand(new Wag(weebApi));
            client.addCommand(new Dab(weebApi));
            client.addCommand(new Deredere(weebApi));
            client.addCommand(new Tickle(weebApi));
            client.addCommand(new Bite(weebApi));
        }

        //General Commands
        client.addCommand(new Help());
        client.addCommand(new Stats());
        client.addCommand(new Poll(client));
        client.addCommand(new Strawpoll());
        client.addCommand(new GoogleSearch());
        client.addCommand(new LMGTFY());
        client.addCommand(new Changelog());
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
        if (weebApi != null) {
            client.addCommand(new Think(weebApi));
            client.addCommand(new Discord(weebApi));
        }
        client.addCommand(new Kirb());
        client.addCommand(new YouTried());
        client.addCommand(new DSXSays());
        client.addCommand(new Spoiler());
        client.addCommand(new WorldWideWeb());
        client.addCommand(new TrashWaifu());
        client.addCommand(new DoorKick());
        client.addCommand(new Garage());
        client.addCommand(new Brave());
        client.addCommand(new NotAllowed());
        client.addCommand(new Torture());
        client.addCommand(new Technology());
        client.addCommand(new Licky());
        client.addCommand(new Doggo());
        client.addCommand(new Urgent());

        //Social Commands
        client.addCommand(new TwitterCommand());


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
        client.addCommand(new Update());


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
     * @param reason The reason for shutting down.
     */
    public static void shutdown(String reason) {
        if (reason.equals("owo i smell an update, stay on your toes!")) {
            for (Guild guild : KekBot.jda.getGuilds()) {
                Settings settings = Settings.getSettings(guild);
                if (settings.getUpdateChannelID() == null) continue;
                if (guild.getTextChannelById(settings.getUpdateChannelID()) == null) {
                    TextChannel warningChannel = Utils.findAvailableTextChannel(guild);
                    if (warningChannel != null) warningChannel.sendMessage("I tried to alert this server for an incoming update, but the channel was deleted for some reason! Please fix this with the `settings` command once I'm done updating!").queue();
                    settings.setUpdateChannel(null);
                    settings.save();
                    continue;
                }
                guild.getTextChannelById(settings.getUpdateChannelID()).sendMessage(reason).queue();
            }
        }
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

    public static float nextFloat() {
        return random.nextFloat();
    }

    public static void update() {
        KekBot.shutdown("owo i smell an update, stay on your toes!");
        KekBot.shutdownListener.setExitCode(ExitCode.UPDATE);
    }

    /**
     * Verifies if the all the tables exist in our database.
     */
    private static void verifyTables() {
        if (!(boolean) r.tableList().contains("Profiles").run(conn)) {
            System.out.println("\"Profiles\" table was not found, so it is being made.");
            r.tableCreate("Profiles").optArg("primary_key", "User ID").run(conn);
        }
        if (!(boolean) r.tableList().contains("Responses").run(conn)) {
            System.out.println("\"Responses\" table was not found, so it is being made.");
            r.tableCreate("Responses").optArg("primary_key", "Action").run(conn);
        }
        if (!(boolean) r.tableList().contains("Settings").run(conn)) {
            System.out.println("\"Settings\" table was not found, so it is being made.");
            r.tableCreate("Settings").optArg("primary_key", "Guild ID").run(conn);
        }
        if (!(boolean) r.tableList().contains("Takeovers").run(conn)) {
            System.out.println("\"Takeovers\" table was not found, so it is being made.");
            r.tableCreate("Takeovers").optArg("primary_key", "Name").run(conn);
        }
        if (!(boolean) r.tableList().contains("Tickets").run(conn)) {
            System.out.println("\"Tickets\" table was not found, so it is being made.");
            r.tableCreate("Tickets").optArg("primary_key", "ID").run(conn);
        }
        if (!(boolean) r.tableList().contains("Twitter").run(conn)) {
            System.out.println("\"Twitter\" table was not found, so it is being made.");
            r.tableCreate("Twitter").optArg("primary_key", "Account ID").run(conn);
        }
    }
}
