package com.godson.kekbot;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.command.UserState;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Channel;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.events.InviteReceivedEvent;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.events.guild.GuildUpdateEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.exceptions.BlockedException;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.hooks.ListenerAdapter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.darichey.discord.api.CommandCategory.TEST;
import static java.lang.System.out;

public class Listener extends ListenerAdapter {

    Date date = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("MM-dd-yyyy");
    Date time = new Date();
    SimpleDateFormat ft2 = new SimpleDateFormat("[HH:mm:ss]: ");
    Date start = new Date();

    @Override
    public void onReady(ReadyEvent event) {
        //Announce Ready
        out.println("KekBot is ready to roll!");
        //Randomize the game the bot is playing.
        Timer gameStatusTimer = new Timer();
        gameStatusTimer.schedule(new GameStatus(), 0, TimeUnit.MINUTES.toMillis(10));
        //Set startup time
        start = Calendar.getInstance().getTime();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        //Logging
        if (!event.isPrivate()) out.println(ft2.format(time) + event.getGuild().getName() + " - #" +
                event.getTextChannel().getName() + " - " + event.getAuthor().getUsername() + ": " + event.getMessage().getContent());
        else out.println("PM " + (event.getAuthor().equals(KekBot.client.getSelfInfo()) ? "From: " : "To: ")
                    + event.getAuthor().getUsername() + ": " + event.getMessage().getContent());
        //Rest of normal stuff

        String serverID = (!event.isPrivate() ? event.getGuild().getId() : null);
        String message = event.getMessage().getContent();
        TextChannel channel = event.getTextChannel();
        Guild server = event.getGuild();
        Document document = null;
        Element root = null;
        String prefix;
        String line = "";

        if (!event.isPrivate()) {
            if (message.equals("<@213151748855037953> reloadPrefixes") && event.getMessage().getAuthor().getId().equals("99405418077364224")) {
                List<Guild> servers = KekBot.client.getGuilds();
                for (int i = 0; i < KekBot.client.getGuilds().size(); i++) {
                    if (XMLUtils.getPrefix(servers.get(i)) != null) {
                        CommandRegistry.getForClient(KekBot.client).setPrefixForGuild(servers.get(i), XMLUtils.getPrefix(servers.get(i)));
                    }
                }
                channel.sendMessage("Succesfully reset prefix for all " + servers.size() + "servers.");
            }
            UserState state = KekBot.states.checkUserState(event.getMessage().getAuthor(), server);
            if (state != null) {
                if (state.equals(UserState.TEST_STATE)) {
                    if (message.equals("test")) {
                        channel.sendMessage("Test Mode confirmed working!");
                        KekBot.states.unsetUserState(event.getMessage().getAuthor(), server);
                    }
                }
            }
        }
        File xml = new File("settings\\" + serverID + ".xml");

        //THE FOLLOWING CHECKS IF MESSAGES RECEIVED ARE NOT PMS.
        if (!event.isPrivate()) {
            if (xml.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(xml);
                    SAXBuilder sb = new SAXBuilder();
                    document = sb.build(fis);
                    root = document.getRootElement();
                    fis.close();
                    if (root.getChild("prefix") != null) {
                        prefix = root.getChild("prefix").getText();
                    } else {
                        prefix = "$";
                    }

                    //command begin

                    if (!event.getAuthor().isBot()) {


                        //----------------ADMIN COMMANDS---------------

                        //Add Role
                        if (message.startsWith(prefix + "addrole")) {
                            String rawSplit[] = message.split(" ", 2);
                                        if (rawSplit.length == 1) {
                                            channel.sendMessage("You haven't specified a role *or* a user! :neutral_face:");
                                        } else {
                                            if (rawSplit.length == 2) {
                                                String parameters[] = rawSplit[1].split("\\u007C", 2);
                                                if (parameters.length == 1) {
                                                    if (event.getMessage().getMentionedUsers().size() == 0) {
                                                        channel.sendMessage("The user you want to specify must be in the form of a mention!");
                                                    } else {
                                                        channel.sendMessage("What rank did you want me to give this user?");
                                                    }
                                                } else {
                                                    if (event.getMessage().getMentionedUsers().size() == 0) {
                                                        channel.sendMessage("The user you want to specify must be in the form of a mention!");
                                                    } else if (event.getMessage().getMentionedUsers().size() == 1) {
                                                        if (server.getRolesByName(parameters[1]).size() == 0) {
                                                            channel.sendMessage("Unable to find any roles by the name of \"" + parameters[1] + "\"!");
                                                        } else {
                                                            if (!event.getGuild().getRolesForUser(event.getMessage().getMentionedUsers().get(0)).contains(server.getRolesByName(parameters[1]).get(0))) {
                                                                try {
                                                                    event.getGuild().getRolesForUser(event.getMessage().getMentionedUsers().get(0)).add(server.getRolesByName(parameters[1]).get(0));
                                                                    channel.sendMessage("Successfully gave `" + event.getMessage().getMentionedUsers().get(0).getUsername() + "` the role `" + parameters[1] + "`!");
                                                                } catch (PermissionException e) {
                                                                    channel.sendMessage("Either the specified user has a higher ranked role in which prevents me from assigning extra roles. Or the role itself is higher ranked than mine.");
                                                                    e.printStackTrace();
                                                                }
                                                            } else {
                                                                channel.sendMessage("This user already has this role!");
                                                            }
                                                        }
                                                    } else {
                                                        if (server.getRolesByName(parameters[1]).size() == 0) {
                                                            channel.sendMessage("Unable to find any roles by the name of \"" + parameters[1] + "\"!");
                                                        } else {
                                                            List<String> users = new ArrayList<>();
                                                            List<String> existing = new ArrayList<>();
                                                            for (int i = 0; i < event.getMessage().getMentionedUsers().size(); i++) {
                                                                if (!event.getGuild().getRolesForUser(event.getMessage().getMentionedUsers().get(i)).contains(server.getRolesByName(parameters[1]).get(0))) {
                                                                        try {
                                                                            event.getGuild().getRolesForUser(event.getMessage().getMentionedUsers().get(i)).add(server.getRolesByName(parameters[1]).get(0));
                                                                            users.add(event.getMessage().getMentionedUsers().get(i).getUsername());
                                                                        } catch (PermissionException e) {
                                                                            if (e.getLocalizedMessage().equals(""))
                                                                            if (i == 0) {
                                                                                channel.sendMessage("The role `" + parameters[1] + "` is higher ranked than mine, I am unable to assign these users that role.");
                                                                            }
                                                                        }
                                                                } else {
                                                                    existing.add(event.getMessage().getMentionedUsers().get(i).getUsername());
                                                                }
                                                            }
                                                            if (users.size() > 0) {
                                                                channel.sendMessage("Successfully gave the specified users: `" + StringUtils.join(users, ", ") + "` the role `" + parameters[1] + "`!");
                                                                if (existing.size() == 1) {
                                                                    channel.sendMessage("However, 1 user (`" + StringUtils.join(existing, ", ") + "`) already have this role.");
                                                                }
                                                                if (existing.size() > 1) {
                                                                    channel.sendMessage("However, " + existing.size() + " users: `" + StringUtils.join(existing, ", ") + "` already have this role.");
                                                                }
                                                            } else {
                                                                if (existing.size() >= 1) {
                                                                    channel.sendMessage("All of the users you have specified already have this role.");
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                        }

                        //----------QUOTES SYSTEM----------
                        if (message.startsWith(prefix + "quote")) {
                            String rawSplit[] = message.split(" ", 3);
                            if (rawSplit[0].equals(prefix + "quote")) {
                                if (rawSplit.length == 1) {
                                    if (new File("quotes\\" + event.getGuild().getId() + ".txt").exists()) {
                                        List<String> quotes = FileUtils.readLines(new File("quotes\\" + event.getGuild().getId() + ".txt"), "utf-8");
                                        if (!quotes.isEmpty()) {
                                            Random random = new Random();
                                            int index = random.nextInt(quotes.size());
                                            channel.sendMessage(quotes.get(index));
                                        } else {
                                            channel.sendMessage("You have no quotes!");
                                        }
                                    } else {
                                        channel.sendMessage("You have no quotes!");
                                    }
                                } else {
                                    switch (rawSplit[1]) {
                                        case "add":
                                            if (channel.checkPermission(KekBot.client.getSelfInfo(), Permission.MESSAGE_WRITE)) {
                                                if (rawSplit.length == 3) {
                                                    if (new File("quotes\\" + event.getGuild().getId() + ".txt").exists()) {
                                                        List<String> quotes = FileUtils.readLines(new File("quotes\\" + event.getGuild().getId() + ".txt"), "utf-8");
                                                        if (!quotes.isEmpty()) {
                                                            if (!quotes.contains(rawSplit[2])) {
                                                                try {
                                                                    FileUtils.writeStringToFile(new File("quotes\\" + event.getGuild().getId() + ".txt"), "\n" + rawSplit[2], "utf-8", true);
                                                                    channel.sendMessage("Successfully added quote! :thumbsup:");
                                                                } catch (IOException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            } else {
                                                                channel.sendMessage("That quote's already in my list!");
                                                            }
                                                        } else {
                                                            try {
                                                                FileUtils.writeStringToFile(new File("quotes\\" + event.getGuild().getId() + ".txt"), rawSplit[2], "utf-8", true);
                                                                channel.sendMessage("Successfully added quote! :thumbsup:");
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    } else {
                                                        try {
                                                            FileUtils.writeStringToFile(new File("quotes\\" + event.getGuild().getId() + ".txt"), rawSplit[2], "utf-8", true);
                                                            channel.sendMessage("Successfully added quote! :thumbsup:");
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                } else {
                                                    channel.sendMessage(event.getAuthor().getAsMention() + " :anger: You haven't specified a quote to add!");
                                                }
                                            }
                                            break;
                                        case "remove":
                                            if (channel.checkPermission(KekBot.client.getSelfInfo(), Permission.MESSAGE_WRITE)) {
                                                if (rawSplit.length == 3) {
                                                    try {
                                                        File inputFile = new File("quotes\\" + event.getGuild().getId() + ".txt");
                                                        File tempFile = new File("quotes\\" + event.getGuild().getId() + ".temp.txt");

                                                        if (inputFile.exists()) {
                                                            List<String> quotes = FileUtils.readLines(new File("quotes\\" + event.getGuild().getId() + ".txt"), "utf-8");
                                                            if (!quotes.isEmpty()) {
                                                                if (quotes.contains(rawSplit[2])) {
                                                                    BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                                                                    BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                                                                    String currentLine;

                                                                    while ((currentLine = reader.readLine()) != null) {
                                                                        if (null != currentLine && !currentLine.equalsIgnoreCase(rawSplit[2])) {
                                                                            writer.write(currentLine + System.getProperty("line.separator"));
                                                                        }
                                                                    }
                                                                    writer.close();
                                                                    reader.close();

                                                                    // Delete the original file
                                                                    if (!inputFile.delete()) {
                                                                        out.println("Could not delete file");
                                                                        return;
                                                                    }
                                                                    tempFile.renameTo(inputFile);
                                                                    channel.sendMessage("Successfully removed __**" + rawSplit[2] + "**__ from this server's list of quotes.");
                                                                } else {
                                                                    channel.sendMessage("That quote does not appear on my list!");
                                                                }
                                                            } else {
                                                                channel.sendMessage("You have no quotes to remove!");
                                                            }
                                                        } else {
                                                            channel.sendMessage("You have no quotes to remove!");
                                                        }
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    channel.sendMessage(event.getMessage().getAuthor().getAsMention() + " :anger: You haven't specified a quote to remove!");
                                                }
                                            }
                                            break;
                                        case "list":
                                            File ff = new File("quotes\\" + event.getGuild().getId() + ".txt");
                                            File ff2 = new File("quotes\\quotes.txt");
                                            if (ff.exists()) {
                                                    try {
                                                        ff.renameTo(ff2);
                                                        channel.sendFile(ff2, null);
                                                        ff2.renameTo(ff);
                                                        channel.sendMessage("Note: Opening this file on Notepad will make the list look strange.");
                                                    } catch (PermissionException e) {
                                                        out.println("I do not have the 'Send Messages' permission in server: " + event.getGuild().getName() + " - #" + channel.getName() + "! Aborting!");
                                                    }
                                            }
                                            break;
                                    }
                                }
                            }
                        }

                        if (message.equals("<@213151748855037953> prefix") || message.equals("<@!213151748855037953> prefix")) {
                            channel.sendMessage("The prefix for __**" + server.getName() + "**__ is: **" + prefix + "**");
                        }

                        if (message.equals("<@213151748855037953> help") || message.equals("<@!213151748855037953> help")) {
                            List<String> commands = new ArrayList<String>();
                            CommandRegistry registry = CommandRegistry.getForClient(KekBot.client);
                            EnumSet<CommandCategory> categories = EnumSet.allOf(CommandCategory.class);
                            categories.forEach(category -> {
                                commands.add("# " + category.toString());
                                for (int i = 0; i < registry.getCommands().size(); i++) {
                                    Set<String> aliases = registry.getCommands().get(i).getAliases();
                                    if (registry.getCommands().get(i).getCategory() != null) {
                                        if (registry.getCommands().get(i).getCategory().equals(category)) {
                                            commands.add("[$" + registry.getCommands().get(i).getName() +
                                                    (registry.getCommands().get(i).getAliases().size() != 0 ? " | " + StringUtils.join(aliases, " | ") : "") + "](" + registry.getCommands().get(i).getDescription() + ")");
                                        }
                                    }
                                }
                                commands.add("");
                            });
                            event.getAuthor().getPrivateChannel().sendMessage("__**KekBot**__\n*Your helpful meme-based bot!*\n" +
                                    "```md\n# KekBot's default prefix for commands is \"$\". However, the server you're on might have it use a different prefix. If you're unsure, feel free to go a server and say \"@KekBot prefix\"" +
                                    "\n# To add me to your server, send me an invite link!\n\n" + StringUtils.join(commands, "\n") + "```");
                            channel.sendMessage(event.getMessage().getAuthor().getAsMention() + " Alright, check your PMs! :thumbsup:");
                        }

                        /*if (message.equals(prefix + "killvoice")) {
                            Optional<VoiceChannel> voiceChannel = KekBot.client.getConnectedVoiceChannels().stream().filter(c -> c.getGuild().equals(server)).findFirst();
                            if (voiceChannel.isPresent()) {
                                voiceChannel.get().leave();
                                AudioPlayer.getAudioPlayerForGuild(server).clean();
                            }
                        }*/



                    }
                    //command end


                } catch (IOException | JDOMException e) {
                    e.printStackTrace();
                }
            }


            //THE FOLLOWING CHECKS IF MESSAGES RECEIVED ARE PMS.
        } else {
            if (event.getMessage().getAuthor().getId().equals(XMLUtils.getBotOwner())) {
                if (message.startsWith("message")) {
                    String rawSplit[] = message.split(" ", 4);
                    if (rawSplit.length == 1) {
                        channel.sendMessage("You can't expect me to send someone a message without telling me who or where I'm sending a message to!");
                    } else if (rawSplit.length >= 2) {
                        switch (rawSplit[1]) {
                            case "user":
                                if (rawSplit.length >= 3) {
                                    if (rawSplit.length == 4) {
                                        try {
                                            KekBot.client.getUserById(rawSplit[2]).getPrivateChannel().sendMessage(rawSplit[3]);
                                            channel.sendMessage("Successfully sent message to: __**" + KekBot.client.getUserById(rawSplit[2]).getUsername() + "**__!");
                                        } catch (BlockedException e) {
                                            channel.sendMessage("It appears that +" + KekBot.client.getUserById(rawSplit[2]).getUsername() + " has either blocked me!");
                                        } catch (NullPointerException e) {
                                            channel.sendMessage("`" + rawSplit[2] + "`" + " is not a valid user ID!");
                                        }
                                    } else {
                                        channel.sendMessage("You can't expect me to send someone a message to this user without telling me what to send them!");
                                    }
                                } else {
                                    channel.sendMessage("You can't expect me to send someone a message without telling me who I'm send sending a message to!");
                                }
                                break;
                            case "channel":
                                if (rawSplit.length >= 3) {
                                    if (rawSplit.length == 4) {
                                            try {
                                                KekBot.client.getTextChannelById(rawSplit[2]).sendMessage(rawSplit[3]);
                                                channel.sendMessage("Successfully sent message to: ``(" + KekBot.client.getTextChannelById(rawSplit[2]).getGuild().getName() + ") #" + KekBot.client.getTextChannelById(rawSplit[2]).getName() + "``!");
                                            } catch (PermissionException e) {
                                                channel.sendMessage("I don't have permissions to say messages in that channel! Aborting!");
                                            } catch (RuntimeException e) {
                                                channel.sendMessage("`" + rawSplit[2] + "`" + " is not a valid channel ID or I am not on a server with this channel ID!");
                                            }
                                    } else {
                                        channel.sendMessage("You can't expect me to send someone a message to this channel without telling me what to send it!");
                                    }
                                } else {
                                    channel.sendMessage("You can't expect me to send someone a message without telling me what channel to send a message to!");
                                }
                                break;
                        }
                    }
                }
                if (message.startsWith("tickets") || message.startsWith("ticket")) {
                    String rawSplit[] = message.split(" ", 3);
                    if (rawSplit[0].equals("tickets") || rawSplit[0].equals("ticket")) {
                        if (rawSplit.length == 1) {
                            channel.sendMessage("You have **" + XMLUtils.numberOfTickets() + (XMLUtils.numberOfTickets() == 1 ? "** ticket." : "** tickets."));
                        } else {
                            switch (rawSplit[1]) {
                                case "list":
                                    try {
                                        XMLUtils.listTickets(channel, (rawSplit.length == 3 ? rawSplit[2] : null));
                                    } catch (JDOMException | IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "view":
                                    if (rawSplit.length == 3) {
                                        try {
                                            XMLUtils.viewTicket(channel, rawSplit[2]);
                                        } catch (JDOMException | IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        channel.sendMessage("No ticket specified.");
                                    }
                                    break;
                                case "close":
                                    if (rawSplit.length == 3) {
                                        try {
                                            XMLUtils.deleteTicket(channel, rawSplit[2]);
                                        } catch (JDOMException | IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        channel.sendMessage("No ticket specified.");
                                    }
                                    break;
                                case "reply":
                                    if (rawSplit.length == 3) {
                                        String parameters[] = rawSplit[2].split(" ", 2);
                                        if (parameters.length == 2) {
                                            try {
                                                XMLUtils.replyToTicket(channel, parameters[0], parameters[1], event.getMessage().getAuthor());
                                            } catch (JDOMException | IOException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            channel.sendMessage("No reply message specified.");
                                        }
                                    } else {
                                        channel.sendMessage("No ticket specified.");
                                    }
                            }
                        }
                    }
                }
                //End of Owner-Only PM Based command
            }
            //End of PM Based command
            if (message.startsWith("help")) {
                String rawSplit[] = message.split(" ", 2);
                List<String> commands = new ArrayList<String>();
                List<String> pages = new ArrayList<String>();
                CommandRegistry registry = CommandRegistry.getForClient(KekBot.client);
                EnumSet<CommandCategory> categories = EnumSet.allOf(CommandCategory.class);
                commands.add("# KekBot's default prefix for commands is \"$\". However, the server you're on might have it use a different prefix. If you're unsure, feel free to go a server and say \"@KekBot prefix\"");
                commands.add("# To add me to your server, send me an invite link!\n");
                categories.forEach(category -> {
                    commands.add("# " + category.toString());
                    for (int i = 0; i < registry.getCommands().size(); i++) {
                        Set<String> aliases = registry.getCommands().get(i).getAliases();
                        if (registry.getCommands().get(i).getCategory() != null) {
                            if (registry.getCommands().get(i).getCategory().equals(category)) {
                                commands.add("[$" + registry.getCommands().get(i).getName() +
                                        (registry.getCommands().get(i).getAliases().size() != 0 ? " | " + StringUtils.join(aliases, " | ") : "") + "](" + registry.getCommands().get(i).getDescription() + ")");
                            }
                        }
                    }
                    commands.add("");
                });
                for (int i = 0; i < registry.getCommands().size(); i += 25) {
                    try {
                        pages.add(StringUtils.join(commands.subList(i, i + 25), "\n"));
                    } catch (IndexOutOfBoundsException e) {
                        pages.add(StringUtils.join(commands.subList(i, commands.size()), "\n"));
                    }
                }
                if (rawSplit[0].equals("help")) {
                    if (rawSplit.length == 1) {
                        channel.sendMessage("__**KekBot**__\n*Your helpful meme-based bot!*\n" +
                                "```md\n" + pages.get(0) + "\n\n" + "[Page](1" + "/" + pages.size() + ")\n" +
                                "# Type \"help <number>\" to view that page!" + "```");
                    } else {
                        try {
                            if (Integer.valueOf(rawSplit[1]) > pages.size()) {
                                channel.sendMessage("Specified page does not exist!");
                            } else {
                                channel.sendMessage("__**KekBot**__\n*Your helpful meme-based bot!*\n" +
                                        "```md\n" + pages.get(Integer.valueOf(rawSplit[1]) - 1) + "\n\n[Page](" + rawSplit[1] + "/" + pages.size() + ")\n" +
                                        "# Type \"help <number>\" to view that page!" + "```");
                            }
                        } catch (NumberFormatException e) {
                            channel.sendMessage("\"" + rawSplit[1] + "\" is not a number!");
                        }
                    }
                }
            }

        }
    }

    @Override
    public void onInviteReceived(InviteReceivedEvent event) {
        if (event.isPrivate() && (!event.getAuthor().equals(KekBot.client.getSelfInfo())))
            event.getMessage().getChannel().sendMessage("Thanks for the invite! However, I cannot simply join your server! You must allow me to connect to your server using the following link:" +
                            "\nhttps://discordapp.com/oauth2/authorize?&client_id=213151748855037953&scope=bot&permissions=0x00000008");

    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        out.println(ft2.format(time) + "Joined/Created server: \"" + event.getGuild().getName() + "\" (ID: " + event.getGuild().getId() + ")");
        try {
            String server = event.getGuild().getId();
            Document document = null;
            Element root = null;
            File xmlFile = new File("settings\\" + server + ".xml");
            if (xmlFile.exists()) {
                // try to load document from xml file if it exist
                // create a file input stream
                FileInputStream fis = new FileInputStream(xmlFile);
                // create a sax builder to parse the document
                SAXBuilder sb = new SAXBuilder();
                // parse the xml content provided by the file input stream and create a Document object
                document = sb.build(fis);
                // get the root element of the document
                root = document.getRootElement();
                fis.close();
            } else {
                // if it does not exist create a new document and new root
                document = new Document();
                root = new Element("settings");
                event.getGuild().getTextChannels().get(0).sendMessage("Thanks for inviting me!");
            }

            if (root.getChild("name") != null) {
                if (!root.getChild("name").getText().equals(event.getGuild().getName())) {
                    root.getChild("name").setText(event.getGuild().getName());

                    document.setContent(root);
                    try {
                        FileWriter writer = new FileWriter("settings\\" + server + ".xml");
                        XMLOutputter outputter = new XMLOutputter();
                        outputter.setFormat(Format.getPrettyFormat());
                        outputter.output(document, writer);
                        writer.close(); // close writer
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                root.addContent(new Element("name").setText(event.getGuild().getName()));
                document.setContent(root);
                try {
                    FileWriter writer = new FileWriter("settings\\" + server + ".xml");
                    XMLOutputter outputter = new XMLOutputter();
                    outputter.setFormat(Format.getPrettyFormat());
                    outputter.output(document, writer);
                    writer.close(); // close writer
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException | JDOMException e) {
            e.printStackTrace();
        }

        if (XMLUtils.getPrefix(event.getGuild()) != null) {
            CommandRegistry.getForClient(KekBot.client).setPrefixForGuild(event.getGuild(), XMLUtils.getPrefix(event.getGuild()));
        }

        if (event.getGuild().equals(KekBot.client.getGuildById("221910104495095808"))) {
            CommandRegistry.getForClient(KekBot.client).customRegister(new Command("customTest")
                    .withCategory(TEST)
                    .withDescription("Just a test command.")
                    .withUsage("{p}test")
                    .caseSensitive(true)
                    .onExecuted(context -> {
                        context.getTextChannel().sendMessage("Test Successful! Custom Comands now work!");
                    }), KekBot.client.getGuildById("221910104495095808"));
        }
    }

    /*@EventSubscriber
    public void onTrackFinishEvent(TrackFinishEvent event) {
        if (event.getPlayer().getPlaylistSize() == 0) {
            Optional<IVoiceChannel> voiceChannel = event.getClient().getConnectedVoiceChannels().stream().filter(c -> c.getGuild().equals(event.getPlayer().getGuild())).findFirst();
            if (voiceChannel.isPresent()) {
                voiceChannel.get().leave();
            }
        }
    }*/

    @Override
    public void onGuildUpdate(GuildUpdateEvent event) {
        try {
            String server = event.getGuild().getId();
            Document document = null;
            Element root = null;
            File xmlFile = new File("settings\\" + server + ".xml");
            if (xmlFile.exists()) {
                // try to load document from xml file if it exist
                // create a file input stream
                FileInputStream fis = new FileInputStream(xmlFile);
                // create a sax builder to parse the document
                SAXBuilder sb = new SAXBuilder();
                // parse the xml content provided by the file input stream and create a Document object
                document = sb.build(fis);
                // get the root element of the document
                root = document.getRootElement();
                fis.close();
            } else {
                // if it does not exist create a new document and new root
                document = new Document();
                root = new Element("settings");
            }


            if (!root.getChild("name").getText().equals(event.getGuild().getName())) {
                root.getChild("name").setText(event.getGuild().getName());
                document.setContent(root);
                try {
                    FileWriter writer = new FileWriter("settings\\" + server + ".xml");
                    XMLOutputter outputter = new XMLOutputter();
                    outputter.setFormat(Format.getPrettyFormat());
                    outputter.output(document, writer);
                    writer.close(); // close writer
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException | JDOMException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        out.println(ft2.format(time) + event.getUser().getUsername() + " has joined " + event.getGuild().getName() + ".");
        try {
            String server = event.getGuild().getId();
            Document document = null;
            Element root = null;
            File xmlFile = new File("settings\\" + server + ".xml");
            if (xmlFile.exists()) {
                // try to load document from xml file if it exist
                // create a file input stream
                FileInputStream fis = new FileInputStream(xmlFile);
                // create a sax builder to parse the document
                SAXBuilder sb = new SAXBuilder();
                // parse the xml content provided by the file input stream and create a Document object
                document = sb.build(fis);
                // get the root element of the document
                root = document.getRootElement();
                fis.close();
            } else {
                // if it does not exist create a new document and new root
                root = new Element("settings");
            }


            if (root.getChild("announce") != null) {
                if (root.getChild("announce").getChild("welcome") != null) {
                    if (root.getChild("announce").getChild("welcome").getChild("channel") != null && root.getChild("announce").getChild("welcome").getChild("message") != null) {
                        String channelID = root.getChild("announce").getChild("welcome").getChild("channel").getText();
                        String message = root.getChild("announce").getChild("welcome").getChild("message").getText().replace("{mention}", event.getUser().getAsMention()).replace("{name}", event.getUser().getUsername());
                        KekBot.client.getTextChannelById(channelID).sendMessage(message);
                    }
                }
            }

            if (root.getChild("auto_role") != null) {
                if (root.getChild("auto_role").getChild("role") != null) {
                        try {
                            event.getGuild().getRolesForUser(event.getUser()).add(event.getGuild().getRoleById(root.getChild("auto_role").getChild("role").getText()));
                        } catch (PermissionException e) {
                            event.getGuild().getTextChannels().get(0).sendMessage("Unable to automatically set role due to not having the **Manage Roles** permission.");
                        } catch (NullPointerException e) {
                            event.getGuild().getTextChannels().get(0).sendMessage("I can no longer find the rank in which I was to automatically assign!");
                        }
                }
            }

        } catch (IOException | JDOMException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        out.println(ft2.format(time) + event.getUser().getUsername() + " has left " + event.getGuild().getName() + ".");
        try {
            String server = event.getGuild().getId();
            Document document = null;
            Element root = null;
            File xmlFile = new File("settings\\" + server + ".xml");
            if (xmlFile.exists()) {
                // try to load document from xml file if it exist
                // create a file input stream
                FileInputStream fis = new FileInputStream(xmlFile);
                // create a sax builder to parse the document
                SAXBuilder sb = new SAXBuilder();
                // parse the xml content provided by the file input stream and create a Document object
                document = sb.build(fis);
                // get the root element of the document
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
                        if (root.getChild("announce").getChild("goodbye").getChild("message") != null) {
                            String channelID = root.getChild("announce").getChild("goodbye").getChild("channel").getText();
                            String message = root.getChild("announce").getChild("goodbye").getChild("message").getText().replace("{mention}", event.getUser().getAsMention()).replace("{name}", event.getUser().getUsername());
                            KekBot.client.getTextChannelById(channelID).sendMessage(message);
                        } else {
                            String channelID = root.getChild("announce").getChild("goodbye").getChild("channel").getText();
                            String message = event.getUser().getUsername() + " has left the server!";
                            KekBot.client.getTextChannelById(channelID).sendMessage(message);
                        }
                    }
                }
            }

        } catch (IOException | JDOMException e) {
            e.printStackTrace();
        }
    }
}
