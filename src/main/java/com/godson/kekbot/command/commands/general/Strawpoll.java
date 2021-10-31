package com.godson.kekbot.command.commands.general;

import com.godson.kekbot.objects.SPoll;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Strawpoll extends Command {

    private final String[] EMPTY_STRING_ARRAY = new String[0];

    public Strawpoll() {
        name = "strawpoll";
        aliases = new String[]{"spoll"};
        description = "Creates a poll in Strawpoll for you.";
        usage.add("strawpoll <Title of Poll> | <option> | <option> | {option...}");
        extendedDescription = "Use the | symbol to separate options, you can have up to 30 different options.";
        exDescPos = ExtendedPosition.AFTER;
        category = new Category("General");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length > 0) {
            String combinedArgs = event.combineArgs();
            String[] pollVariables = combinedArgs.split("\\u007c");
            if (pollVariables.length == 1) {
                event.getChannel().sendMessage(event.getString("command.general.poll.nooptions")).queue();
            } else {
                List<String> list = new ArrayList<>();
                for (String option : pollVariables) {
                    if (option.matches(".*\\w.*") && !option.equals(pollVariables[0])) {
                        if (option.startsWith(" ")) option = option.replaceFirst("([ ]+)", "");
                        if (option.endsWith(" ")) option = option.replaceAll("([ ]+$)", "");
                        list.add(option);
                    }
                }
                String[] options = list.toArray(EMPTY_STRING_ARRAY);
                SPoll poll = new SPoll(pollVariables[0]).withOptions(options).isMulti(false);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(poll);
                try {
                    Document document = Jsoup.connect("https://www.strawpoll.me/api/v2/polls")
                            .userAgent("Mozilla/5.0").ignoreContentType(true)
                            .requestBody(json)
                            .post();
                    event.getChannel().sendMessage("https://strawpoll.me/" + gson.fromJson(document.body().text(), SPoll.class).getID()).queue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else event.getChannel().sendMessage(event.getString("command.noargs", "`" + event.getPrefix() + "help strawpoll`")).queue();
    }
}
