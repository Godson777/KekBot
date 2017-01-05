package com.godson.kekbot.commands;

import com.darichey.discord.api.Command;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class test {
    public static Command test = new Command("test")
            .onExecuted(context -> {

            });
}
