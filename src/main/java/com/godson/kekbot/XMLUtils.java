package com.godson.kekbot;

import com.darichey.discord.api.CommandRegistry;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalNameException;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import sx.blah.discord.handle.obj.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class XMLUtils {


    public static void setPrefix(IGuild server, String prefix) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + server.getID() + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }

        if (root.getChild("prefix") != null) {
            if (!prefix.equals(CommandRegistry.getForClient(KekBot.client).getPrefix())) {
                root.getChild("prefix").setText(prefix);
                CommandRegistry.getForClient(KekBot.client).setPrefixForGuild(server, prefix);
            } else {
                root.removeChild("prefix");
                CommandRegistry.getForClient(KekBot.client).deletePrefixForGuild(server);
            }
        } else {
            if (!prefix.equals(CommandRegistry.getForClient(KekBot.client).getPrefix())) {
                root.addContent(new Element("prefix").setText(prefix));
                CommandRegistry.getForClient(KekBot.client).setPrefixForGuild(server, prefix);
            }
        }


        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + server.getID() + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPrefix(IGuild server) {
        String prefix = null;
        try {
            Document document = null;
            Element root = null;

            File xmlFile = new File("settings\\" + server.getID() + ".xml");
            if (xmlFile.exists()) {
                FileInputStream fis = new FileInputStream(xmlFile);
                SAXBuilder sb = new SAXBuilder();
                document = sb.build(fis);
                root = document.getRootElement();
                fis.close();
            } else {
                // if it does not exist create a new document and new root
                document = new Document();
                root = new Element("settings");
            }
            if (root.getChild("prefix") != null) {
                prefix = root.getChild("prefix").getText();
            } else {
                prefix = null;
            }
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return prefix;
    }

    public static void addTag(String serverID, IChannel channel, IUser author, String tagName, String value) throws JDOMException, IOException, IllegalNameException {
        Document document = null;
        Element root = null;
        Date creation = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM dd, hh:mma ('EST')");

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }
        Element tags = new Element("tags");
        Element tagElement = new Element("tag");
        tagElement.addContent(new Element("name").setText(tagName));
        tagElement.addContent(new Element("value").setText(value));
        tagElement.addContent(new Element("author").setText(author.getID()));
        tagElement.addContent(new Element("created_at").setText(format.format(creation)));

        Optional<Element> tag;

        if (root.getChild("tags") != null) {
            tag = root.getChild("tags").getChildren().stream().filter(t -> t.getChild("name").getValue().equals(tagName)).findFirst();
            if (!tag.isPresent()) {
                root.getChild("tags").addContent(tagElement);
                EasyMessage.send(channel, "Successfully added tag!");
            } else {
                EasyMessage.send(channel, "Tag \"" + tagName + "\" already exists!");
            }
        } else {
            tags.addContent(tagElement);
            root.addContent(tags);
            EasyMessage.send(channel, "Successfully added tag!");
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeTag(String serverID, IChannel channel, IUser user, String tagName) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }

        Optional<Element> tag;

        if (root.getChild("tags") != null) {
            tag = root.getChild("tags").getChildren().stream().filter(t -> t.getChild("name").getValue().equals(tagName)).findFirst();
            if (tag.isPresent()) {
                if (tag.get().getChild("author").getValue().equals(user.getID()) || channel.getModifiedPermissions(user).contains(Permissions.ADMINISTRATOR)) {
                    tag.get().detach();
                    EasyMessage.send(channel, "Successfully removed tag \"" + tagName + "\".");
                } else {
                    EasyMessage.send(channel, "You can't delete tags that don't belong to you!");
                }
            } else {
                EasyMessage.send(channel, "Tag \"" + tagName + "\" doesn't exist!");
            }
        } else {
            EasyMessage.send(channel, "There are no tags to remove!");
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getTagInfo(String serverID, IChannel channel, String tagName) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }

        Optional<Element> tag;

        if (root.getChild("tags") != null) {
            tag = root.getChild("tags").getChildren().stream().filter(t -> t.getChild("name").getValue().equals(tagName)).findFirst();
            if (tag.isPresent()) {
                EasyMessage.send(channel, "Creator: " + KekBot.client.getUserByID(tag.get().getChild("author").getText()).getName() +
                        "\nCreated at: " + (tag.get().getChild("created_at") != null ? tag.get().getChild("created_at").getText() : "N/A"));
            } else {
                EasyMessage.send(channel, "I couldn't find any tags with the name \"" + tagName + "\"");
            }
        } else {
            EasyMessage.send(channel, "I couldn't find any tags with the name \"" + tagName + "\"");
        }
    }

    public static void listTags(IGuild server, IChannel channel) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + server.getID() + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }

        if (root.getChild("tags") != null) {
            if (root.getChild("tags").getChildren().size() != 0) {
                List<String> tagList = new ArrayList<>();
                for (int i = 0; i < root.getChild("tags").getChildren().size(); i++) {
                    tagList.add(root.getChild("tags").getChildren().get(i).getChild("name").getText());
                }
                EasyMessage.send(channel, "The tags for " + server.getName() + " are: \n`" + StringUtils.join(tagList, ", ") + "`");
            } else {
                EasyMessage.send(channel, "This server doesn't seem to have any tags...");
            }
        } else {
            EasyMessage.send(channel, "This server doesn't seem to have any tags...");
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + server.getID() + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendTag(String serverID, IChannel channel, String tagName) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }

        Optional<Element> tag = root.getChild("tags").getChildren().stream().filter(t -> t.getChild("name").getText().equals(tagName)).findFirst();

        if (root.getChild("tags") != null) {
            if (root.getChild("tags").getChildren().size() != 0) {
                if (tag.isPresent()) {
                    EasyMessage.send(channel, tag.get().getChild("value").getText());
                } else {
                    EasyMessage.send(channel, "I could not find any tags with the name \"" + tagName + "\".");
                }
            } else {
                EasyMessage.send(channel, "This server doesn't seem to have any tags...");
            }
        } else {
            EasyMessage.send(channel, "This server doesn't seem to have any tags...");
        }
    }

    public static void setWelcomeChannel(String serverID, IChannel channel, IChannel welcomeChannel) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }
        Element welcome = new Element("welcome");
        welcome.addContent(new Element("channel").setText(welcomeChannel.getID()));

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("welcome") != null) {
                if (root.getChild("announce").getChild("welcome").getChild("channel") != null) {
                    root.getChild("announce").getChild("welcome").getChild("channel").setText(welcomeChannel.getID());
                } else {
                    root.getChild("announce").getChild("welcome").addContent(new Element("channel").setText(welcomeChannel.getID()));
                }
            } else {
                root.getChild("announce").addContent(welcome);
            }
        } else {
            root.addContent(new Element("announce").addContent(welcome));
        }
        EasyMessage.send(channel, "Alright, I will now welcome people who join this server in " + welcomeChannel.mention() + ". :thumbsup:");
        if (root.getChild("announce").getChild("welcome").getChild("message") == null) {
            EasyMessage.send(channel, "However, I still need the welcome message, how else am I supposed to welcome people if I don't know what to tell them?");
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setWelcomeMessage(String serverID, IChannel channel, String message) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }
        Element welcome = new Element("welcome");

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("welcome") != null) {
                if (root.getChild("announce").getChild("welcome").getChild("message") != null) {
                    root.getChild("announce").getChild("welcome").getChild("message").setText(message);
                } else {
                    root.getChild("announce").getChild("welcome").addContent(new Element("message").setText(message));
                }
            } else {
                welcome.addContent(new Element("message").setText(message));
                root.getChild("announce").addContent(welcome);
            }
        } else {
            welcome.addContent(new Element("message").setText(message));
            root.addContent(new Element("announce").addContent(welcome));
        }
        EasyMessage.send(channel, "Successfully set welcome message to: \n\"" + message.replace("{mention}", "@Example User").replace("{name}", "Example User") + "\"");
        if (root.getChild("announce").getChild("welcome").getChild("channel") == null) {
            EasyMessage.send(channel, "However, you still need to tell me which channel I'll be welcoming people in!");
        }


        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteWelcomeChannel(String serverID) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("welcome") != null) {
                if (root.getChild("announce").getChild("welcome").getChild("channel") != null) {
                    root.getChild("announce").getChild("welcome").removeChild("channel");
                }
            }
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteWelcomeMessage(String serverID) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("welcome") != null) {
                if (root.getChild("announce").getChild("welcome").getChild("message") != null) {
                    root.getChild("announce").getChild("welcome").removeChild("message");
                }
            }
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reviewWelcomeSettings(String serverID, IChannel channel) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("welcome") != null) {
                if (root.getChild("announce").getChild("welcome").getChild("channel") != null && root.getChild("announce").getChild("welcome").getChild("message") != null) {
                    EasyMessage.send(channel, "I am currently welcoming people in " + KekBot.client.getChannelByID(root.getChild("announce").getChild("welcome").getChild("channel").getText()).mention() + ", and the welcome message is: \n\"" + root.getChild("announce").getChild("welcome").getChild("message").getText().replace("{mention}", "@Example User").replace("{name}", "Example User") + "\"");
                } else if (root.getChild("announce").getChild("welcome").getChild("channel") == null && root.getChild("announce").getChild("welcome").getChild("message") != null) {
                    EasyMessage.send(channel, "I currently don't have a channel to welcome people in. But, your welcome message is at least set. \n\"" + root.getChild("announce").getChild("welcome").getChild("message").getText().replace("{mention}", "@Example User").replace("{name}", "Example User") + "\"");
                } else if (root.getChild("announce").getChild("welcome").getChild("channel") != null && root.getChild("announce").getChild("welcome").getChild("message") == null) {
                    EasyMessage.send(channel, "I would be welcoming people in " + KekBot.client.getChannelByID(root.getChild("announce").getChild("welcome").getChild("channel").getText()).mention() + ". But you haven't given me a welcome message to use, yet!");
                } else {
                    EasyMessage.send(channel, "You don't seem to have any welcome settings!");
                }
            } else {
                EasyMessage.send(channel, "You don't seem to have any welcome settings!");
            }
        } else {
            EasyMessage.send(channel, "You don't seem to have any welcome settings!");
        }
    }

    public static void setGoodbyeMessage(String serverID, IChannel channel, String message) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }
        Element goodbye = new Element("goodbye");

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("goodbye") != null) {
                if (root.getChild("announce").getChild("goodbye").getChild("message") != null) {
                    root.getChild("announce").getChild("goodbye").getChild("message").setText(message);
                } else {
                    root.getChild("announce").getChild("goodbye").addContent(new Element("message").setText(message));
                }
            } else {
                goodbye.addContent(new Element("message").setText(message));
                root.getChild("announce").addContent(goodbye);
            }
        } else {
            goodbye.addContent(new Element("channel").setText(message));
            root.addContent(new Element("announce").addContent(goodbye));
        }

        EasyMessage.send(channel, "Successfully set goodbye message to: \n\"" + message.replace("{mention}", "@Example User").replace("{name}", "Example User") + "\"");
        if (root.getChild("announce").getChild("goodbye").getChild("channel") == null) {
            EasyMessage.send(channel, "However, you still need to tell me which channel to announce people leaving!");
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteGoodbyeMessage(String serverID) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("goodbye") != null) {
                if (root.getChild("announce").getChild("goodbye").getChild("message") != null) {
                    root.getChild("announce").getChild("goodbye").removeChild("message");
                }
            }
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setGoodbyeChannel(String serverID, IChannel channel, IChannel farewellChannel) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }
        Element goodbye = new Element("goodbye");

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("goodbye") != null) {
                if (root.getChild("announce").getChild("goodbye").getChild("channel") != null) {
                    root.getChild("announce").getChild("goodbye").getChild("channel").setText(farewellChannel.getID());
                } else {
                    root.getChild("announce").getChild("goodbye").addContent(new Element("channel").setText(farewellChannel.getID()));
                }
            } else {
                goodbye.addContent(new Element("channel").setText(farewellChannel.getID()));
                root.getChild("announce").addContent(goodbye);
            }
        } else {
            goodbye.addContent(new Element("channel").setText(farewellChannel.getID()));
            root.addContent(new Element("announce").addContent(goodbye));
        }
        EasyMessage.send(channel, "Alright, I will let everyone know when someone leaves in " + farewellChannel.mention() + ". :thumbsup:");
        if (root.getChild("announce").getChild("goodbye").getChild("message") == null) {
            EasyMessage.send(channel, "However, you still need to tell me which channel to announce people leaving!");
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteGoodbyeChannel(String serverID) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("goodbye") != null) {
                if (root.getChild("announce").getChild("goodbye").getChild("channel") != null) {
                    root.getChild("announce").getChild("goodbye").removeChild("channel");
                }
            }
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reviewFarewellSettings(String serverID, IChannel channel) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("goodbye") != null) {
                if (root.getChild("announce").getChild("goodbye").getChild("channel") != null && root.getChild("announce").getChild("goodbye").getChild("message") != null) {
                    EasyMessage.send(channel, "I am currently letting everyone know when people leave in " + KekBot.client.getChannelByID(root.getChild("announce").getChild("goodbye").getChild("channel").getText()).mention() + ", and your custom message is: \n\"" + root.getChild("announce").getChild("goodbye").getChild("message").getText().replace("{mention}", "@Example User").replace("{name}", "Example User") + "\"");
                } else if (root.getChild("announce").getChild("goodbye").getChild("channel") == null && root.getChild("announce").getChild("goodbye").getChild("message") != null) {
                    EasyMessage.send(channel, "I currently don't have a channel let everyone know people left in. But, your custom message is at least set. \n\"" + root.getChild("announce").getChild("goodbye").getChild("message").getText().replace("{mention}", "@Example User").replace("{name}", "Example User") + "\"");
                } else if (root.getChild("announce").getChild("goodbye").getChild("channel") != null && root.getChild("announce").getChild("goodbye").getChild("message") == null) {
                    EasyMessage.send(channel, "I am currently letting everyone know when people leave in " + KekBot.client.getChannelByID(root.getChild("announce").getChild("goodbye").getChild("channel").getText()).mention() + ", and you have no custom message. So I will be using the default message.");
                } else {
                    EasyMessage.send(channel, "You don't seem to have any farewell settings!");
                }
            } else {
                EasyMessage.send(channel, "You don't seem to have any farewell settings!");
            }
        } else {
            EasyMessage.send(channel, "You don't seem to have any farewell settings!");
        }
    }

    public static void setBroadcastsChannel(String serverID, String channelID) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }
        Element broadcasts = new Element("broadcasts");

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("broadcasts") != null) {
                if (root.getChild("announce").getChild("broadcasts").getChild("channel") != null) {
                    root.getChild("announce").getChild("broadcasts").getChild("channel").setText(channelID);
                } else {
                    root.getChild("announce").getChild("broadcasts").addContent(new Element("channel").setText(channelID));
                }
            } else {
                broadcasts.addContent(new Element("channel").setText(channelID));
                root.getChild("announce").addContent(broadcasts);
            }
        } else {
            broadcasts.addContent(new Element("channel").setText(channelID));
            root.addContent(new Element("announce").addContent(broadcasts));
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteBroadcastsChannel(String serverID) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("broadcasts") != null) {
                if (root.getChild("announce").getChild("broadcasts").getChild("channel") != null) {
                    root.getChild("announce").getChild("broadcasts").removeChild("channel");
                }
            }
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void enableBroadcasts(String serverID, IChannel channel) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("broadcasts") != null) {
                if (root.getChild("announce").getChild("broadcasts").getChild("status") != null) {
                    if (!root.getChild("announce").getChild("broadcasts").getChild("status").getText().equals("enabled")) {
                        root.getChild("announce").getChild("broadcasts").getChild("status").setText("enabled");
                        EasyMessage.send(channel, "Broadcasts are now enabled. :thumbsup:");
                    } else {
                        EasyMessage.send(channel, "Broadcasts are already enabled!");
                    }
                } else {
                    EasyMessage.send(channel, "Broadcasts are already enabled!");
                }
            } else {
                EasyMessage.send(channel, "Broadcasts are already enabled!");
            }
        } else {
            EasyMessage.send(channel, "Broadcasts are already enabled!");
        }
        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void disableBroadcasts(String serverID, IChannel channel) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }
        Element broadcasts = new Element("broadcasts");
        broadcasts.addContent(new Element("status").setText("disabled"));

        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("broadcasts") != null) {
                if (root.getChild("announce").getChild("broadcasts").getChild("status") != null) {
                    if (!root.getChild("announce").getChild("broadcasts").getChild("status").getText().equals("disabled")) {
                        root.getChild("announce").getChild("broadcasts").getChild("status").setText("disabled");
                        EasyMessage.send(channel, "Broadcasts are now disabled. :thumbsup:");
                    } else {
                        EasyMessage.send(channel, "Broadcasts are already disabled!");
                    }
                } else {
                    root.getChild("announce").getChild("broadcasts").addContent(new Element("status").setText("disabled"));
                    EasyMessage.send(channel, "Broadcasts are now disabled. :thumbsup:");
                }
            } else {
                root.getChild("announce").addContent(broadcasts);
                EasyMessage.send(channel, "Broadcasts are now disabled. :thumbsup:");
            }
        } else {
            root.addContent(new Element("announce").addContent(broadcasts));
            EasyMessage.send(channel, "Broadcasts are now disabled. :thumbsup:");
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + serverID + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reviewBroadcastSettings(String serverID, IChannel channel) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + serverID + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }


        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("broadcasts") != null) {
                if (root.getChild("announce").getChild("broadcasts").getChild("status") != null) {
                    if (root.getChild("announce").getChild("broadcasts").getChild("status").getText().equals("enabled")) {
                        if (root.getChild("announce").getChild("broadcasts").getChild("channel") == null) {
                            EasyMessage.send(channel, "Broadcasts are currently **enabled** and are set to send in the first channel I find.");
                        } else {
                            EasyMessage.send(channel, "Broadcasts are currently **enabled** and are set to send in " + KekBot.client.getChannelByID(root.getChild("announce").getChild("broadcasts").getChild("channel").getText()).mention() + ".");
                        }
                    } else {
                        EasyMessage.send(channel, "Broadcasts are currently **disabled**.");
                    }
                } else {
                    if (root.getChild("announce").getChild("broadcasts").getChild("channel") == null) {
                        EasyMessage.send(channel, "Broadcasts are currently **enabled** and are set to send in the first channel I find.");
                    } else {
                        EasyMessage.send(channel, "Broadcasts are currently **enabled** and are set to send in " + KekBot.client.getChannelByID(root.getChild("announce").getChild("broadcasts").getChild("channel").getText()).mention() + ".");
                    }
                }
            } else {
                EasyMessage.send(channel, "Broadcasts are currently **enabled** and are set to send in the first channel I find.");
            }
        } else {
            EasyMessage.send(channel, "Broadcasts are currently **enabled** and are set to send in the first channel I find.");
        }
    }

    public static void broadcast(IGuild server, String message) throws JDOMException, IOException {

        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + server.getID() + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }
        if (root.getChild("announce") != null) {
            if (root.getChild("announce").getChild("broadcasts") != null) {
                if (root.getChild("announce").getChild("broadcasts").getChild("status") != null) {
                    if (root.getChild("announce").getChild("broadcasts").getChild("status").getText().equals("enabled")) {
                        if (root.getChild("announce").getChild("broadcasts").getChild("channel") == null) {
                            EasyMessage.send(server.getChannels().get(0), "**BROADCAST:** " + message);
                        } else {
                            try {
                                EasyMessage.send(KekBot.client.getChannelByID(root.getChild("announce").getChild("broadcasts").getChild("channel").getText()), "**BROADCAST:** " + message);
                            } catch (RuntimeException e) {
                                EasyMessage.send(server.getChannels().get(0), "**WARNING: Specified channel for announcements no longer exists! Reverting back to default!**");
                                EasyMessage.send(server.getChannels().get(0), "**BROADCAST:** " + message);
                                root.getChild("announce").getChild("broadcasts").getChild("channel").detach();
                            }
                        }
                    }
                } else {
                    if (root.getChild("announce").getChild("broadcasts").getChild("channel") == null) {
                        EasyMessage.send(server.getChannels().get(0), "**BROADCAST:** " + message);
                    } else {
                        try {
                            EasyMessage.send(KekBot.client.getChannelByID(root.getChild("announce").getChild("broadcasts").getChild("channel").getText()), "**BROADCAST:** " + message);
                        } catch (RuntimeException e) {
                            EasyMessage.send(server.getChannels().get(0), "**WARNING: Specified channel for announcements no longer exists! Reverting back to default!**");
                            EasyMessage.send(server.getChannels().get(0), "**BROADCAST:** " + message);
                            root.getChild("announce").getChild("broadcasts").getChild("channel").detach();
                        }
                    }
                }
            } else {
                EasyMessage.send(server.getChannels().get(0), "**BROADCAST:** " + message);
            }
        } else {
            EasyMessage.send(server.getChannels().get(0), "**BROADCAST:** " + message);
        }

        document.setContent(root);
    }

    public static void addTicket(String userID, String title, String contents, IGuild server) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("tickets.xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("tickets");
        }

        Element ticket = new Element("Ticket");
        ticket.addContent(new Element("Status").setText("Open"));
        ticket.addContent(new Element("Title").setText(title));
        ticket.addContent(new Element("Author").setText(userID));
        ticket.addContent(new Element("Server").setText(server.getID()));
        ticket.addContent(new Element("Contents").setText(contents));

        root.addContent(ticket);

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("tickets.xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void listTickets(IChannel channel, String pageNumber) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("tickets.xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("tickets");
        }
        int tickets = root.getChildren().size();
        List<String> ticketsList = new ArrayList<>();
        List<String> pages = new ArrayList<>();

        if (tickets != 0) {
            for (int i = 0; i < tickets; i++) {
                Element ticket = root.getChildren().get(i);
                String ticketTitle = ticket.getChild("Title").getText();
                ticketsList.add(String.valueOf(i+1) + ". \"" + (ticketTitle.length() >= 24 ? ticketTitle.substring(0, 25) + "..." : ticketTitle) + "\"" + StringUtils.repeat(" ", 30-(ticketTitle.length() >= 20 ? 28 : ticketTitle.length())) + "<" + ticket.getChild("Status").getText() + ">");
            }
            try {
                if (pageNumber == null || Integer.valueOf(pageNumber) == 1) {
                    if (ticketsList.size() <= 10) {
                        EasyMessage.send(channel, "```md\n" + StringUtils.join(ticketsList, "\n") + "```");
                    } else {
                        for (int i = 0; i < ticketsList.size(); i += 10) {
                            try {
                                pages.add(StringUtils.join(ticketsList.subList(i, i + 10), "\n"));
                            } catch (IndexOutOfBoundsException e) {
                                pages.add(StringUtils.join(ticketsList.subList(i, ticketsList.size()), "\n"));
                            }
                        }
                        EasyMessage.send(channel, "```md\n" + pages.get(0) + "\n\n[Page](1" + "/" + pages.size() + ")" + "```");
                    }
                } else {
                    if (ticketsList.size() <= 10) {
                        EasyMessage.send(channel, "There are no other pages!");
                    } else {
                        for (int i = 0; i < ticketsList.size(); i += 10) {
                            try {
                                pages.add(StringUtils.join(ticketsList.subList(i, i + 10), "\n"));
                            } catch (IndexOutOfBoundsException e) {
                                pages.add(StringUtils.join(ticketsList.subList(i, ticketsList.size()), "\n"));
                            }
                        }
                        if (Integer.valueOf(pageNumber) > pages.size()) {
                            EasyMessage.send(channel, "Specified page does not exist!");
                        } else {
                            EasyMessage.send(channel, "```md\n" + pages.get(Integer.valueOf(pageNumber) - 1) + "\n\n[Page](" + pageNumber + "/" + pages.size() + ")" + "```");
                        }
                    }
                }
            } catch (NumberFormatException e) {
                EasyMessage.send(channel, "\"" + pageNumber + "\" is not a number!");
            }
        } else {
            EasyMessage.send(channel, "There are no tickets to list!");
        }
    }

    public static int numberOfTickets() {
        try {
            Document document = null;
            Element root = null;

            File xmlFile = new File("tickets.xml");
            if (xmlFile.exists()) {
                FileInputStream fis = new FileInputStream(xmlFile);
                SAXBuilder sb = new SAXBuilder();
                document = sb.build(fis);
                root = document.getRootElement();
                fis.close();
            } else {
                document = new Document();
                root = new Element("tickets");
            }
            return root.getChildren().size();
        } catch (JDOMException | IOException e) {
            return 0;
        }
    }

    public static void viewTicket(IChannel channel, String ticketNumber) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("tickets.xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            document = new Document();
            root = new Element("tickets");
        }



        if (root.getChildren().size() == 0) {
            EasyMessage.send(channel, "You don't have any tickets to view!");
        } else {
            try {
                if (Integer.valueOf(ticketNumber) <= root.getChildren().size()) {
                    int ticketNumberInt = Integer.valueOf(ticketNumber)-1;
                    String ticketTitle = root.getChildren().get(ticketNumberInt).getChild("Title").getText();
                    String ticketContents = root.getChildren().get(ticketNumberInt).getChild("Contents").getText();
                    String ticketAuthor = root.getChildren().get(ticketNumberInt).getChild("Author").getText();
                    String server = root.getChildren().get(ticketNumberInt).getChild("Server").getText();
                    String ticketStatus = root.getChildren().get(ticketNumberInt).getChild("Status").getText();

                    EasyMessage.send(channel, "Title: **" + ticketTitle + "**\nStatus: **" + ticketStatus + "**\nAuthor: **" + KekBot.client.getUserByID(ticketAuthor).getName() + "** (ID: **" + ticketAuthor + "**)\nServer: **" + KekBot.client.getGuildByID(server).getName() + "** (ID: **" + server + "**)\n\nContents: \n" + ticketContents);
                }
            } catch (NumberFormatException e) {
                EasyMessage.send(channel, "\"" + ticketNumber + "\" is not a valid number!");
            }

        }
    }

    public static void deleteTicket(IChannel channel, String ticketNumber) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("tickets.xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("tickets");
        }
        int tickets = root.getChildren().size();

        try {
            if (Integer.valueOf(ticketNumber) <= tickets) {
                EasyMessage.send(channel, "Ticket Closed.");
                EasyMessage.send(KekBot.client.getUserByID(root.getChildren().get(Integer.valueOf(ticketNumber)-1).getChild("Author").getText()), "Your ticket (**" + root.getChildren().get(Integer.valueOf(ticketNumber)-1).getChild("Title").getText() + "**) has been closed.");
                root.getChildren().get(Integer.valueOf(ticketNumber)-1).detach();
            }
        } catch (NumberFormatException e) {
            EasyMessage.send(channel, "\"" + ticketNumber + "\" is not a valid number!");
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("tickets.xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void replyToTicket(IChannel channel, String ticketNumber, String message, IUser author) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("tickets.xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            document = new Document();
            root = new Element("tickets");
        }

        if (root.getChildren().size() == 0) {
            EasyMessage.send(channel, "You don't have any tickets to view!");
        } else {
            try {
                if (Integer.valueOf(ticketNumber) <= root.getChildren().size()) {
                    EasyMessage.send(KekBot.client.getUserByID(root.getChildren().get(Integer.valueOf(ticketNumber)-1).getChild("Author").getText()), "You have received a reply for your ticket. (**" + root.getChildren().get(Integer.valueOf(ticketNumber)-1).getChild("Title").getText() + "**)\n**" + author.getName() + "**:\n\n" + message);
                    EasyMessage.send(channel, "Reply Sent!");
                    root.getChildren().get(Integer.valueOf(ticketNumber)-1).getChild("Status").setText("Sent Reply");
                }
            } catch (NumberFormatException e) {
                EasyMessage.send(channel, "\"" + ticketNumber + "\" is not a valid number!");
            }
        }
        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("tickets.xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setAutoRole(IGuild server, IRole role) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("settings\\" + server.getID() + ".xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("settings");
        }
        Element autoRole = new Element("auto_role");

        if (root.getChild("auto_role") != null) {
            if (root.getChild("auto_role").getChild("role") != null) {
                root.getChild("auto_role").getChild("role").setText(role.getID());
            }
        } else {
            autoRole.addContent(new Element("role").setText(role.getID()));
            root.addContent(autoRole);
        }


        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("settings\\" + server.getID() + ".xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addAllowedUsers(String userID) throws JDOMException, IOException {
        Document document = null;
        Element root = null;

        File xmlFile = new File("KekBot.xml");
        if (xmlFile.exists()) {
            FileInputStream fis = new FileInputStream(xmlFile);
            SAXBuilder sb = new SAXBuilder();
            document = sb.build(fis);
            root = document.getRootElement();
            fis.close();
        } else {
            // if it does not exist create a new document and new root
            document = new Document();
            root = new Element("config");
        }

        if (root.getChild("allowed_users") == null) {
            root.addContent(new Element("allowed_users").addContent(new Element("user").setText(userID)));
        } else {
            root.getChild("allowed_users").addContent(new Element("user").setText(userID));
        }

        document.setContent(root);
        try {
            FileWriter writer = new FileWriter("KekBot.xml");
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(document, writer);
            writer.close(); // close writer
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getBotOwner() {
        String botOwner = null;
        try {
            Document document = null;
            Element root = null;

            File xmlFile = new File("KekBot.xml");
            if (xmlFile.exists()) {
                FileInputStream fis = new FileInputStream(xmlFile);
                SAXBuilder sb = new SAXBuilder();
                document = sb.build(fis);
                root = document.getRootElement();
                fis.close();
            } else {
                // if it does not exist create a new document and new root
                document = new Document();
                root = new Element("config");
            }
            if (root.getChild("bot_owner") != null) {
                botOwner = root.getChild("bot_owner").getText();
            } else {
                botOwner = null;
            }
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return botOwner;
    }

    public static List<IUser> getAllowedUsers() {
        List<IUser> allowedUsers = new ArrayList<>();
        try {
            Document document = null;
            Element root = null;

            File xmlFile = new File("KekBot.xml");
            if (xmlFile.exists()) {
                FileInputStream fis = new FileInputStream(xmlFile);
                SAXBuilder sb = new SAXBuilder();
                document = sb.build(fis);
                root = document.getRootElement();
                fis.close();
            } else {
                // if it does not exist create a new document and new root
                document = new Document();
                root = new Element("config");
            }

            if (root.getChild("allowed_users") != null) {
                for (int i = 0; i < root.getChild("allowed_users").getChildren().size(); i++) {
                    allowedUsers.add(KekBot.client.getUserByID(root.getChild("allowed_users").getChildren().get(i).getText()));
                }
            }
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return allowedUsers;
    }
}
