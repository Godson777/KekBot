package com.godson.kekbot;

import com.godson.kekbot.Objects.UDictionary;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Responses.Responder;
import com.godson.kekbot.Responses.ResponseSuggestions;
import com.godson.kekbot.Settings.*;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.entities.Guild;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GSONUtils {

    public static Settings getSettings(Guild guild) {
        Settings settings = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader("settings/" + guild.getId() + "/Settings.json"));
            Gson gson = new Gson();
            settings = gson.fromJson(br, Settings.class);
            br.close();
        } catch (FileNotFoundException e) {
            settings = new Settings();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return settings;
    }

    public static Responder getResponder(Action action) {
        Responder response = new Responder(action);
        try {
            BufferedReader br = new BufferedReader(new FileReader("responses/" + action.name() + ".json"));
            Gson gson = new Gson();
            response = gson.fromJson(br, Responder.class);
            response.setAction(action);
            br.close();
        } catch (FileNotFoundException e) {
            //do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static ResponseSuggestions getSuggestions() {
        ResponseSuggestions suggestions = new ResponseSuggestions();
        try {
            BufferedReader br = new BufferedReader(new FileReader("responses/responses.json"));
            Gson gson = new Gson();
            suggestions = gson.fromJson(br, ResponseSuggestions.class);
            br.close();
        } catch (FileNotFoundException e) {
            //do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
        return suggestions;
    }

    public static TagManager getTagManager(Guild guild) {
        TagManager manager = new TagManager();
        try {
            BufferedReader br = new BufferedReader(new FileReader("settings/" + guild.getId() + "/Tags.json"));
            Gson gson = new Gson();
            manager = gson.fromJson(br, TagManager.class);
            br.close();
        } catch (FileNotFoundException e) {
            //do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
        return manager;
    }

    public static Quotes getQuotes(Guild guild) {
        Quotes quotes = new Quotes();
        try {
            BufferedReader br = new BufferedReader(new FileReader("settings/" + guild.getId() + "/Quotes.json"));
            Gson gson = new Gson();
            quotes = gson.fromJson(br, Quotes.class);
            br.close();
        } catch (FileNotFoundException e) {
            //do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
        return quotes;
    }

    public static Config getConfig() {
        Config config = new Config();
        try {
            BufferedReader br = new BufferedReader(new FileReader("config/config.json"));
            Gson gson = new Gson();
            config = gson.fromJson(br, Config.class);
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("config.json not found! What have you done with it?!");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static TicketManager getTicketManager() {
        TicketManager manager = new TicketManager();
        try {
            BufferedReader br = new BufferedReader(new FileReader("tickets.json"));
            Gson gson = new Gson();
            manager = gson.fromJson(br, TicketManager.class);
            br.close();
        } catch (FileNotFoundException e) {
            manager.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return manager;
    }

    public static UDictionary getUDResults(String word) {
        UDictionary uDictionary = new UDictionary();
        try {
            HttpResponse<String> response = Unirest.get("https://mashape-community-urban-dictionary.p.mashape.com/define?term=" + word)
                    .header("X-Mashape-Key", "ceU4edWIr7mshi68Xs4IQYUQ7XgTp1ILJUgjsnsO4Qf4MOc543")
                    .header("Accept", "text/plain")
                    .asString();
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getRawBody()));
            Gson gson = new Gson();
            uDictionary = gson.fromJson(br, UDictionary.class);
            br.close();
        } catch (IOException | UnirestException e) {
            e.printStackTrace();
        }
        return uDictionary;
    }

    public static int numberOfCCommands(Guild guild) {
        File folder = new File("settings/" + guild.getId() + "/commands/");
        if (folder.exists() && folder.isDirectory()) {
            try {
                return folder.listFiles().length;
            } catch (NullPointerException e) {
                return 0;
            }
        } else return 0;
    }

    public static List<CustomCommand> getCCommands(Guild guild) {
        List<CustomCommand> commands = new ArrayList<>();
        for (int i = 0; i < numberOfCCommands(guild); i++) {
            CustomCommand command;
            File folder = new File("settings/" + guild.getId() + "/commands/");
            try {
                BufferedReader br = new BufferedReader(new FileReader(folder.listFiles()[i]));
                Gson gson = new Gson();
                command = gson.fromJson(br, CustomCommand.class);
                br.close();
                commands.add(command);
            } catch (FileNotFoundException e) {
                //do nothing
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return commands;
    }

    public static CustomCommand getCCommand(String name, Guild guild) {
        Optional<CustomCommand> command = getCCommands(guild).stream().filter(cmd -> cmd.getName().equals(name)).findFirst();
        if (command.isPresent()) {
            return command.get();
        } else throw new NullPointerException("How did you manage to cause THIS error!?");
    }
}
