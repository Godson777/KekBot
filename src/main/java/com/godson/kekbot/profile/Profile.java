package com.godson.kekbot.profile;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.music.Playlist;
import com.godson.kekbot.Utils;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.rethinkdb.model.MapObject;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.SystemUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Profile {
    @SerializedName("User ID")
    private long userID;
    @SerializedName("Token")
    private Token token;
    @SerializedName("Tokens")
    private List<Token> tokens = new ArrayList<>();
    @SerializedName("Backgrounds")
    private List<String> backgrounds = new ArrayList<String>();
    @SerializedName("Current Background ID")
    private String currentBackgroundID;
    @SerializedName("Badge")
    private Badge badge;
    @SerializedName("Topkeks")
    private double topkeks;
    private int KXP = 0;
    @SerializedName("Max KXP")
    private int maxKXP = 250;
    @SerializedName("Level")
    private int level = 1;
    //private int wins;
    //private int losses;
    @SerializedName("Subtitle")
    private String subtitle = "Just another user.";
    @SerializedName("Bio")
    private String bio;
    @SerializedName("Playlists")
    private List<Playlist> playlists = new ArrayList<>();
    @SerializedName("Next Daily")
    private long daily;

    private volatile User user;

    /**
     * Default constructor.
     * This is only ever used if a user doesn't already have a profile saved in a file.
     * @param userID The {@link User user's} ID.
     */
    private Profile(long userID) {
        this.userID = userID;
    }

    /**
     * Test constructor.
     */
    public Profile() {}

    /**
     * Equips a token to the user's profile.
     * This equipped token is used for minigames, though how they're used varies from minigame to minigame.
     * @param token The {@link Token token} the user wants to equip.
     */
    public void equipToken(Token token) {
        this.token = token;
    }

    /**
     * Unequips the {@link Token token} from the user's profile.
     */
    public void unequipToken() {
        token = null;
    }

    /**
     * Checks if the user has a {@link Token token} equipped.
     * @return The boolean value to represent this check.
     */
    public boolean hasTokenEquipped() {
        return token != null;
    }

    /**
     * Checks if the user has a {@link Background background} equipped.
     * @return The boolean value to represent this check.
     */
    public boolean hasBackgroundEquipped() {
        return currentBackgroundID != null;
    }

    /**
     * Checks if the user has a profile saved in a file.
     * @param user The user we're checking for.
     * @return The boolean value to represent this check.
     */
    public static boolean checkForProfile(User user) {
        return new File("profiles/" + user.getId() + ".json").exists();
    }

    /**
     * Checks if the user has a profile saved in a file.
     * @param userID The user's ID we're checking for.
     * @return The boolean value to represent this check.
     */
    public static boolean checkForProfile(long userID) {
        return new File("profiles/" + userID + ".json").exists();
    }

    /*
    For the time being, this method is not needed.
    But it will be reused.
    public void wonGame() {
        ++wins;
    }
    */

    /**
     * Gives the user rewards for winning a minigame.
     * @param topkeks The topkeks the user has earned.
     * @param KXP The KXP the user has earned.
     */
    public void wonGame(double topkeks, int KXP) {
        //++wins;
        this.topkeks += topkeks;
        addKXP(KXP);
    }

    /**
     * Same as {@link Profile#wonGame(double, int) wonGame}, however this doesn't increment the win counter.
     * @param topkeks The topkeks the user has earned.
     * @param KXP The KXP the user has earned.
     */
    public void tieGame(double topkeks, int KXP) {
        this.topkeks += topkeks;
        addKXP(KXP);
    }

    /*
    For the time being, this method is not needed.
    But it will be reused.
    public void lostGame() {
        ++losses;
    }
    */

    /**
     * Draws the user's card.
     * @return The byte array of the card. This is mostly meant to be used with {@link TextChannel#sendFile(File, String, Message) TextChannel#sendFile}.
     * @throws IOException If for some reason, the files are missing, this exception is thrown.
     */
    public byte[] drawCard() throws IOException {
        BufferedImage cardTemplate = ImageIO.read(new File("resources/profile/template.png"));
        BufferedImage background = (currentBackgroundID == null ? drawDefaultBackground() : KekBot.backgroundManager.get(currentBackgroundID).drawBackground());
        BufferedImage base = new BufferedImage(cardTemplate.getWidth(), cardTemplate.getHeight(), cardTemplate.getType());
        BufferedImage topkek = ImageIO.read(new File("resources/profile/topkek.png"));
        BufferedImage kxpBar = drawKXP();
        BufferedImage ava = Utils.getUserAvatarImage(user);
        Graphics2D card = base.createGraphics();
        //Draw background
        card.drawImage(background, 0, 0, background.getWidth(), background.getHeight(), null);
        //Draw user avatar
        card.drawImage(ava, 20, 18, 197, 197, null);
        //Draw card template
        card.drawImage(cardTemplate, 0, 0, cardTemplate.getWidth(), cardTemplate.getHeight(), null);
        //Draw Text
        card.setFont(ProfileUtils.topBarTitle);
        card.setColor(Color.BLACK);
        card.drawString(user.getName(), 249, 45);
        card.setFont(ProfileUtils.topBarSubtitle);
        card.drawString(subtitle, 249, 75);
        card.drawString("Bio:", 249, 145);
        if (bio != null) {
            card.setFont(ProfileUtils.topBarBio);
            String tempBio = bio;
            while (card.getFont().getStringBounds(tempBio, card.getFontRenderContext()).getWidth() > 736) {
                tempBio = tempBio.substring(0, tempBio.lastIndexOf(" ", tempBio.length()));
            }
            card.drawString(Utils.removeWhitespaceEdges(tempBio), 249, 175);
            if (bio.length() > tempBio.length()) card.drawString(Utils.removeWhitespaceEdges(bio.substring(tempBio.length(), bio.length())), 249, 175 + card.getFontMetrics().getHeight());
        }
        //Draw Levels, XP, topkeks, playlists and their badge (if they have one).
        card.setFont(ProfileUtils.sideBar);
        card.drawString("LVL " + level, 87, 252);
        card.drawImage(kxpBar, 46, 262, kxpBar.getWidth(), kxpBar.getHeight(), null);
        card.drawImage(topkek, 60, 314, 51, 51, null);
        card.drawString(shortenNumber(topkeks), 116, 351);

        //Draw other important texts.
        card.drawString("Tokens: ", 251, 264);
        card.drawString("Playlists: ", 251, 466);
        //Draw playlists.
        card.setFont(ProfileUtils.topBarBio);
        List<Playlist> playlists = getPlaylists().stream().filter(playlist -> !playlist.isHidden()).collect(Collectors.toList());
        for (int i = 0; i < (playlists.size() > 5 ? 5 : playlists.size()); i++) {
            Playlist playlist = playlists.get(i);
            card.drawString(playlist.getName() + " (" + Utils.convertMillisToHMmSs(playlist.getTotalLength()) + ")", 251, 490 + (20 * i));
        }
        //Draw user's badge (if they have one).
        if (badge != null) card.drawImage(badge.drawBadge(), 43, 435, 145, 145, null);

        //Draw Tokens
        for (int i = 0; i < (tokens.size() < 6 ? tokens.size() : 6); i++) {
            card.drawImage(getTokens().get(i).drawToken(), (265 + (120 * (i))), 295, 100, 100, null);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.flush();
        ImageIO.setUseCache(false);
        ImageIO.write(base, "png", outputStream);
        byte[] image = outputStream.toByteArray();
        outputStream.close();
        return image;
    }

    /**
     * Draws a copy of a user's profile card, but with a different background applied.
     * @param background The background to apply to this card.
     * @return The byte array of the card. This is mostly meant to be used with {@link TextChannel#sendFile(File, String, Message) TextChannel#sendFile}.
     * @throws IOException If for some reason, the files are missing, this exception is thrown.
     */
    public byte[] previewBackground(Background background) throws IOException {
        currentBackgroundID = background.getID();
        return drawCard();
    }

    /**
     * Draws a bland, gray background that's used for a users' default background.
     */
    private BufferedImage drawDefaultBackground() {
        BufferedImage background = new BufferedImage(1000, 600, BufferedImage.BITMASK);
        Graphics2D graphics = background.createGraphics();
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(0, 0, 1000, 600);
        return background;
    }

    /**
     * Draws the user's XP bar.
     * @return The drawn XP bar.
     * @throws IOException If for some reason, the files are missing, this exception is thrown.
     */
    private BufferedImage drawKXP() throws IOException {
        BufferedImage outline = ImageIO.read(new File("resources/profile/bar.png"));
        BufferedImage base = new BufferedImage(outline.getWidth(), outline.getHeight(), outline.getType());
        Graphics2D kxpBar = base.createGraphics();
        kxpBar.setColor(Color.GREEN);
        kxpBar.fillRect(4, 3, (int) (142 * ((double) KXP / (double) maxKXP)), 33);
        kxpBar.drawImage(outline, 0, 0, null);
        kxpBar.setFont(ProfileUtils.sideBar);
        //Prepare KXP/maxKXP text
        String kxp = shortenNumber(KXP) + "/" + shortenNumber(maxKXP);
        //Prepare to center text
        Font font = ProfileUtils.sideBar;
        Rectangle2D r2D = font.getStringBounds(kxp, ProfileUtils.frc);
        int rWidth = (int) Math.round(r2D.getWidth());
        int rHeight = (int) Math.round(r2D.getHeight());
        int rX = (int) Math.round(r2D.getX());
        int rY = (int) Math.round(r2D.getY());
        int a = (outline.getWidth() / 2) - (rWidth / 2) - rX;
        int b = (outline.getHeight() / 2) - (rHeight / 2) - rY;
        //And finally, draw the text
        kxpBar.setColor(Color.black);
        kxpBar.drawString(kxp, a, b + (SystemUtils.IS_OS_LINUX ? 4 : 0));
        kxpBar.dispose();
        return base;
    }

    /**
     * Gets the user's KXP value.
     * @return The user's KXP.
     */
    public int getKXP() {
        return KXP;
    }

    /**
     * Gets the user's max KXP value.
     * @return The user's max KXP.
     */
    public int getMaxKXP() {
        return maxKXP;
    }

    /**
     * (May be moved to the Utils class later.)
     * Shortens any number received, rounds it up to the nearest number, and shortens it to a more compact size. (Example, 1,000,000 becomes "1M")
     * @param value The number to be shortened.
     * @return The shortened number.
     */
    private String shortenNumber(double value) {
        if (value < 1000) {
            if (value % 1 == 0) return String.valueOf((int) value);
            else return String.valueOf(value);
        } else if (value >= 1000 && value < 100000) {
            if (value % 1000 == 0) return value / 1000 + "K";
            else if (value % 1000 > 950 && value % 1000 <= 999) return Math.round( value / 1000) + "K";
            else {
                //if (((double) value / 1000) > 99.9)
                    //return (Math.round((double) value / 1000)) + "K";
                return new BigDecimal( value / 1000).setScale(1, BigDecimal.ROUND_HALF_UP) + "K";
            }
        } else if (value >= 100000 && value < 1000000) {
            return (value / 1000) + "K";
        } else {
            if (value % 1000000 == 0) return value / 1000000 + "M";
            else if (value % 1000000 > 950000 && value % 1000000 <= 999999) return Math.round( value / 1000000) + "M";
            else {
                BigDecimal decimal = new BigDecimal(value / 1000000).setScale(1, BigDecimal.ROUND_HALF_EVEN);
                return decimal + "M";
            }
        }
    }

    /**
     * Adds a playlist object to a user's list of playlists.
     * @param playlist The playlist object.
     */
    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
    }

    /**
     * Gets the user's list of playlists.
     * @return The list of playlists.
     */
    public List<Playlist> getPlaylists() {
        return playlists;
    }

    /**
     * Adds KXP to the user's total, and levels them up if they've reached their maximum.
     * @param KXP The amount of KXP to add.
     */
    public void addKXP(int KXP) {
        this.KXP += KXP;
        if (this.KXP >= maxKXP) {
            this.KXP -= maxKXP;
        }
    }

    /**
     * Subtracts KXP from the user's total, and levels them down if needed.
     * @param KXP The amount of KXP to subtract.
     */
    public void takeKXP(int KXP) {
        this.KXP -= KXP;
        if (this.KXP < 0) {
            if (levelDown()) this.KXP += maxKXP;
            else this.KXP = 0;
        }
    }

    /**
     * Overrides the user's current KXP value, and sets it to whatever value given.
     * @param KXP The KXP value to set.
     */
    public void setKXP(int KXP) {
        this.KXP = KXP;
    }

    /**
     * Levels up the user, alerts them via DM, and adjusts appropriate values based on their new state.
     */
    private void levelUp() {
        maxKXP = (int) Math.round(maxKXP * 1.10);
        level++;
        user.openPrivateChannel().queue(ch -> ch.sendMessage("Congrats, you've successfully levelled up to level " + level + "!").queue());
    }

    /**
     * Lower the user's level.
     * @return A boolean value stating whether the job was complete or not.
     */
    private boolean levelDown() {
        if (level > 1) {
            maxKXP = (int) Math.round(maxKXP / 1.10);
            level--;
            return true;
        } else return false;
    }

    /**
     * Adds topkeks to the user's total.
     * @param topkeks The amount to add.
     */
    public void addTopKeks(double topkeks) {
        this.topkeks += topkeks;
    }

    /**
     * Checks if the user can spend a specified amount of topkeks.
     * @param topkeks Amount of topkeks to check for.
     * @return A boolean value stating whether they can spend the amount of topkeks or not.
     */
    public boolean canSpend(double topkeks) {
        return this.topkeks >= topkeks;
    }

    /**
     * Subtracts topkeks from the user's total. This method is used for both "spending" and forcefully subtracting topkeks from the user.
     * @param topkeks The amount to subtract.
     */
    public void spendTopKeks(double topkeks) {
        if (canSpend(topkeks)) this.topkeks -= topkeks;
        else this.topkeks = 0;
    }

    /**
     * Overrides the user's current topkek total, and sets it to whatever value given.
     * @param topkeks The amount to set.
     */
    public void setTopKeks(double topkeks) {
        this.topkeks = topkeks;
    }

    /**
     * Pays another user a specified amount of topkeks. (Unused)
     * @param topkeks The amount to give.
     * @param user The user to pay.
     */
    public void payUser(double topkeks, User user) {
        spendTopKeks(topkeks);
        save();
        Profile payee = Profile.getProfile(user);
        payee.addTopKeks(topkeks);
        payee.save();
        user.openPrivateChannel().queue(c -> c.sendMessage("Woohoo! You just got paid " + CustomEmote.printPrice(topkeks) + " by `" + this.user.getName() + "`.").queue());
    }

    /**
     * Removes a playlist from their list of playlists.
     * @param playlist The playlst to remove.
     */
    public void removePlaylist(Playlist playlist) {
        if (playlists.contains(playlist)) {
            playlists.remove(playlist);
        } else throw new NullPointerException("The playlist doesn't exist! How did you screw THIS up?");
    }

    /**
     * Gets the user's "subtitle".
     * @return The user's "subtitle".
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * Gets the user's "bio".
     * @return The user's "bio".
     */
    public String getBio() {
        return bio;
    }

    /**
     * Gets the user's currently equipped token.
     * @return The user's token.
     */
    public Token getToken() {
        return token;
    }

    /**
     * Gets a list of all the user's owned tokens.
     * @return The user's tokens.
     */
    public List<Token> getTokens() {
        return tokens;
    }

    /**
     * Sets the user's badge value to whatever specified.
     * @param badge The badge to set.
     */
    public void setBadge(Badge badge) {
        this.badge = badge;
    }

    /**
     * Gets the user's currently set badge.
     * @return The user's badge.
     */
    public Badge getBadge() {
        return badge;
    }

    /**
     * Sets the user's current background.
     * @param background The background to set.
     */
    public void setCurrentBackground(Background background) {
        if (background == null) currentBackgroundID = null;
        else currentBackgroundID = background.getID();
    }

    /**
     * Gets the user's current background.
     * @return THe user's background.
     */
    public Background getCurrentBackground() {
        return KekBot.backgroundManager.get(currentBackgroundID);
    }

    /**
     * Gets a list of all the user's owned backgrounds.
     * @return The user's backgrounds.
     */
    public List<String> getBackgrounds() {
        return backgrounds;
    }

    /**
     * Sets the user's "bio".
     * @param bio The "bio" value.
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Sets the user's "subtitle".
     * @param subtitle The "subtitle" value.
     */
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * Adds a token to the user's list of tokens.
     * @param token The token to add.
     */
    public void addToken(Token token) {
        tokens.add(token);
    }

    /**
     * Adds a background to the user's list of backgrounds.
     * @param background The background to add.
     */
    public void addBackground(Background background) {
        backgrounds.add(background.getID());
    }

    /**
     * Removes the user's background via it's ID.
     * @param background The background's ID.
     */
    public void removeBackgroundByID(String background) {
        backgrounds.remove(background);
    }

    /**
     * Removes a token from the user's list.
     * @param token The token to remove.
     */
    public void removeToken(Token token) {
        tokens.remove(token);
    }

    /**
     * Checks if the user has a specified token.
     * @param token The token to check for.
     * @return A boolean value stating whether they have the token or not.
     */
    public boolean hasToken(Token token) {
        return tokens.contains(token);
    }

    /**
     * Checks if the user has a specified background.
     * @param background The background to check for.
     * @return A boolean value stating whether they have the background or not.
     */
    public boolean hasBackground(Background background) {
        return backgrounds.contains(background.getID());
    }

    /**
     * Gets the user's current total of topkeks.
     * @return The user's total of topkeks.
     */
    public double getTopkeks() {
        return topkeks;
    }

    /**
     * Gets the user's current level.
     * @return The user's level.
     */
    public int getLevel() {
        return level;
    }

    public Instant getDaily() {
        return Instant.ofEpochSecond(daily);
    }

    public void setDaily(OffsetDateTime time) {
        daily = time.toInstant().getEpochSecond();
    }

    /**
     * A static method used to grab a profile object based on a user object.
     * @param user The user.
     * @return The user's profile.
     */
    public static Profile getProfile(User user) {
        Gson gson = new Gson();
        Profile profile;
        if (KekBot.r.table("Profiles").get(user.getIdLong()).run(KekBot.conn) != null) profile = gson.fromJson((String) KekBot.r.table("Profiles").get(user.getIdLong()).toJson().run(KekBot.conn), Profile.class);
        else profile = new Profile(user.getIdLong());
        profile.user = user;
        return profile;
    }

    /**
     * Saves the user's profile, overriding the old entry in the database with a newer version.
     */
    public void save() {
        MapObject object = KekBot.r.hashMap("User ID", userID)
                .with("Token", token != null ? token.toString() : null)
                .with("Tokens", tokens.stream().map(Enum::toString).collect(Collectors.toList()))
                .with("Backgrounds", backgrounds)
                .with("Current Background ID", currentBackgroundID)
                .with("Badge", badge != null ? badge.toString() : null)
                .with("Topkeks", topkeks)
                .with("KXP", KXP)
                .with("Max KXP", maxKXP)
                .with("Level", level)
                .with("Subtitle", subtitle)
                .with("Bio", bio)
                .with("Playlists", playlists)
                .with("Next Daily", daily);
        if (KekBot.r.table("Profiles").get(userID).run(KekBot.conn) == null) {
            KekBot.r.table("Profiles").insert(object).run(KekBot.conn);
        } else {
            KekBot.r.table("Profiles").get(userID).update(object).run(KekBot.conn);
        }
    }
}
