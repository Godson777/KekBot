package com.godson.kekbot.command.commands.admin;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.util.LocaleUtils;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.responses.Action;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Purge extends Command {

    public Purge() {
        name = "purge";
        description = "Mass deletes X number of messages. Supply a keyphrase or a mention {or multiple mentions} to purge all messages within X number of messages.";
        usage.add("purge <number> {keyphrase or @mention(s)}");
        aliases = new String[]{"prune", "clear"};
        category = new Category("Admin");
        requiredBotPerms = new Permission[]{Permission.MESSAGE_MANAGE};
        requiredUserPerms = new Permission[]{Permission.MESSAGE_MANAGE};
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length < 1) {
            event.getTextChannel().sendMessage(LocaleUtils.getString("command.noargs", event.getLocale(), "`" + event.getPrefix() + "help " + name + "`")).queue();
            return;
        }

        try {
            int purge = Integer.parseInt(event.getArgs()[0]);
            if (purge <= 1) {
                event.getTextChannel().sendMessage(KekBot.respond(Action.PURGE_TOOLOW, event.getLocale())).queue();
            } else if (purge > 100) {
                event.getTextChannel().sendMessage(KekBot.respond(Action.PURGE_TOOHIGH, event.getLocale())).queue();
            } else {
                event.getMessage().delete().queue(useless -> {
                    event.getTextChannel().getHistory().retrievePast(purge).queue(msgsRaw -> {
                        List<Message> msgs = msgsRaw.stream().filter(message -> !message.getTimeCreated().plusWeeks(2).isBefore(OffsetDateTime.now())).collect(Collectors.toList());
                        event.getTextChannel().sendMessage(LocaleUtils.getString("command.admin.purge.purging", event.getLocale())).queue(msg -> {
                            if (msgs.size() > 0) {
                                if (event.getArgs().length >= 2) {
                                    String keyphrase = event.combineArgs(1);
                                    if (event.getMentionedUsers().size() == 0) {
                                        List<Message> keywordPurge = msgs.stream().filter(mes -> mes.getContentRaw().toLowerCase().contains(keyphrase.toLowerCase())).collect(Collectors.toList());
                                        if (keywordPurge.size() > 1)
                                            event.getTextChannel().deleteMessages(keywordPurge).queue();
                                        else if (keywordPurge.size() == 1)
                                            keywordPurge.get(0).delete().queue();
                                        if (keywordPurge.size() >= 1)
                                            msg.editMessage(KekBot.respond(Action.KEYPHRASE_PURGE_SUCCESS, event.getLocale(), "`" + keywordPurge.size() + "`", "`" + keyphrase + "`")).queue();
                                        else
                                            msg.editMessage(KekBot.respond(Action.KEYPHRASE_PURGE_FAIL, event.getLocale())).queue();
                                    } else {
                                        List<String> mentions = new ArrayList<String>();
                                        event.getMentionedUsers().forEach(user -> mentions.add(user.getAsMention()));
                                        Pattern p = Pattern.compile("([A-Z])+", Pattern.CASE_INSENSITIVE);
                                        Matcher m = p.matcher(keyphrase);
                                        if (mentions.stream().anyMatch(mentions::contains) && m.find()) {
                                            msg.editMessage(LocaleUtils.getString("command.admin.purge.filtererror", event.getLocale())).queue();
                                        } else {
                                            List<Message> mentionPurge = msgs.stream().filter(mes -> event.getMentionedUsers().stream().anyMatch(mes.getAuthor()::equals)).collect(Collectors.toList());
                                            if (mentionPurge.size() > 1)
                                                event.getTextChannel().deleteMessages(mentionPurge).queue();
                                            else if (mentionPurge.size() == 1)
                                                mentionPurge.get(0).delete().queue();
                                            if (mentionPurge.size() >= 1)
                                                msg.editMessage(KekBot.respond(Action.MENTION_PURGE_SUCCESS, event.getLocale(), "`" + mentionPurge.size() + "`", "`" + StringUtils.join(event.getMentionedUsers().stream().map(user -> user.getName() + "#" + user.getDiscriminator()).collect(Collectors.toList()), ", ") + "`")).queue();
                                            else
                                                msg.editMessage(KekBot.respond(Action.MENTION_PURGE_FAIL, event.getLocale())).queue();
                                        }
                                    }
                                } else {
                                    if (msgs.size() == 1)
                                        msgs.get(0).delete().queue(delet -> msg.editMessage(KekBot.respond(Action.PURGE_SUCCESS, event.getLocale(), "`" + msgs.size() + "`")).queue());
                                    else
                                        event.getTextChannel().deleteMessages(msgs).queue(delet -> msg.editMessage(KekBot.respond(Action.PURGE_SUCCESS, event.getLocale(), "`" + msgs.size() + "`")).queue());
                                }
                            } else
                                msg.editMessage(LocaleUtils.getString("command.admin.purge.purgeerror", event.getLocale())).queue();
                        });
                    });
                });
            }
        } catch (NumberFormatException e) {
            event.getTextChannel().sendMessage(KekBot.respond(Action.NOT_A_NUMBER, event.getLocale(), "`" + event.getArgs()[0] + "`")).queue();
        }
    }
}
