package com.godson.kekbot;

import com.godson.kekbot.objects.UDictionary;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.responses.Responder;
import com.godson.kekbot.responses.ResponseSuggestions;
import com.godson.kekbot.settings.*;
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
