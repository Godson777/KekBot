package com.godson.kekbot;

import com.godson.kekbot.profile.BackgroundManager;
import com.godson.kekbot.settings.Config;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.Requester;
import okhttp3.*;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Utils {

    private final static String[] EMOJI = new String[Character.MAX_VALUE+1];
    //Prepares the character replacer.
    static {
        for(int i=Character.MIN_VALUE;i<=Character.MAX_VALUE;i++)
            EMOJI[i] = Character.toString(Character.toLowerCase((char) i));
        EMOJI['a'] =  "\uD83C\uDDE6 ";
        EMOJI['b'] =  "\uD83C\uDDE7 ";
        EMOJI['c'] = "\uD83C\uDDE8 ";
        EMOJI['d'] = "\uD83C\uDDE9 ";
        EMOJI['e'] = "\uD83C\uDDEA ";
        EMOJI['f'] = "\uD83C\uDDEB ";
        EMOJI['g'] = "\uD83C\uDDEC ";
        EMOJI['h'] = "\uD83C\uDDED ";
        EMOJI['i'] = "\uD83C\uDDEE ";
        EMOJI['j'] = "\uD83C\uDDEF ";
        EMOJI['k'] = "\uD83C\uDDF0 ";
        EMOJI['l'] = "\uD83C\uDDF1 ";
        EMOJI['m'] = "\uD83C\uDDF2 ";
        EMOJI['n'] = "\uD83C\uDDF3 ";
        EMOJI['o'] = "\uD83C\uDDF4 ";
        EMOJI['p'] = "\uD83C\uDDF5 ";
        EMOJI['q'] = "\uD83C\uDDF6 ";
        EMOJI['r'] = "\uD83C\uDDF7 ";
        EMOJI['s'] = "\uD83C\uDDF8 ";
        EMOJI['t'] = "\uD83C\uDDF9 ";
        EMOJI['u'] = "\uD83C\uDDFA ";
        EMOJI['v'] = "\uD83C\uDDFB ";
        EMOJI['w'] = "\uD83C\uDDFC ";
        EMOJI['x'] = "\uD83C\uDDFD ";
        EMOJI['y'] = "\uD83C\uDDFE ";
        EMOJI['z'] = "\uD83C\uDDFF ";
        EMOJI['0'] = "0⃣ ";
        EMOJI['1'] = "1⃣ ";
        EMOJI['2'] = "2⃣ ";
        EMOJI['3'] = "3⃣ ";
        EMOJI['4'] = "4⃣ ";
        EMOJI['5'] = "5⃣ ";
        EMOJI['6'] = "6⃣ ";
        EMOJI['7'] = "7⃣ ";
        EMOJI['8'] = "8⃣ ";
        EMOJI['9'] = "9⃣ ";
        EMOJI['!'] = "❗ ";
        EMOJI['?'] = "❓ ";
    }

    /**
     * Deletes an entire directory. Enough said.
     * @param directory The directory we're going to delete.
     * @return Whether or not the directory has been deleted successfully.
     */
    public static boolean deleteDirectory(File directory) {
        if (directory.exists()){
            File[] files = directory.listFiles();
            if (null != files){
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return(directory.delete());
    }

    /**
     * Sends stats to DiscordBots, DiscordBotsList, DiscordListBots, and Carbonitex. (If tokens for those sites are provided in the config file.)
     * Featuring slightly borrowed code from JDA-Utilites (jag pls don't hate me)
     * @param jda The instance of JDA (or shard) to send stats from.
     */
    public static void sendStats(JDA jda) {
        OkHttpClient client = ((JDAImpl) jda).getHttpClientBuilder().build();
        Config config = Config.getConfig();
        String carbonToken = config.getCarbonToken();
        String botsListToken = config.getdBotsListToken();
        String botsToken = config.getdApiToken();
        String dListBotsToken = config.getdListBotsToken();

        if (carbonToken != null) {
            FormBody.Builder bodyBuilder = new FormBody.Builder().add("key", carbonToken).add("servercount", Integer.toString(jda.getGuilds().size()));

            if (jda.getShardInfo() != null)
                bodyBuilder.add("shard_id", Integer.toString(jda.getShardInfo().getShardId()))
                        .add("shard_count", Integer.toString(jda.getShardInfo().getShardTotal()));

            Request.Builder builder = new Request.Builder()
                    .post(bodyBuilder.build())
                    .url("https://www.carbonitex.net/discord/data/botdata.php");

            client.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    //Do nothing for now.
                }
            });
        }

        if (botsListToken != null) {
            JSONObject body = new JSONObject().put("server_count", jda.getGuilds().size());
            if (jda.getShardInfo() != null)
                body.put("shard_id", jda.getShardInfo().getShardId()).put("shard_count", jda.getShardInfo().getShardTotal());

            Request.Builder builder = new Request.Builder()
                    .post(RequestBody.create(Requester.MEDIA_TYPE_JSON, body.toString()))
                    .url("https://discordbots.org/api/bots/" + jda.getSelfUser().getId() + "/stats")
                    .header("Authorization", botsListToken)
                    .header("Content-Type", "application/json");

            client.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    //Do nothing for now.
                }
            });
        }

        if (botsToken != null) {
            JSONObject body = new JSONObject().put("server_count", jda.getGuilds().size());

            if (jda.getShardInfo() != null) body.put("shard_id", jda.getShardInfo().getShardId()).put("shard_count", jda.getShardInfo().getShardTotal());

            Request.Builder builder = new Request.Builder()
                    .post(RequestBody.create(Requester.MEDIA_TYPE_JSON, body.toString()))
                    .url("https://bots.discord.pw/api/bots/" + jda.getSelfUser().getId() + "/stats")
                    .header("Authorization", botsToken)
                    .header("Content-Type", "application/json");

            client.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                }
            });
        }

        if (dListBotsToken != null) {
            JSONObject body = new JSONObject().put("token", dListBotsToken).put("servers", KekBot.jda.getGuilds());

            Request.Builder builder = new Request.Builder().post(RequestBody.create(Requester.MEDIA_TYPE_JSON, body.toString()))
                    .url("https://bots.discordlist.net/api")
                    .header("Content-Tytpe", "application/json");

            client.newCall(builder.build()).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    //Do nothing for now.
                }
            });
        }
    }

    /**
     * Purely a method to use with eval.
     */
    public static void reloadBackgrounds() {
        KekBot.backgroundManager = new BackgroundManager();
    }

    /**
     * Attempts to get a BufferedImage of the user's avatar. If the user doesn't have one, a generic one is used instead.
     * @param user The User KekBot will try to steal the avatar from.
     * @return The user's avatar as a {@link BufferedImage BufferedImage object}. Will return a generic avatar if the user doesn't happen (which will only happen if a MalformedURLException occurs).
     */
    public static BufferedImage getUserAvatarImage(User user) {
        BufferedImage ava = null;
        try {
            URL userAva = new URL(user.getAvatarUrl() + "?size=1024");
            URLConnection connection = userAva.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();
            ava = ImageIO.read(connection.getInputStream());
        } catch (MalformedURLException e) {
           ava = KekBot.genericAvatar;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ava == null) ava = KekBot.genericAvatar;
        return ava;
    }

    /**
     * Gets the user's avatar URL. If they don't have one, their default is returned.
     * @param user The User KekBot will try to steal the avatar from.
     * @return The user's avatar URL.
     */
    public static String getUserAvatarURL(User user) {
        return user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl();
    }

    /**
     * Converts milliseconds to a H:Mm:Ss format. (Example: 1:02:30)
     * @param millis The milliseconds to convert.
     * @return The converted H:Mm:Ss format.
     */
    public static String convertMillisToHMmSs(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        return (hours > 0 ? hours + ":" : "") +
                (minutes > 0 ? (minutes > 9 ? minutes + ":" : (hours > 0 ? "0" + minutes + ":" : minutes + ":" )) : (hours > 0 ? "00:" : "0:")) +
                (seconds > 0 ? (seconds > 9 ? seconds : "0" + seconds) : "00");
    }

    /**
     * Converts milliseconds to a "Time" format. (Example, 1 Day, 20 Hours, 10 minutes, and 5 Seconds.
     * @param millis The milliseconds to convert.
     * @return The converted "Time" format.
     */
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

    /**
     * Converts two millis to a H:Mm:Ss format. Mostly to compare one to the other.
     * This is mostly meant to be used within the music player, since the first variable is the current timestamp, while the second variable is the length of the track.
     * @param current Current position in track.
     * @param length Total track length.
     * @return The converted H:Mm:Ss format.
     */
    //This may wind up being a private method in the music player in a later version.
    public static String songTimestamp(long current, long length) {
        return convertMillisToHMmSs(current) + "/" + convertMillisToHMmSs(length);
    }

    /**
     * Removes any whitespace from the edges of a string.
     * @param string The {@link String string} we're going to remove whitespaces from.
     * @return The {@link String string}, minus the unnecessary whitespaces at the edges.
     */
    public static String removeWhitespaceEdges(String string) {
        if (string.matches(".*\\w.*")) {
            if (string.startsWith(" ")) string = string.replaceFirst("([ ]+)", "");
            if (string.endsWith(" ")) string = string.replaceAll("([ ]+$)", "");
        } else string = "";
        return string;
    }

    /**
     * Combines a set of arguments to a single string.
     * @param arguments The arguments to combine.
     * @return The combined {@link String string} object.
     */
    public static String combineArguments(String[] arguments) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            builder.append(arguments[i]);
            if (i != arguments.length-1) builder.append(" ");
        }
        return builder.toString();
    }

    /**
     * Prints a human friendly readable number, with commas.
     * @param number Number to make human friendly.
     * @return The human friendly version of the number inputted.
     */
    public static String printReadableNumber(int number) {
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }

    /**
     * Converts a boring piece of text into exciting emojis!
     * @param word Boring piece of text.
     * @return The exciting version of the text inputted.
     */
    public static String emojify(String word) {
        StringBuilder sb = new StringBuilder(word.length());
        for(int i = 0; i < word.length(); i++)
            sb.append(EMOJI[word.toLowerCase().charAt(i)]);
        return sb.toString();
    }
}
