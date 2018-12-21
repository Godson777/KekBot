package com.godson.kekbot.questionaire;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.util.LocaleUtils;
import com.godson.kekbot.command.CommandEvent;
import com.google.gson.internal.Primitives;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Questionnaire {
    private final List<Question> questions = new ArrayList<>();
    private final EventWaiter waiter = KekBot.waiter;
    private final List<Object> answers = new ArrayList<>();
    private final Map<Question, List<String>> choices = new HashMap<>();
    private boolean skipQuestionMessage = false;
    private boolean skipOnRepeat = false;
    private boolean customErrorMessageEnabled = false;
    private boolean includeCancel = true;
    private String customErrorMessage;
    private boolean useRawInput = false;

    //Guild Info:
    private final Guild guild;
    private final TextChannel channel;
    private final User user;
    private final String locale;

    //Timeout Stuff:
    private long timeout = 1;
    private TimeUnit timeoutUnit = TimeUnit.MINUTES;

    //Interruption Stuff:
    private Predicate<Event> interruptionCondition;
    private Consumer<Event> interruptionAction;

    private Consumer<Results> results = results -> {};

    public static Questionnaire newQuestionnaire(CommandEvent event) {
        return new Questionnaire(event);
    }

    public static Questionnaire newQuestionnaire(MessageReceivedEvent event) {
        return new Questionnaire(event);
    }

    public static Questionnaire newQuestionnaire(Results results) {
        return new Questionnaire(results);
    }

    public static Questionnaire newQuestionnaire(Guild guild, TextChannel channel, User user) {
        return new Questionnaire(guild, channel, user);
    }

    private Questionnaire(CommandEvent event) {
        this.guild = event.getGuild();
        this.channel = event.getTextChannel();
        this.user = event.getAuthor();
        this.locale = event.getLocale();
    }

    private Questionnaire(MessageReceivedEvent event) {
        this.guild = event.getGuild();
        this.channel = event.getTextChannel();
        this.user = event.getAuthor();
        this.locale = KekBot.getCommandClient().getLocale(guild.getId());
    }

    private Questionnaire(Guild guild, TextChannel channel, User user) {
        this.guild = guild;
        this.channel = channel;
        this.user = user;
        this.locale = KekBot.getCommandClient().getLocale(guild.getId());
    }

    private Questionnaire(Results results) {
        this.guild = results.getGuild();
        this.channel = results.getChannel();
        this.user = results.getUser();
        this.locale = KekBot.getCommandClient().getLocale(guild.getId());
    }

    public Questionnaire withoutRepeats() {
        this.skipOnRepeat = true;
        return this;
    }

    public Questionnaire withTimeout(long time, TimeUnit unit) {
        timeout = unit.toMillis(time);
        timeoutUnit = unit;
        return this;
    }

    public Questionnaire includeCancel(boolean includeCancel) {
        this.includeCancel = includeCancel;
        return this;
    }

    public Questionnaire withCustomErrorMessage(String customErrorMessage) {
        customErrorMessageEnabled = true;
        this.customErrorMessage = customErrorMessage;
        return this;
    }

    public Questionnaire useRawInput() {
        useRawInput = true;
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

    public Questionnaire withInterruption(Predicate<Event> interruptionCondition, Consumer<Event> interruptionAction) {
        this.interruptionCondition = interruptionCondition;
        this.interruptionAction = interruptionAction;
        return this;
    }

    public void execute(Consumer<Results> results) {
        KekBot.getCommandClient().registerQuestionnaire(channel.getId(), user.getId());
        this.results = results;
        execute(0);
    }

    private void execute(int i) {
        Question question = questions.get(i);
        if (!skipQuestionMessage) channel.sendMessage(question.getMessage() + (includeCancel ? " " + LocaleUtils.getString("questionnaire.cancelmessage", locale, "`" + "cancel" + "`") : "")).queue();
        //here comes some crazy shit
        waiter.waitForEvent(Event.class, e -> {
            if (e instanceof GuildMessageReceivedEvent)
                return ((GuildMessageReceivedEvent) e).getAuthor().equals(user) && ((GuildMessageReceivedEvent) e).getChannel().equals(channel);
            else
                return interruptionCondition != null && interruptionCondition.test(e);
        }, e -> {
            if (e instanceof GuildMessageReceivedEvent) {
                String message = (useRawInput ? ((GuildMessageReceivedEvent) e).getMessage().getContentRaw() : ((GuildMessageReceivedEvent) e).getMessage().getContentDisplay());
                RestAction<Message> errorMessage = ((GuildMessageReceivedEvent) e).getChannel().sendMessage((!customErrorMessageEnabled ? LocaleUtils.getString("questionnaire.error", locale) : customErrorMessage));
                if (message.equalsIgnoreCase("cancel")) {
                    ((GuildMessageReceivedEvent) e).getChannel().sendMessage(LocaleUtils.getString("questionnaire.cancelled", locale)).queue();
                    KekBot.getCommandClient().unregisterQuestionnaire(channel.getId(), user.getId());
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
                            Optional<String> choice = choices.get(question).stream().filter(c -> c.equalsIgnoreCase(((GuildMessageReceivedEvent) e).getMessage().getContentDisplay())).findFirst();
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
                            Optional<String> yesNoChoice = choices.get(question).stream().filter(c -> c.equalsIgnoreCase(((GuildMessageReceivedEvent) e).getMessage().getContentDisplay())).findFirst();
                            if (!yesNoChoice.isPresent()) {
                                if (skipOnRepeat) skipQuestionMessage = true;
                                errorMessage.queue();
                                execute(i);
                                return;
                            } else {
                                if (yesNoChoice.get().equalsIgnoreCase("y") || yesNoChoice.get().equalsIgnoreCase("yes"))
                                    answers.add(true);
                                else answers.add(false);
                            }
                    }
                    if (i + 1 != questions.size()) {
                        if (skipOnRepeat && skipQuestionMessage) skipQuestionMessage = false;
                        execute(i + 1);
                    } else {
                        KekBot.getCommandClient().unregisterQuestionnaire(channel.getId(), user.getId());
                        finish();
                    }
                }
            } else {
                KekBot.getCommandClient().unregisterQuestionnaire(channel.getId(), user.getId());
                interruptionAction.accept(e);
            }
        }, timeout, timeoutUnit, () -> {
            KekBot.getCommandClient().unregisterQuestionnaire(channel.getId(), user.getId());
            channel.sendMessage(LocaleUtils.getString("questionnaire.noinput", locale) + " " + LocaleUtils.getString("questionnaire.cancelled", locale)).queue();
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
        private CommandEvent event;


        Results(Questionnaire questionnaire) {
            this.questionnaire = questionnaire;
            this.answers = questionnaire.answers;
            this.guild = questionnaire.guild;
            this.channel = questionnaire.channel;
            this.user = questionnaire.user;
        }

        public Object getAnswer(int i) {
            return answers.get(i);
        }

        public <T> T getAnswerAsType(int i, Class<T> classOfT) {
            return Primitives.wrap(classOfT).cast(answers.get(i));
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

    class Question {
        private QuestionType type;
        private String message;

        public Question(String message) {
            this.message = message;
        }

        public Question setType(QuestionType type) {
            this.type = type;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public QuestionType getType() {
            return type;
        }
    }

}
