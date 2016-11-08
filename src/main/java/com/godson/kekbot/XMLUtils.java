package com.godson.kekbot;

import com.godson.kekbot.Settings.Settings;
import com.godson.kekbot.Settings.Tag;
import com.godson.kekbot.Settings.TagManager;
import net.dv8tion.jda.entities.*;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.util.*;

public class XMLUtils {

    public static void portToJSON() throws IOException, JDOMException {
        for (Guild guild : KekBot.jdas[0].getGuilds()) {
            File settings = new File("settings\\" + guild.getId() + ".xml");
            Document document = null;
            Element root = null;
            FileInputStream fis = new FileInputStream(settings);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
            Settings guildSettings = new Settings();
            guildSettings.setName(guild.getName());
            TagManager manager = new TagManager();
            if (root.getChild("prefix") != null) {
                guildSettings.setPrefix(root.getChild("prefix").getText());
            }

            if (root.getChild("announce") != null) {
                Element announce = root.getChild("announce");
                if (announce.getChild("welcome") != null) {
                    Element welcome = announce.getChild("welcome");
                    guildSettings.toggleWelcome(true);
                    if (welcome.getChild("message") != null) {
                        guildSettings.setWelcomeMessage(welcome.getChild("message").getText());
                    }
                    if (welcome.getChild("channel") != null) {
                        guildSettings.setWelcomeChannel(KekBot.jdas[0].getTextChannelById(welcome.getChild("channel").getText()));
                    }
                }
                if (announce.getChild("goodbye") != null) {
                    Element farewell = announce.getChild("goodbye");
                    guildSettings.toggleFarewell(true);
                    if (farewell.getChild("message") != null) {
                        guildSettings.setFarewellMessage(farewell.getChild("message").getText());
                    }
                    if (farewell.getChild("channel") != null) {
                        guildSettings.setFarewellChannel(KekBot.jdas[0].getTextChannelById(farewell.getChild("channel").getText()));
                    }
                }
                if (announce.getChild("broadcasts") != null) {
                    Element broadcasts = announce.getChild("broadcasts");
                    if (broadcasts.getChild("status") != null) {
                        if (broadcasts.getChild("status").getText().equals("disabled")) guildSettings.toggleBroadcasts(false);
                    }
                    if (broadcasts.getChild("channel") != null) {
                        guildSettings.setBroadcastChannel(KekBot.jdas[0].getTextChannelById(broadcasts.getChild("channel").getText()));
                    }
                }
            }

            if (root.getChild("auto_role") != null) {
                if (root.getChild("auto_role").getChild("role") != null) {
                    guildSettings.setAutoRoleID(root.getChild("auto_role").getChild("role").getText());
                }
            }

            if (root.getChild("tags") != null) {
                for (Element tag : root.getChild("tags").getChildren()) {
                    String name = tag.getChild("name").getText();
                    String contents = tag.getChild("value").getText();
                    String creatorID = tag.getChild("author").getText();
                    String time = (tag.getChild("created_at") != null ? tag.getChild("created_at").getText() : "N/A");
                    Tag toPort = new Tag(name).setContents(contents).setCreator(KekBot.jdas[0].getUserById(creatorID)).setTime(time);
                    manager.addTag(toPort);
                }
            }

            if (!manager.hasNoTags()) manager.save(guild);
            guildSettings.save(guild);
        }
    }
}
