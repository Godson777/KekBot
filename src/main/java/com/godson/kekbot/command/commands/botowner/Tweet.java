package com.godson.kekbot.command.commands.botowner;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.questionaire.QuestionType;
import com.godson.kekbot.questionaire.Questionnaire;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.core.entities.Message;
import twitter4j.StatusUpdate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class Tweet extends Command {

    public Tweet() {
        name = "tweet";
        category = new Category("Bot Owner");
        commandPermission = CommandPermission.OWNER;
    }

    @Override
    public void onExecuted(CommandEvent event) throws IOException {
        //If there aren't any arguments.
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage("Not enough args.").queue();
            return;
        }

        //Converts 1st argument to integer.
        int toSkip;
        try {
             toSkip = Integer.valueOf(event.getArgs()[0]);
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, "`" + event.getArgs()[0] + "`")).queue();
            return;
        }

        //Prepares tweet with user-defined message.
        StatusUpdate status = new StatusUpdate((event.getArgs().length > 1 ? event.combineArgs(1) : ""));

        //Applies image attachment to tweet, if any found in message.
        if (event.getMessage().getAttachments().size() > 0) {
            if (event.getMessage().getAttachments().get(0).isImage()) {
                Message.Attachment attachment = event.getMessage().getAttachments().get(0);
                status.setMedia(attachment.getFileName(), attachment.getInputStream());
            }
        }

        //Calculates an estimate of when the tweet will be sent,
        OffsetDateTime estimate = KekBot.twitterManager.calculateOverride(toSkip);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM dd, 'at' hh:mma").withZone(ZoneId.systemDefault());

        Questionnaire.newQuestionnaire(event)
                .addYesNoQuestion("The estimated time of this tweet being posted is: `" + estimate.format(formatter) + "`. Is this okay?")
                .execute(results -> {
                    if (results.getAnswerAsType(0, boolean.class)) {
                        KekBot.twitterManager.overrideTweet(estimate.minusSeconds(10), status);
                    }
                });
    }
}
