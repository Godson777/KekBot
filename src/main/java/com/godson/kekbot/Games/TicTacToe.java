package com.godson.kekbot.Games;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.Profile.Profile;
import com.godson.kekbot.Profile.Token;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TicTacToe extends Game {
    private int[] board = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    private int turn;
    private Random random = new Random();

    //AI STUFF
    private HashMap<Integer, int[]> secondarySlots = new HashMap<>();
    private HashMap<Integer, HashMap<Integer, Integer>> tertiarySlots = new HashMap<>();
    private int[] primarySlots = {
            0, /* skipping 1 */ 2,
            /* skipping 3*/ 4, /* skipping 5 */
            6, /* skipping 7 */ 8
    };

    //Board Drawing Stuff
    private BufferedImage[] tokens = new BufferedImage[3];
    private BufferedImage player1;
    private BufferedImage player2;

    public TicTacToe(TextChannel channel) {
        super(2, true, channel, "TicTacToe");
    }

    @Override
    public void startGame() {
        try {
            tokens[0] = ImageIO.read(new File("resources/games/tictactoe/blank.png"));
            if (Profile.checkForProfile(players.get(0))) {
                if (Profile.getProfile(players.get(0)).hasTokenEquipped()) {
                    tokens[1] = Profile.getProfile(players.get(0)).token.drawToken();
                }
            }
            if (players.size() == 2) {
                if (Profile.checkForProfile(players.get(1))) {
                    if (Profile.getProfile(players.get(1)).hasTokenEquipped()) {
                        tokens[2] = Profile.getProfile(players.get(1)).token.drawToken();
                    }
                }
            } else {
                tokens[2] = Token.GRAND_DAD.drawToken();
            }
            if (tokens[1] == null) tokens[1] = ImageIO.read(new File("resources/games/tictactoe/X.png"));
            if (tokens[2] == null) tokens[2] = ImageIO.read(new File("resources/games/tictactoe/O.png"));
            URL player1URL = new URL(players.get(0).getAvatarUrl());
            URLConnection connection = player1URL.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();
            player1 = ImageIO.read(connection.getInputStream());
            if (players.size() == 2) {
                URL player2URL = new URL(players.get(1).getAvatarUrl());
                URLConnection connection2 = player2URL.openConnection();
                connection2.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection2.connect();
                player2 = ImageIO.read(connection2.getInputStream());
            } else {
                URL player2URL = new URL(channel.getJDA().getSelfUser().getAvatarUrl());
                URLConnection connection2 = player2URL.openConnection();
                connection2.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection2.connect();
                player2 = ImageIO.read(connection2.getInputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        turn = random.nextInt(2)+1;
        if (players.size() < maxNumberOfPlayers) {
            prepareAI();
            if (turn == 2) {
                aiFillSlot();
                turn = 1;
                channel.sendMessage("**KekBot got the first move.**").queue();
            } else {
                drawBoard();
                channel.sendMessage("**" + players.get(0).getName() + ", you're first!**").queue();
            }
        } else {
            drawBoard();
            channel.sendMessage("**" + players.get(turn-1).getName() + ", you're first!**").queue();
        }
    }

    @Override
    public void acceptInputFromMessage(Message message) {
        String contents = message.getRawContent();
        try {
            int slot = Integer.valueOf(contents);
            fillSlot(slot-1, message.getAuthor());
        } catch (NumberFormatException e) {
            //do nothing.
        }
    }

    private void drawBoard() {
        try {
            channel.sendTyping().queue();
            BufferedImage base = ImageIO.read(new File("resources/games/tictactoe/tictactoe.png"));
            Graphics2D board = base.createGraphics();
            board.drawImage(player1, 22, 22, 158, 158, null);
            board.drawImage(player2, 420, 22, 158, 158, null);
            for (int i = 0; i < 8; i += 3) {
                for (int ii = 0; ii < 3; ii++) {
                   if (this.board[i+ii] != 0) {
                       board.drawImage(tokens[0], 205 * ii, 200 + (205 * (i / 3)), 190, 190, null);
                       board.drawImage(tokens[this.board[i+ii]], 205 * ii, 200 + (205 * (i / 3)), 190, 190, null);
                   }
                }
            }
            board.dispose();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.setUseCache(false);
            ImageIO.write(base, "png", stream);
            channel.sendFile(stream.toByteArray(), "tictactoe.png", null).queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fillSlot(int slot, User player) {
        if (turn == getPlayerNumber(player)) {
            if (slot < board.length && slot > -1) {
                if (board[slot] == 0) {
                    board[slot] = getPlayerNumber(player);
                    if (!check(player)) {
                        if (!checkForDraw()) {
                            if (players.size() == 1) {
                                aiFillSlot();
                            } else {
                                drawBoard();
                                if (turn == 1) turn = 2;
                                else turn = 1;
                            }
                        }
                    }
                } else {
                    if (board[slot] == getPlayerNumber(player)) channel.sendMessage("You already own that space.").queue();
                    else channel.sendMessage("Your opponent already owns that space.").queue();
                }
            }
        } else {
            channel.sendMessage("It's not your turn yet!").queue();
        }
    }

    //Credits to Tech_Hutch for making this.
    private void prepareAI() {
        int[][] secondary = {{1, 3, 4, 2, 6, 8}, {1, 5, 4, 0, 6, 8}, {1, 3, 5, 7}, {3, 7, 4, 0, 2, 8}, {7, 5, 4, 0, 2, 6}};

        secondarySlots.put(0, secondary[0]);
        secondarySlots.put(2, secondary[1]);
        secondarySlots.put(4, secondary[2]);
        secondarySlots.put(6, secondary[3]);
        secondarySlots.put(8, secondary[4]);

        for (int primarySlot : primarySlots) {
            tertiarySlots.put(primarySlot, new HashMap<>());
        }
        tertiarySlots.get(0).put(1, 2);
        tertiarySlots.get(0).put(3, 6);
        tertiarySlots.get(0).put(4, 8);
        tertiarySlots.get(0).put(2, 1);
        tertiarySlots.get(0).put(6, 3);
        tertiarySlots.get(0).put(8, 4);
        tertiarySlots.get(2).put(1, 0);
        tertiarySlots.get(2).put(4, 6);
        tertiarySlots.get(2).put(5, 8);
        tertiarySlots.get(2).put(0, 1);
        tertiarySlots.get(2).put(6, 4);
        tertiarySlots.get(2).put(8, 5);
        tertiarySlots.get(4).put(1, 7);
        tertiarySlots.get(4).put(7, 1);
        tertiarySlots.get(4).put(3, 5);
        tertiarySlots.get(4).put(5, 3);
        tertiarySlots.get(6).put(3, 0);
        tertiarySlots.get(6).put(4, 2);
        tertiarySlots.get(6).put(7, 8);
        tertiarySlots.get(6).put(0, 3);
        tertiarySlots.get(6).put(2, 4);
        tertiarySlots.get(6).put(8, 7);
        tertiarySlots.get(8).put(4, 0);
        tertiarySlots.get(8).put(5, 2);
        tertiarySlots.get(8).put(7, 6);
        tertiarySlots.get(8).put(0, 4);
        tertiarySlots.get(8).put(2, 5);
        tertiarySlots.get(8).put(6, 7);
    }

    private void aiFillSlot() {
        int slot;
        ArrayList<Integer> dangerousSlots = new ArrayList<>();
        ArrayList<Integer> winningSlots = new ArrayList<>();

        for (int primarySlot : primarySlots) {
            if (board[primarySlot] == 1) {
                // The player has a token in one of the corners
                int[] secondaries = secondarySlots.get(primarySlot);
                for (int secondarySlot : secondaries) {
                    if (board[secondarySlot] == 1) {
                        int tertiarySlot = tertiarySlots.get(primarySlot).get(secondarySlot);
                        if (board[tertiarySlot] == 0) {
                            dangerousSlots.add(tertiarySlot);
                        }
                    }
                }
            }
            if (board[primarySlot] == 2) {
                // KekBot has a token in one of the corners
                int[] secondaries = secondarySlots.get(primarySlot);
                for (int secondarySlot : secondaries) {
                    if (board[secondarySlot] == 2) {
                        int tertiarySlot = tertiarySlots.get(primarySlot).get(secondarySlot);
                        if (board[tertiarySlot] == 0) {
                            winningSlots.add(tertiarySlot);
                        }
                    }
                }
            }
        }

        if (winningSlots.size() > 0) {
            slot = winningSlots.get(random.nextInt(winningSlots.size()));
        } else {
            if (dangerousSlots.size() > 0) {
                slot = dangerousSlots.get(random.nextInt(dangerousSlots.size()));
            } else {
                do {
                    slot = random.nextInt(board.length);
                } while (board[slot] != 0);
            }
        }
        board[slot] = 2;
        if (!checkAI()) {
            if (!checkForDraw()) {
                drawBoard();
            }
        }
    }

    private boolean check(User player) {
        boolean winner = false;
        int playerToken = getPlayerNumber(player);
        for (int i = 0; i < 9; i += 3) {
            if (board[i] == playerToken && board[i+1] == playerToken && board[i+2] == playerToken) {
                winner = true;
                break;
            }
        }
        if (!winner) {
            for (int i = 0; i < 3; i++) {
                if (board[i] == playerToken && board[i+3] == playerToken && board[i+6] == playerToken) {
                    winner = true;
                    break;
                }
            }
        }
        if (!winner) {
            if (board[0] == playerToken && board[4] == playerToken && board[8] == playerToken) winner = true;
            else if (board[2] == playerToken && board[4] == playerToken && board[6] == playerToken) winner = true;
        }
        if (winner) {
            drawBoard();
            channel.sendMessage("\uD83C\uDF89 **" + player.getName() + " wins!** \uD83C\uDF89").queue();
            if (players.size() == maxNumberOfPlayers) endGame(player, random.nextInt(8), ThreadLocalRandom.current().nextInt(4, 7));
            else endGame(player, random.nextInt(3) + 1, random.nextInt(3) + 1);
        }
        return winner;
    }

    private boolean checkAI() {
        boolean winner = false;
        for (int i = 0; i < 9; i += 3) {
            if (board[i] == 2 && board[i+1] == 2 && board[i+2] == 2) {
                winner = true;
                break;
            }
        }
        if (!winner) {
            for (int i = 0; i < 3; i++) {
                if (board[i] == 2 && board[i+3] == 2 && board[i+6] == 2) {
                    winner = true;
                    break;
                }
            }
        }
        if (!winner) {
            if (board[0] == 2 && board[4] == 2 && board[8] == 2) winner = true;
            else if (board[2] == 2 && board[4] == 2 && board[6] == 2) winner = true;
        }
        if (winner) {
            drawBoard();
            channel.sendMessage("\uD83C\uDF89 **KekBot wins!** \uD83C\uDF89").queue();
            endGame();
        }
        return winner;
    }

    private boolean checkForDraw() {
        boolean draw = true;
        for (int i = 0; i < 9; i++) {
            if (board[i] == 0) {
                draw = false;
                break;
            }
        }
        if (draw) {
            drawBoard();
            channel.sendMessage("**It's a draw!**").queue();
            endTie(random.nextInt(2), random.nextInt(3));
            KekBot.gamesManager.closeGame(channel);
        }
        return draw;
    }
}
