package com.godson.kekbot.Profile;

import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Music.Playlist;
import com.godson.kekbot.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import javax.imageio.ImageIO;
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
    private int topkeks;
    private int KXP = 0;
    private int maxKXP = 250;
    private int level = 1;
    //private int wins;
    //private int losses;
    private String subtitle = "Just another user.";
    private String bio;
    private transient long userID;
    private List<Playlist> playlists = new ArrayList<>();

    private Profile(long userID) {
        this.userID = userID;
    }

    public void equipToken(Token token) {
        this.token = token;
    }

    public void unequipToken() {
        token = null;
    }

    public boolean hasTokenEquipped() {
        return token != null;
    }

    public boolean hasBackgroundEquipped() {
        return currentBackgroundID != null;
    }

    public static boolean checkForProfile(User user) {
        return new File("profiles/" + user.getId() + ".json").exists();
    }

    /*
    For the time being, this method is not needed.
    But it will be reused.
    public void wonGame() {
        ++wins;
    }
    */



    public void wonGame(TextChannel channel, int topkeks, int KXP) {
        //++wins;
        if (!(topkeks == 0 && KXP == 0)) stateEarnings(channel, topkeks, KXP);
        this.topkeks += topkeks;
        addKXP(channel.getJDA(), KXP);
    }

    public void tieGame(TextChannel channel, int topkeks, int KXP) {
        if (!(topkeks == 0 && KXP == 0)) stateEarnings(channel, topkeks, KXP);
        this.topkeks += topkeks;
        addKXP(channel.getJDA(), KXP);
    }

    /*
    For the time being, this method is not needed.
    But it will be reused.
    public void lostGame() {
        ++losses;
    }
    */

    private void stateEarnings(TextChannel channel, int topkeks, int KXP) {
        channel.sendMessage(channel.getGuild().getMemberById(String.valueOf(userID)).getAsMention() + ", you've earned " +
                (topkeks > 0 ? topkeks + CustomEmote.TOPKEK : "") +
                (topkeks > 0 && KXP > 0 ? ", and " : "") +
                (KXP > 0 ? KXP + " KXP" : "") +
                (KXP <= 0 && topkeks <= 0 ? "nothing" : "") + "!").queue();
    }

    public byte[] drawCard(JDA jda) throws IOException {
        BufferedImage cardTemplate = ImageIO.read(new File("resources/profile/template.png"));
        BufferedImage background = (currentBackgroundID == null ? drawDefaultBackground() : KekBot.backgroundManager.get(currentBackgroundID).drawBackground());
        BufferedImage base = new BufferedImage(cardTemplate.getWidth(), cardTemplate.getHeight(), cardTemplate.getType());
        BufferedImage topkek = ImageIO.read(new File("resources/profile/topkek.png"));
        BufferedImage kxpBar = drawKXP();
        BufferedImage ava = Utils.getAvatar(jda.getUserById(String.valueOf(userID)));
        Graphics2D card = base.createGraphics();
        //Draw background (implement later)
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
            card.drawString(KekBot.removeWhitespaceEdges(tempBio), 249, 175);
            if (bio.length() > tempBio.length()) card.drawString(KekBot.removeWhitespaceEdges(bio.substring(tempBio.length(), bio.length())), 249, 175 + card.getFontMetrics().getHeight());
        }
        //Draw Levels, XP, topkeks, playlists and their badge (if they have one).
        card.setFont(ProfileUtils.sideBar);
        card.drawString("LVL " + level, 87, 252);
        card.drawImage(kxpBar, 46, 262, kxpBar.getWidth(), kxpBar.getHeight(), null);
        card.drawImage(topkek, 60, 314, 51, 51, null);
        card.drawString(shortenNumber(topkeks), 116, 351);

        //May likely remove these two lines below:
        //card.drawString("Wins: " + shortenNumber(wins), 66, 395);
        //card.drawString("Losses: " + shortenNumber(losses), 66, 428);

        card.drawString("Tokens: ", 251, 264);
        card.drawString("Playlists: ", 251, 466);
        card.setFont(ProfileUtils.topBarBio);
        List<Playlist> playlists = getPlaylists().stream().filter(playlist -> !playlist.isHidden()).collect(Collectors.toList());
        for (int i = 0; i < playlists.size(); i++) {
            Playlist playlist = playlists.get(i);
            card.drawString(playlist.getName() + " (" + KekBot.convertMillisToHMmSs(playlist.getTotalLength()) + ")", 251, 490 + (20 * i));
        }
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

    public byte[] previewBackground(JDA jda, Background background) throws IOException {
        currentBackgroundID = background.getID();
        return drawCard(jda);
    }

    private BufferedImage drawDefaultBackground() {
        BufferedImage background = new BufferedImage(1000, 600, BufferedImage.BITMASK);
        Graphics2D graphics = background.createGraphics();
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(0, 0, 1000, 600);
        return background;
    }

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

    private String shortenNumber(int value) {
        if (value < 1000) {
            return String.valueOf(value);
        } else if (value >= 1000 && value < 100000) {
            if (value % 1000 == 0) return value / 1000 + "K";
            else if (value % 1000 > 950 && value % 1000 <= 999) return Math.round((double) value / 1000) + "K";
            else {
                //if (((double) value / 1000) > 99.9)
                    //return (Math.round((double) value / 1000)) + "K";
                return new BigDecimal((double) value / 1000).setScale(1, BigDecimal.ROUND_HALF_UP) + "K";
            }
        } else if (value >= 100000 && value < 1000000) {
            return (value / 1000) + "K";
        } else {
            if (value % 1000000 == 0) return value / 1000000 + "M";
            else if (value % 1000000 > 950000 && value % 1000000 <= 999999) return Math.round((double) value / 1000000) + "M";
            else {
                BigDecimal decimal = new BigDecimal((double) value / 1000000).setScale(1, BigDecimal.ROUND_HALF_EVEN);
                return decimal + "M";
            }
        }
    }

    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    public void addKXP(JDA jda, int KXP) {
        this.KXP += KXP;
        if (this.KXP >= maxKXP) {
            this.KXP -= maxKXP;
            levelUp(jda);
        }
    }

    public void takeKXP(int KXP) {
        this.KXP -= KXP;
        if (this.KXP < 0) {
            levelDown();
            this.KXP += maxKXP;
        }
    }

    public void setKXP(int KXP) {
        this.KXP = KXP;
    }

    public void overrideMaxKXP(int maxKXP) {
        this.maxKXP = maxKXP;
    }

    public void levelUp(JDA jda) {
        User user = jda.getUserById(String.valueOf(userID));
        maxKXP = (int) Math.round(maxKXP * 1.10);
        level++;
        user.openPrivateChannel().queue(ch -> ch.sendMessage("Congrats, you've successfully levelled up to level " + level + "!").queue());
    }

    public void levelDown() {
        maxKXP = (int) Math.round(maxKXP / 1.10);
        level--;
    }

    public void addTopKeks(int topkeks) {
        this.topkeks += topkeks;
    }

    public boolean canSpend(int topkeks) {
        return this.topkeks >= topkeks;
    }

    public void spendTopKeks(int topkeks) {
        this.topkeks -= topkeks;
    }

    public void setTopKeks(int topkeks) {
        this.topkeks = topkeks;
    }

    public void removePlaylist(Playlist playlist) {
        if (playlists.contains(playlist)) {
            playlists.remove(playlist);
        } else throw new NullPointerException("The playlist doesn't exist! How did you screw THIS up?");
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getBio() {
        return bio;
    }

    public Token getToken() {
        return token;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setBadge(Badge badge) {
        this.badge = badge;
    }

    public Badge getBadge() {
        return badge;
    }

    public void setCurrentBackground(Background background) {
        if (background == null) currentBackgroundID = null;
        else currentBackgroundID = background.getID();
    }

    public Background getCurrentBackground() {
        return KekBot.backgroundManager.get(currentBackgroundID);
    }

    public List<String> getBackgrounds() {
        return backgrounds;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void addToken(Token token) {
        tokens.add(token);
    }

    public void addBackground(Background background) {
        backgrounds.add(background.getID());
    }

    public void removeBackgroundByID(String background) {
        backgrounds.remove(background);
    }

    public void removeToken(Token token) {
        tokens.remove(token);
    }

    public boolean hasToken(Token token) {
        return tokens.contains(token);
    }

    public boolean hasBackground(Background background) {
        return backgrounds.contains(background.getID());
    }

    public int getTopkeks() {
        return topkeks;
    }

    public int getLevel() {
        return level;
    }

    public static Profile getProfile(User user) {
        Profile profile = new Profile(Long.valueOf(user.getId()));
        if (new File("profiles/" + user.getId() + ".json").exists()) {
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
