package com.godson.kekbot.games;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class Hangman extends Game {
    private List<String> words = new ArrayList<>();
    private List<Character> guessedLetters = new ArrayList<>();
    private List<User> eliminatedPlayers = new ArrayList<>();
    private Map<User, Integer> playerPoints = new HashMap<>();
    private String word;
    private char[] letters;
    private char[] board;
    private int penalty = 0;
    private int turn;
    private Random random = new Random();

    private Font font = new Font("Calibri", Font.BOLD, 63);


    public Hangman(TextChannel channel) {
        super(2, 10, false, channel, "Hangman", false);
        isTranslatable = false;
        try {
            words.addAll(FileUtils.readLines(new File("resources/games/hangman/words.txt"), "utf-8"));
        } catch (IOException e) {
            //Nothing's gonna happen unless the file magically winds up deleted.
            e.printStackTrace();
        }
    }

    @Override
    public void startGame() {
        word = words.get(random.nextInt(words.size())).toUpperCase();
        letters = word.toUpperCase().toCharArray();
        board = new char[letters.length];
        if (word.contains(" ")) for (int i = 0; i < board.length; i++) if (letters[i] == ' ') board[i] = ' ';
        turn = random.nextInt(players.size()) + 1;
        channel.sendTyping().queue();
        channel.sendFile(drawBoard(), "hangman.png").content("**" + players.get(turn - 1).getName() + ", you're first!**").queue();
    }

    @Override
    public String getRules() {
        return "Try to guess the word! As soon as the game starts, players have to take turns trying to guess letters of the word or phrase by typing the " +
                "letter in chat, or, if they think they know the word, they can also try to guess the whole word by saying it in chat! However, they have to wait their turn " +
                "before trying to make a guess, otherwise they're ignored.\n" +
                "\nGuessing a letter gives you points based on how many times it appears in the word. (For example: \"Cat\", a only appears once, therefore you only get one point.)" +
                "\nGuessing the whole word gives you points based on how many letters haven't already been guessed. (For example: \"Cat\", except the letter A was already guessed, leaving the board at `_a_`, then you get two points.)" +
                "\n\nThe player(s) with the most points wins!" +
                "\nDepending on how many players are playing, there can also be second and third place winners!";
    }

    /**
     * Renders the game board.
     * @return The rendered board as a byte array, which can then be uploaded with JDA's sendFile method.
     */
    private byte[] drawBoard() {
        BufferedImage scene = new BufferedImage(800, 1000, BufferedImage.TYPE_INT_RGB);
        try {
            BufferedImage letterBoard = ImageIO.read(new File("resources/games/hangman/board.png"));
            Graphics2D graphics = scene.createGraphics();
            graphics.drawImage(ImageIO.read(new File("resources/games/hangman/stages/" + penalty + ".png")), 0, 0, null);
            graphics.drawImage(letterBoard, 0, 750, null);
            BufferedImage letterBoxes = drawBlanks();
            Rectangle2D r2D = new Rectangle(letterBoxes.getWidth(), letterBoxes.getHeight());
            int rWidth = (int) Math.round(r2D.getWidth());
            int rHeight = (int) Math.round(r2D.getHeight());
            int rX = (int) Math.round(r2D.getX());
            int rY = (int) Math.round(r2D.getY());
            int a = (letterBoard.getWidth() / 2) - (rWidth / 2) - rX;
            int b = (letterBoard.getHeight() / 2) - (rHeight / 2) - rY;
            graphics.drawImage(letterBoxes, a, 750 + b, null);
            graphics.dispose();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.setUseCache(false);
            ImageIO.write(scene, "png", stream);
            byte[] output = stream.toByteArray();
            stream.close();
            return output;
        } catch (IOException e) {
            e.printStackTrace(); // This will never get thrown, but okay.
        }
        throw new RuntimeException("Something's broke trying to render the hangman board!"); // This is probably a bad idea, but it's possible that this may also never be thrown as well.
    }

    /**
     * Renders the required number of boxes that represent the board array.
     * @return The rendered image.
     */
    private BufferedImage drawBlanks() {
        BufferedImage scene = new BufferedImage(800, 250, BufferedImage.TYPE_INT_ARGB);
        //Values for text position.
        int i = 0;
        int x = 0;
        int x2 = 0;
        int y = 0;
        //Values for text centering.
        Rectangle2D r2D;
        int rWidth;
        int rHeight;
        int rX;
        int rY;
        int a;
        int b;
        try {
            BufferedImage letterBox = ImageIO.read(new File("resources/games/hangman/letterbox.png")); //Blank letter file.
            Graphics2D graphics = scene.createGraphics();
            graphics.setBackground(new Color(0, true));
            graphics.clearRect(0, 0, scene.getWidth(), scene.getHeight());
            graphics.setColor(Color.BLACK);
            graphics.setFont(font);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //Antialiasing.
            while (i < letters.length) {
                if (letters[i] != ' ') {
                    graphics.drawImage(letterBox, 55 * x, 55 * y, null);
                    if (board[i] != 0) {
                        String letter = String.valueOf(letters[i]);
                        r2D = font.getStringBounds(letter, graphics.getFontRenderContext());
                        rWidth = (int) Math.round(r2D.getWidth());
                        rHeight = (int) Math.round(r2D.getHeight());
                        rX = (int) Math.round(r2D.getX());
                        rY = (int) Math.round(r2D.getY());
                        a = (letterBox.getWidth() / 2) - (rWidth / 2) - rX;
                        b = (letterBox.getHeight() / 2) - (rHeight / 2) - rY;
                        graphics.drawString(letter, a + (55 * x), b + 2 + (55 * y) + (SystemUtils.IS_OS_LINUX ? 4 : 0));
                    }
                    i++;
                    x++;
                } else {
                    String tempWord = this.word.substring(i + 1, (this.word.indexOf(' ', i + 1) != -1 ? this.word.indexOf(' ', i + 1) : this.word.length()));
                    int nextWordLength = tempWord.length();
                    int temp = (55 * x) + (55 * nextWordLength);
                    if (temp > 800) {
                        y++;
                        x2 = x;
                        x = 0;
                    } else x++;
                    i++;
                }
            }
            graphics.dispose();
        } catch (IOException e) {
            e.printStackTrace(); // This is never going to get thrown, but okay.
        }
        return scene.getSubimage(0, 0, (x2 == 0 ? (x) * 55 : (x2) * 55), (y + 1) * 55);
    }

    private boolean guessLetter(char letter, int player) {
        boolean guess = false;
        int points = 0;
        for (int i = 0; i < board.length; i++) {
            if (letters[i] == letter) {
                board[i] = letter;
                guess = true;
                points++;
            }
        }
        guessedLetters.add(letter);
        addPoints(player, points);
        return guess;
    }

    private void addPoints(int player, int points) {
        User user = players.get(player-1);
        if (!playerPoints.containsKey(user)) {
            playerPoints.put(user, points);
        } else playerPoints.replace(user, playerPoints.get(user) + points);
    }

    private void addPenalty() {
        penalty++;
    }

    private boolean isBoardFull() {
        for (char letter : board) {
            if (letter == 0) {
                //If this letter is empty, then the board isn't full.
                return false;
            }
        }
        //If all the letters of the board aren't empty, then the board is obviously full.
        return true;
    }

    private void eliminatePlayer(User user) {
        eliminatedPlayers.add(user);
    }

    private boolean isLetterUsed(char letter) {
        return guessedLetters.contains(letter);
    }

    private boolean guessWord(String word, int player) {
        int points = 0;
        if (word.equalsIgnoreCase(this.word)) {
            for (char letter : board) {
                if (letter == 0) points++;
            }
            board = letters;
            addPoints(player, points);
            return true;
        } else return false;
    }

    private void endTurn() {
        if (turn < players.size()) turn++;
        else turn = 1;
        if (eliminatedPlayers.contains(players.get(turn-1))) endTurn();
    }

    private boolean isWordPhrase() {
        return word.contains(" ");
    }

    @Override
    public void acceptInputFromMessage(Message message) {
        String result = null;
        if (eliminatedPlayers.contains(message.getAuthor()) || turn != getPlayerNumber(message.getAuthor())) {
            return;
        }

        if (message.getContentDisplay().length() == 1) {
            char letter = message.getContentDisplay().toUpperCase().charAt(0);
            if (letter <= 'Z' && letter >= 'A') {
                if (isLetterUsed(letter)) {
                    channel.sendMessage("That letter's already been used.").queue();
                    return;
                }
                if (!guessLetter(letter, getPlayerNumber(message.getAuthor()))) {
                    addPenalty();
                    result = "There are no `" + letter + "`s in this word/phrase.";
                } else result = "Looks like there are " + StringUtils.countMatches(word, letter) + "`" + letter + "`s in this word/phrase.";
            } else {
                channel.sendMessage("That isn't a letter.").queue();
                return;
            }
        } else {
            if (!Pattern.compile("[A-Z\\s]+", Pattern.CASE_INSENSITIVE).matcher(message.getContentRaw()).matches()) {
                channel.sendMessage("That's not a valid word/phrase.").queue();
                return;
            }
            if (message.getContentRaw().length() != word.length()) {
                channel.sendMessage("That guess is " + (message.getContentRaw().length() < word.length() ? "shorter" : "longer") + " than the actual word/phrase, and cannot be a valid guess.").queue();
                return;
            }
            if (!guessWord(message.getContentRaw(), getPlayerNumber(message.getAuthor()))) {
                eliminatePlayer(message.getAuthor());
                channel.sendMessage("**" + message.getAuthor().getAsMention() + " has guessed the " + (isWordPhrase() ? "phrase" : "word") + " incorrectly, and has been eliminated!**").queue();
            } else {
                endGame("You guessed the word, " + message.getAuthor().getAsMention() + "!");
                return;
            }
        }
        if (penalty == 8) {
            endGame("You ran out of guesses! The word was " + word);
            return;
        }
        if (eliminatedPlayers.size() == players.size()) {
            endGame("Everyone was eliminated! The word was " + word);
            return;
        }
        if (isBoardFull()) {
            endGame("You all figured out the word!");
            return;
        }
        endTurn();
        channel.sendTyping().queue();
        channel.sendFile(drawBoard(), "hangman.png").content(result).queue();
        channel.sendMessage(players.get(turn-1).getAsMention() + ", it's your turn!").queue();
    }

    private void endGame(String reason) {
        if (board != letters) board = letters;
        channel.sendFile(drawBoard(), "hangman.png").content(reason).queue();
        List<User> winners = new ArrayList<>(playerPoints.keySet());
        winners.sort(Comparator.comparingInt(playerPoints::get).reversed());
        winners = winners.subList(0, (winners.size() > 3 ? 2 : winners.size() - 1));
        endGame(winners, random.nextInt(5), random.nextInt(4));
    }

}
