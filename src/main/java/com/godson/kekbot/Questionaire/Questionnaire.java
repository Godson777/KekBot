package com.godson.kekbot.Questionaire;

import com.darichey.discord.api.CommandContext;
import com.godson.kekbot.KekBot;
import com.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.*;
import java.util.function.Consumer;

public class Questionnaire {
    private List<Question> questions = new ArrayList<>();
    private EventWaiter waiter = KekBot.waiter;
    private List<Object> answers = new ArrayList<>();
    private Map<Question, List<String>> choices = new HashMap<>();
    private boolean skipQuestionMessage = false;
    private boolean skipOnRepeat = false;
    private boolean customErrorMessageEnabled = false;
    private String customErrorMessage;

    //Guild Info:
    private Guild guild;
    private TextChannel channel;
    private User user;
    private CommandContext context;

    private Consumer<Results> results = results -> {};

    public Questionnaire(CommandContext context) {
        this.context = context;
        this.guild = context.getGuild();
        this.channel = context.getTextChannel();
        this.user = context.getAuthor();
    }

    public Questionnaire(MessageReceivedEvent event) {
        this.guild = event.getGuild();
        this.channel = event.getTextChannel();
        this.user = event.getAuthor();
    }

    public Questionnaire(Results results) {
        if (results.context != null) this.context = results.context;
        this.guild = results.getGuild();
        this.channel = results.getChannel();
        this.user = results.getUser();
    }

    public Questionnaire withoutRepeats() {
        this.skipOnRepeat = true;
        return this;
    }

    public Questionnaire withCustomErrorMessage(String customErrorMessage) {
        customErrorMessageEnabled = true;
        this.customErrorMessage = customErrorMessage;
        return this;
    }

    public Questionnaire addQuestion(String message, QuestionType type) {
        //Allows addition of other "Types" that require more params.
        if (type.equals(QuestionType.CHOICE_STRING)) {
            String method = "";
            //Add any new "types" to this switch, as well as the method used to create questions of that type.
            switch (type) {
                case CHOICE_STRING:
                    method = "addChoiceQuestion()";
                    break;
                case YES_NO_STRING:
                    method = "addYesNoQuestion()";
                    break;
            }
            throw new IllegalArgumentException("You are not allowed to set this type of question. (Please use " + method + " to use this type.");
        }
        questions.add(new Question(message).setType(type));
        return this;
    }

    public Questionnaire addChoiceQuestion(String message, String... choices) {
        Question question = new Question(message).setType(QuestionType.CHOICE_STRING);
        questions.add(question);
        this.choices.put(question, Arrays.asList(choices));
        return this;
    }

    public Questionnaire addYesNoQuestion(String message) {
        Question question = new Question(message).setType(QuestionType.YES_NO_STRING);
        questions.add(question);
        String[] choices = {"yes", "y", "no", "n"};
        this.choices.put(question, Arrays.asList(choices));
        return this;
    }

    public void execute(Consumer<Results> results) {
        if (context != null) context.getRegistry().disableUserInGuild(guild, user);
        this.results = results;
        execute(0);
    }

    private void execute(int i) {
        Question question = questions.get(i);
        if (!skipQuestionMessage) channel.sendMessage(question.getMessage()).queue();
        waiter.waitForEvent(GuildMessageReceivedEvent.class, e -> e.getAuthor().equals(user) && e.getChannel().equals(channel), e -> {
            String message = e.getMessage().getContent();
            RestAction<Message> errorMessage = e.getChannel().sendMessage((!customErrorMessageEnabled ? "I'm sorry, I didn't quite catch that, let's try that again..." : customErrorMessage));
            if (message.equalsIgnoreCase("cancel")) {
                e.getChannel().sendMessage("Cancelled.").queue();
                if (context != null) context.getRegistry().enableUserInGuild(guild, user);
            } else {
                switch (question.getType()) {
                    case STRING:
                        answers.add(message);
                        break;
                    case INT:
                        try {
                            answers.add(Integer.valueOf(message));
                        } catch (NumberFormatException e1) {
                            if (skipOnRepeat) skipQuestionMessage = true;
                            errorMessage.queue();
                            execute(i);
                            return;
                        }
                        break;
                    case CHOICE_STRING:
                        Optional<String> choice = choices.get(question).stream().filter(c -> c.equalsIgnoreCase(e.getMessage().getContent())).findFirst();
                        if (!choice.isPresent()) {
                            if (skipOnRepeat) skipQuestionMessage = true;
                            errorMessage.queue();
                            execute(i);
                            return;
                        } else {
                            answers.add(choice.get());
                        }
                        break;
                    case YES_NO_STRING:
                        Optional<String> yesNoChoice = choices.get(question).stream().filter(c -> c.equalsIgnoreCase(e.getMessage().getContent())).findFirst();
                        if (!yesNoChoice.isPresent()) {
                            if (skipOnRepeat) skipQuestionMessage = true;
                            errorMessage.queue();
                            execute(i);
                            return;
                        } else {
                            answers.add(yesNoChoice.get());
                        }
                }
                if (i + 1 != questions.size()) {
                    if (skipOnRepeat && skipQuestionMessage) skipQuestionMessage = false;
                    execute(i + 1);
                } else {
                    if (context != null) context.getRegistry().enableUserInGuild(guild, user);
                    finish();
                }
            }
        });
    }

    private void finish() {
        results.accept(new Results(this));
    }

    public class Results {
        private Questionnaire questionnaire;
        private List<Object> answers;
        private Guild guild;
        private TextChannel channel;
        private User user;
        private CommandContext context;


        Results(Questionnaire questionnaire) {
            this.questionnaire = questionnaire;
            this.answers = questionnaire.answers;
            this.guild = questionnaire.guild;
            this.channel = questionnaire.channel;
            this.user = questionnaire.user;
            if (questionnaire.context != null) this.context = questionnaire.context;
        }

        public Object getAnswer(int i) {
            return answers.get(i);
        }

        public List<Object> getAnswers() {
            return answers;
        }

        public Guild getGuild() {
            return guild;
        }

        public TextChannel getChannel() {
            return channel;
        }

        public User getUser() {
            return user;
        }

        public void reExecute() {
            questionnaire.answers.clear();
            questionnaire.execute(results);
        }

        public void reExecuteWithoutMessage() {
            questionnaire.answers.clear();
            questionnaire.skipQuestionMessage = true;
            questionnaire.execute(results);
        }
    }

}
