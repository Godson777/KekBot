package com.godson.kekbot.command.commands;


import com.godson.kekbot.CustomEmote;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.KekBot;
import com.godson.kekbot.Utils;
import com.godson.kekbot.profile.Profile;
import com.godson.kekbot.responses.Action;
import com.godson.kekbot.responses.Responder;
import com.godson.kekbot.settings.QuoteManager;
import com.godson.kekbot.settings.Settings;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.settings.TagManager;
import com.google.gson.Gson;
import com.rethinkdb.model.MapObject;
import com.sun.imageio.plugins.gif.GIFImageWriterSpi;
import net.dv8tion.jda.core.entities.Guild;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Inet4Address;

public class TestCommand extends Command {

    public TestCommand() {
        name = "test";
        description = "test";
        category = new Category("Test");
    }

    @Override
    public void onExecuted(CommandEvent event) {
    }
}
