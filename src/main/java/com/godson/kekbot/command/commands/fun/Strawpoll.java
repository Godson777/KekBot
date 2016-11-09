package com.godson.kekbot.command.commands.fun;

import com.darichey.discord.api.Command;
import com.godson.kekbot.Objects.GSONStrawpoll;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.entities.TextChannel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Strawpoll {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static Command strawpoll = new Command("strawpoll")
            .withAliases("spoll")
            .withDescription("Creates a poll in Strawpoll for you.")
            .withUsage("{p}strawpoll <Title of Poll> | <option> {use | to separate options, you can have up to 30 different options.}")
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                if (rawSplit.length == 1) {
                    channel.sendMessageAsync("No poll title specified!", null);
                } else {
                    String pollVariables[] = rawSplit[1].split("\\u007c");
                    if (pollVariables.length == 1) {
                        channel.sendMessageAsync("No poll options specified!", null);
                    } else {
                        List<String> list = new ArrayList<>();
                        for (String option : pollVariables) {
                            if (option.matches(".*\\w.*") && !option.equals(pollVariables[0])) {
                                if (option.startsWith(" ")) option = option.replaceFirst("([ ]+)", "");
                                if (option.endsWith(" ")) option = option.replaceAll("([ ]+$)", "");
                                list.add(option);
                            }
                        }
                        String options[] = list.toArray(EMPTY_STRING_ARRAY);
                        GSONStrawpoll poll = new GSONStrawpoll(pollVariables[0]).withOptions(options).isMulti(false);
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        String json = gson.toJson(poll);
                        try {
                            Document document = Jsoup.connect("http://strawpoll.me/api/v2/polls")
                                    .userAgent("Mozilla/5.0").ignoreContentType(true)
                                    .requestBody(json)
                                    .post();
                            channel.sendMessageAsync("https://strawpoll.me/" + gson.fromJson(document.body().text(), GSONStrawpoll.class).getID(), null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
}
