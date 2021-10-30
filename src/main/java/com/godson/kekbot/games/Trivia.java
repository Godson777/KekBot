package com.godson.kekbot.games;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Trivia extends Game {

    private final Map<String, List<TriviaQuestion>> questions = new HashMap<>();
    private final Map<User, Integer> playerPoints = new HashMap<>();
    private final Random random = new Random();

    private boolean roundActive = false;
    private int round = 0;
    private final int maxRounds = 15;
    private final int roundDuration = 10;
    private TriviaQuestion currentQuestion;
    private final List<TriviaQuestion> previousQuestions = new ArrayList<>();

    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture = null;

    public Trivia(TextChannel channel) {
        super(2, 10, false, channel, "Trivia", false);
        isTranslatable = false;
        try {
            for (String s : FileUtils.readLines(new File("resources/games/trivia/questions.txt"), "utf-8")) {
                if (questions.containsKey(s.substring(0, s.indexOf("|")))) {
                    questions.get(s.substring(0, s.indexOf("|"))).add(new TriviaQuestion(s.substring(0, s.indexOf("|")), s.substring(s.indexOf("|") + 1, s.lastIndexOf("|")), s.substring(s.lastIndexOf("|") + 1)));
                } else {
                    List<TriviaQuestion> questions = new ArrayList<>();
                    questions.add(new TriviaQuestion(s.substring(0, s.indexOf("|")), s.substring(s.indexOf("|") + 1, s.lastIndexOf("|")), s.substring(s.lastIndexOf("|") + 1)));
                    this.questions.put(s.substring(0, s.indexOf("|")), questions);
                }
            }
        } catch (IOException e) {
            //Nothing's gonna happen unless the file magically winds up deleted.
            e.printStackTrace();
        }
    }

    @Override
    public void startGame() {
        startRound();
    }

    @Override
    public void acceptInputFromMessage(Message message) {
        if (!roundActive) return;
        if (currentQuestion.answers.stream().noneMatch(s -> s.replaceAll("([':])", "").equalsIgnoreCase(message.getContentRaw().replaceAll("([':])", "")))) return;

        endRound(message.getAuthor());
        scheduledFuture.cancel(true);
    }

    @Override
    public String getRules() {
        return "It's a good-ol' game of Trivia! I'll ask a question, and it's up to everyone to try to figure out the answer, " +
                "whoever gets the correct answer first gets a point. Whoever gets the most points by the end of the game is the winner. " +
                "But don't take too long, as you only have " + roundDuration + " seconds to get the answer, after that, the round is over and no one gets a point.";
    }

    private void startRound() {
        roundActive = true;
        String category = questions.keySet().toArray(new String[questions.size()])[random.nextInt(questions.size())];
        currentQuestion = questions.get(category).get(random.nextInt(questions.get(category).size()));
        while (!previousQuestions.isEmpty() && previousQuestions.contains(currentQuestion)) {
            category = questions.keySet().toArray(new String[questions.size()])[random.nextInt(questions.size())];
            currentQuestion = questions.get(category).get(random.nextInt(questions.get(category).size()));
        }
        previousQuestions.add(currentQuestion);
        channel.sendMessage("Round " + ++round + ":\n\n" + currentQuestion.category + "\n" + currentQuestion.question).queue();
        scheduledFuture = timer.schedule((Runnable) this::endRound, roundDuration, TimeUnit.SECONDS);
    }

    private void endRound(User user) {
        addPoint(user);
        endRound(user.getName() + " won the round!");
    }

    private void endRound() {
        endRound("You took too long to guess the answer!\n\nThe correct answer was: " + currentQuestion.answers.get(0));
    }

    private void endRound(String result) {
        roundActive = false;
        if (round >= maxRounds) {
            List<User> winners = new ArrayList<>(playerPoints.keySet());
            winners.sort(Comparator.comparingInt(playerPoints::get).reversed());
            String space = StringUtils.repeat(" ", 20);
            StringBuilder builder = new StringBuilder();
            for (User player : players) {
                builder.append("`").append(
                        (player.getName().length() > 20 ?
                                player.getName().substring(0, 19) + "..." :
                                (player.getName().length() < 20 ? player.getName() + space.substring(0, 19 - player.getName().length()) :
                                        player.getName())))
                        .append(":` ").append(playerPoints.get(player));
                builder.append("\n");
            }
            channel.sendMessage(result + "\n\nGame over! Here are the results:\n\n" + builder).queue();
            endGame(winners.get(0), playerPoints.get(winners.get(0)), playerPoints.get(winners.get(0)) / 2);
            return;
        }
        channel.sendMessage(result + "\n\nNext round starting in 5 seconds...").queue();
        timer.schedule(this::startRound, 5, TimeUnit.SECONDS);
    }

    private void addPoint(User user) {
        if (!playerPoints.containsKey(user)) {
            playerPoints.put(user, 1);
        } else playerPoints.replace(user, playerPoints.get(user) + 1);
    }

    @Override
    public void endTie() {
        timer.shutdownNow();
        super.endTie();
    }

    private class TriviaQuestion {
        private String category;
        private String question;
        private List<String> answers = new ArrayList<>();

        private TriviaQuestion(String category, String question, String answer) {
            this.category = category;
            this.question = question;
            String[] answers = answer.split("/");
            this.answers.addAll(Arrays.asList(answers));
        }
    }
}
