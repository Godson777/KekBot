package com.godson.kekbot.Profile;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Music.Playlist;
import com.godson.kekbot.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import javax.imageio.ImageIO;
import javax.rmi.CORBA.Util;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Profile {
    public Token token;
    private List<Token> tokens = new ArrayList<Token>();
    private List<String> backgrounds = new ArrayList<String>();
    private String currentBackgroundID;
    private Badge badge;
    private double topkeks;
    private int KXP = 0;
    private int maxKXP = 250;
    private int level = 1;
    //private int wins;
    //private int losses;
    private String subtitle = "Just another user.";
    private String bio;
    private transient long userID;
    private List<Playlist> playlists = new ArrayList<>();

    /**
     * Default constructor.
     * This is only ever used if a user doesn't already have a profile saved in a file.
     * @param userID The {@link User user's} ID.
     */
    private Profile(long userID) {
        this.userID = userID;
    }

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
    public void wonGame(JDA jda, double topkeks, int KXP) {
        //++wins;
        this.topkeks += topkeks;
        addKXP(jda, KXP);
    }

    /**
     * Same as {@link Profile#wonGame(JDA, double, int) wonGame}, however this doesn't increment the win counter.
     * @param topkeks The topkeks the user has earned.
     * @param KXP The KXP the user has earned.
     */
    public void tieGame(JDA jda, double topkeks, int KXP) {
        this.topkeks += topkeks;
        addKXP(jda, KXP);
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
     * @param jda The instance of JDA
     * @return The byte array of the card. This is mostly meant to be used with {@link TextChannel#sendFile(File, String, Message) TextChannel#sendFile}.
     * @throws IOException If for some reason, the files are missing, this exception is thrown.
     */
    public byte[] drawCard(JDA jda) throws IOException {
        BufferedImage cardTemplate = ImageIO.read(new File("resources/profile/template.png"));
        BufferedImage background = (currentBackgroundID == null ? drawDefaultBackground() : KekBot.backgroundManager.get(currentBackgroundID).drawBackground());
        BufferedImage base = new BufferedImage(cardTemplate.getWidth(), cardTemplate.getHeight(), cardTemplate.getType());
        BufferedImage topkek = ImageIO.read(new File("resources/profile/topkek.png"));
        BufferedImage kxpBar = drawKXP();
        BufferedImage ava = Utils.getUserAvatarImage(jda.getUserById(String.valueOf(userID)));
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
        card.drawString(jda.getUserById(String.valueOf(userID)).getName(), 249, 45);
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
            card.drawImage(tokens.get(i).drawToken(), (265 + (120 * (i))), 295, 100, 100, null);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.flush();
        ImageIO.write(base, "png", outputStream);
        byte[] image = outputStream.toByteArray();
        outputStream.close();
        return image;
    }

    /**
     * Draws a copy of a user's profile card, but with a different background applied.
     * @param jda The instance of JDA
     * @param background The background to apply to this card.
     * @return The byte array of the card. This is mostly meant to be used with {@link TextChannel#sendFile(File, String, Message) TextChannel#sendFile}.
     * @throws IOException If for some reason, the files are missing, this exception is thrown.
     */
    public byte[] previewBackground(JDA jda, Background background) throws IOException {
        currentBackgroundID = background.getID();
        return drawCard(jda);
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
        int a = (132 / 2) - (rWidth / 2) - rX;
        int b = (29 / 2) - (rHeight / 2) - rY;
        //And finally, draw the text
        kxpBar.setColor(Color.black);
        kxpBar.drawString(kxp,9 + a,7 + b);
        kxpBar.dispose();
        return base;
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
     * @param jda The instance of JDA that may be called to DM the user for when they're leveled up.
     * @param KXP The amount of KXP to add.
     */
    public void addKXP(JDA jda, int KXP) {
        this.KXP += KXP;
        if (this.KXP >= maxKXP) {
            this.KXP -= maxKXP;
            levelUp(jda);
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
     * @param jda The instance of JDA to use to DM the user.
     */
    private void levelUp(JDA jda) {
        User user = jda.getUserById(String.valueOf(userID));
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
        user.openPrivateChannel().queue(c -> c.sendMessage("Woohoo! You just got paid " + CustomEmote.printPrice(topkeks) + " by `" + Utils.findShardUser(Long.toString(userID)).getName() + "`.").queue());
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

    /**
     * A static method used to grab a profile object based on a user object.
     * @param user The user.
     * @return The user's profile.
     */
    public static Profile getProfile(User user) {
        Profile profile = new Profile(Long.valueOf(user.getId()));
        if (checkForProfile(user)) {
            try {
                BufferedReader br = new BufferedReader(new FileReader("profiles/" + user.getId() + ".json"));
                Gson gson = new Gson();
                profile = gson.fromJson(br, Profile.class);
                profile.userID = Long.valueOf(user.getId());
                br.close();
            } catch (FileNotFoundException e) {
                //do nothing
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return profile;
    }

    /**
     * Identical to {@link Profile#getProfile(User)}, however it uses a user ID, instead of a user object.
     * This may never be used, however there may be a time where this is more useful than using a user object.
     * @param userID The user's ID.
     * @return The user's profile.
     */
    public static Profile getProfile(long userID) {
        Profile profile = new Profile(userID);
        if (checkForProfile(userID)) {
            try {
                BufferedReader br = new BufferedReader(new FileReader("profiles/" + userID + ".json"));
                Gson gson = new Gson();
                profile = gson.fromJson(br, Profile.class);
                profile.userID = userID;
                br.close();
            } catch (FileNotFoundException e) {
                //do nothing
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return profile;
    }

    /**
     * Saves the user's profile, overriding the old file with a newer version.
     */
    public void save() {
        File folder = new File("profiles");
        File profile = new File("profiles/" + userID + ".json");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try {
            FileWriter writer = new FileWriter(profile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(this, Profile.class));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
