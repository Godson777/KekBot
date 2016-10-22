package com.godson.kekbot;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.command.UserState;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.*;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.events.TrackFinishEvent;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.darichey.discord.api.CommandCategory.TEST;
import static java.lang.System.out;

public class Listener {

    Date date = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("MM-dd-yyyy");
    Date time = new Date();
    SimpleDateFormat ft2 = new SimpleDateFormat("[HH:mm:ss]: ");
    Date start = new Date();

    @EventSubscriber
    public void onReady(ReadyEvent event) {
        //Announce Ready
        out.println("KekBot is ready to roll!");
        //Randomize the game the bot is playing.
        Timer timer = new Timer();
        timer.schedule(new GameStatus(), 0, TimeUnit.MINUTES.toMillis(10));
        //Set startup time
        start = Calendar.getInstance().getTime();
    }

    @EventSubscriber
    public void logMessage(MessageReceivedEvent event) {
        if (!event.getMessage().getChannel().isPrivate()) {
            out.println(ft2.format(time) + event.getMessage().getGuild().getName() + " - #" + event.getMessage().getChannel().getName() + " - " + event.getMessage().getAuthor().getName() + ": " + event.getMessage().getContent());
        } else {
            out.println("PM From: " + event.getMessage().getAuthor().getName() + ": " + event.getMessage().getContent());
        }
    }

    @EventSubscriber
    public void onMessageEvent(MessageReceivedEvent event) {
        String serverID = (!event.getMessage().getChannel().isPrivate() ? event.getMessage().getGuild().getID() : null);
        String message = event.getMessage().getContent();
        IChannel channel = event.getMessage().getChannel();
        IGuild server = event.getMessage().getGuild();
        Document document = null;
        Element root = null;
        String prefix;
        String line = "";

        if (!channel.isPrivate()) {
            if (message.equals("<@213151748855037953> reloadPrefixes") && event.getMessage().getAuthor().getID().equals("99405418077364224")) {
                List<IGuild> servers = KekBot.client.getGuilds();
                for (int i = 0; i < KekBot.client.getGuilds().size(); i++) {
                    if (XMLUtils.getPrefix(servers.get(i)) != null) {
                        CommandRegistry.getForClient(KekBot.client).setPrefixForGuild(servers.get(i), XMLUtils.getPrefix(servers.get(i)));
                    }
                }
                EasyMessage.send(channel, "Succesfully reset prefix for all " + servers.size() + "servers.");
            }
            UserState state = KekBot.states.checkUserState(event.getMessage().getAuthor(), server);
            if (state != null) {
                if (state.equals(UserState.TEST_STATE)) {
                    if (message.equals("test")) {
                        EasyMessage.send(channel, "Test Mode confirmed working!");
                        KekBot.states.unsetUserState(event.getMessage().getAuthor(), server);
                    }
                }
            }
        }
        File xml = new File("settings\\" + serverID + ".xml");

        //THE FOLLOWING CHECKS IF MESSAGES RECEIVED ARE NOT PMS.
        if (!channel.isPrivate()) {
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

                    if (!event.getMessage().getAuthor().isBot()) {


                        //----------------ADMIN COMMANDS---------------

                        //Add Role
                        if (message.startsWith(prefix + "addrole")) {
                            String rawSplit[] = message.split(" ", 2);
                                        if (rawSplit.length == 1) {
                                            EasyMessage.send(channel, "You haven't specified a role *or* a user! :neutral_face:");
                                        } else {
                                            if (rawSplit.length == 2) {
                                                String parameters[] = rawSplit[1].split("\\u007C", 2);
                                                if (parameters.length == 1) {
                                                    if (event.getMessage().getMentions().size() == 0) {
                                                        EasyMessage.send(channel, "The user you want to specify must be in the form of a mention!");
                                                    } else {
                                                        EasyMessage.send(channel, "What rank did you want me to give this user?");
                                                    }
                                                } else {
                                                    if (event.getMessage().getMentions().size() == 0) {
                                                        EasyMessage.send(channel, "The user you want to specify must be in the form of a mention!");
                                                    } else if (event.getMessage().getMentions().size() == 1) {
                                                        if (server.getRolesByName(parameters[1]).size() == 0) {
                                                            EasyMessage.send(channel, "Unable to find any roles by the name of \"" + parameters[1] + "\"!");
                                                        } else {
                                                            if (!event.getMessage().getMentions().get(0).getRolesForGuild(server).contains(server.getRolesByName(parameters[1]).get(0))) {
                                                                RequestBuffer.request(() -> {
                                                                    try {
                                                                        event.getMessage().getMentions().get(0).addRole(server.getRolesByName(parameters[1]).get(0));
                                                                        EasyMessage.send(channel, "Successfully gave `" + event.getMessage().getMentions().get(0).getName() + "` the role `" + parameters[1] + "`!");
                                                                    } catch (DiscordException | RateLimitException e) {
                                                                        e.printStackTrace();
                                                                    } catch (MissingPermissionsException e) {
                                                                        EasyMessage.send(channel, "Either the specified user has a higher ranked role in which prevents me from assigning extra roles. Or the role itself is higher ranked than mine.");
                                                                        e.printStackTrace();
                                                                    }
                                                                });
                                                            } else {
                                                                EasyMessage.send(channel, "This user already has this role!");
                                                            }
                                                        }
                                                    } else {
                                                        if (server.getRolesByName(parameters[1]).size() == 0) {
                                                            EasyMessage.send(channel, "Unable to find any roles by the name of \"" + parameters[1] + "\"!");
                                                        } else {
                                                            List<String> users = new ArrayList<>();
                                                            List<String> existing = new ArrayList<>();
                                                            for (int i = 0; i < event.getMessage().getMentions().size(); i++) {
                                                                int finalI = i;
                                                                if (!event.getMessage().getMentions().get(i).getRolesForGuild(server).contains(server.getRolesByName(parameters[1]).get(0))) {
                                                                    RequestBuffer.request(() -> {
                                                                        try {
                                                                            event.getMessage().getMentions().get(finalI).addRole(server.getRolesByName(parameters[1]).get(0));
                                                                            users.add(event.getMessage().getMentions().get(finalI).getName());
                                                                        } catch (DiscordException | RateLimitException e) {
                                                                            e.printStackTrace();
                                                                        } catch (MissingPermissionsException e) {
                                                                            if (e.getLocalizedMessage().equals(""))
                                                                            if (finalI == 0) {
                                                                                EasyMessage.send(channel, "The role `" + parameters[1] + "` is higher ranked than mine, I am unable to assign these users that role.");
                                                                            }
                                                                        }
                                                                    });
                                                                } else {
                                                                    existing.add(event.getMessage().getMentions().get(i).getName());
                                                                }
                                                            }
                                                            if (users.size() > 0) {
                                                                EasyMessage.send(channel, "Successfully gave the specified users: `" + StringUtils.join(users, ", ") + "` the role `" + parameters[1] + "`!");
                                                                if (existing.size() == 1) {
                                                                    EasyMessage.send(channel, "However, 1 user (`" + StringUtils.join(existing, ", ") + "`) already have this role.");
                                                                }
                                                                if (existing.size() > 1) {
                                                                    EasyMessage.send(channel, "However, " + existing.size() + " users: `" + StringUtils.join(existing, ", ") + "` already have this role.");
                                                                }
                                                            } else {
                                                                if (existing.size() >= 1) {
                                                                    EasyMessage.send(channel, "All of the users you have specified already have this role.");
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
                                    if (new File("quotes\\" + event.getMessage().getGuild().getID() + ".txt").exists()) {
                                        List<String> quotes = FileUtils.readLines(new File("quotes\\" + event.getMessage().getGuild().getID() + ".txt"), "utf-8");
                                        if (!quotes.isEmpty()) {
                                            Random random = new Random();
                                            int index = random.nextInt(quotes.size());
                                            EasyMessage.send(channel, quotes.get(index));
                                        } else {
                                            EasyMessage.send(channel, "You have no quotes!");
                                        }
                                    } else {
                                        EasyMessage.send(channel, "You have no quotes!");
                                    }
                                } else {
                                    switch (rawSplit[1]) {
                                        case "add":
                                            if (channel.getModifiedPermissions(KekBot.client.getOurUser()).contains(Permissions.SEND_MESSAGES)) {
                                                if (rawSplit.length == 3) {
                                                    if (new File("quotes\\" + event.getMessage().getGuild().getID() + ".txt").exists()) {
                                                        List<String> quotes = FileUtils.readLines(new File("quotes\\" + event.getMessage().getGuild().getID() + ".txt"), "utf-8");
                                                        if (!quotes.isEmpty()) {
                                                            if (!quotes.contains(rawSplit[2])) {
                                                                try {
                                                                    FileUtils.writeStringToFile(new File("quotes\\" + event.getMessage().getGuild().getID() + ".txt"), "\n" + rawSplit[2], "utf-8", true);
                                                                    EasyMessage.send(channel, "Successfully added quote! :thumbsup:");
                                                                } catch (IOException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            } else {
                                                                EasyMessage.send(channel, "That quote's already in my list!");
                                                            }
                                                        } else {
                                                            try {
                                                                FileUtils.writeStringToFile(new File("quotes\\" + event.getMessage().getGuild().getID() + ".txt"), rawSplit[2], "utf-8", true);
                                                                EasyMessage.send(channel, "Successfully added quote! :thumbsup:");
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    } else {
                                                        try {
                                                            FileUtils.writeStringToFile(new File("quotes\\" + event.getMessage().getGuild().getID() + ".txt"), rawSplit[2], "utf-8", true);
                                                            EasyMessage.send(channel, "Successfully added quote! :thumbsup:");
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                } else {
                                                    EasyMessage.send(channel, event.getMessage().getAuthor().mention() + " :anger: You haven't specified a quote to add!");
                                                }
                                            }
                                            break;
                                        case "remove":
                                            if (channel.getModifiedPermissions(KekBot.client.getOurUser()).contains(Permissions.SEND_MESSAGES)) {
                                                if (rawSplit.length == 3) {
                                                    try {
                                                        File inputFile = new File("quotes\\" + event.getMessage().getGuild().getID() + ".txt");
                                                        File tempFile = new File("quotes\\" + event.getMessage().getGuild().getID() + ".temp.txt");

                                                        if (inputFile.exists()) {
                                                            List<String> quotes = FileUtils.readLines(new File("quotes\\" + event.getMessage().getGuild().getID() + ".txt"), "utf-8");
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
                                                                    EasyMessage.send(channel, "Successfully removed __**" + rawSplit[2] + "**__ from this server's list of quotes.");
                                                                } else {
                                                                    EasyMessage.send(channel, "That quote does not appear on my list!");
                                                                }
                                                            } else {
                                                                EasyMessage.send(channel, "You have no quotes to remove!");
                                                            }
                                                        } else {
                                                            EasyMessage.send(channel, "You have no quotes to remove!");
                                                        }
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    EasyMessage.send(channel, event.getMessage().getAuthor().mention() + " :anger: You haven't specified a quote to remove!");
                                                }
                                            }
                                            break;
                                        case "list":
                                            File ff = new File("quotes\\" + event.getMessage().getGuild().getID() + ".txt");
                                            File ff2 = new File("quotes\\quotes.txt");
                                            if (ff.exists()) {
                                                RequestBuffer.request(() -> {
                                                    try {
                                                        ff.renameTo(ff2);
                                                        channel.sendFile(ff2);
                                                        ff2.renameTo(ff);
                                                        new MessageBuilder(KekBot.client).withChannel(channel).withContent("Note: Opening this file on Notepad will make the list look strange.").send();
                                                    } catch (DiscordException | IOException e) {
                                                        e.printStackTrace();
                                                    } catch (MissingPermissionsException e) {
                                                        out.println("I do not have the 'Send Messages' permission in server: " + event.getMessage().getGuild().getName() + " - #" + channel.getName() + "! Aborting!");
                                                    }
                                                });
                                            }
                                            break;
                                    }
                                }
                            }
                        }

                        if (message.equals("<@213151748855037953> prefix") || message.equals("<@!213151748855037953> prefix")) {
                            EasyMessage.send(channel, "The prefix for __**" + server.getName() + "**__ is: **" + prefix + "**");
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
                            EasyMessage.send(event.getMessage().getAuthor(), "__**KekBot**__\n*Your helpful meme-based bot!*\n" +
                                    "```md\n# KekBot's default prefix for commands is \"$\". However, the server you're on might have it use a different prefix. If you're unsure, feel free to go a server and say \"@KekBot prefix\"" +
                                    "\n# To add me to your server, send me an invite link!\n\n" + StringUtils.join(commands, "\n") + "```");
                            EasyMessage.send(event.getMessage().getChannel(), event.getMessage().getAuthor().mention() + " Alright, check your PMs! :thumbsup:");
                        }

                        if (message.equals(prefix + "killvoice")) {
                            Optional<IVoiceChannel> voiceChannel = KekBot.client.getConnectedVoiceChannels().stream().filter(c -> c.getGuild().equals(server)).findFirst();
                            if (voiceChannel.isPresent()) {
                                voiceChannel.get().leave();
                                AudioPlayer.getAudioPlayerForGuild(server).clean();
                            }
                        }



                    }
                    //command end


                } catch (IOException | JDOMException e) {
                    e.printStackTrace();
                }
            }


            //THE FOLLOWING CHECKS IF MESSAGES RECEIVED ARE PMS.
        } else {
            if (event.getMessage().getAuthor().getID().equals(XMLUtils.getBotOwner())) {
                if (message.startsWith("message")) {
                    String rawSplit[] = message.split(" ", 4);
                    if (rawSplit.length == 1) {
                        EasyMessage.send(channel, "You can't expect me to send someone a message without telling me who or where I'm sending a message to!");
                    } else if (rawSplit.length >= 2) {
                        switch (rawSplit[1]) {
                            case "user":
                                if (rawSplit.length >= 3) {
                                    if (rawSplit.length == 4) {
                                        RequestBuffer.request(() -> {
                                            try {
                                                new MessageBuilder(KekBot.client).withChannel(KekBot.client.getOrCreatePMChannel(KekBot.client.getUserByID(rawSplit[2]))).withContent(rawSplit[3]).send();
                                                new MessageBuilder(KekBot.client).withChannel(channel).withContent("Successfully sent message to: __**" + KekBot.client.getUserByID(rawSplit[2]).getName() + "**__!").send();
                                            } catch (DiscordException e) {
                                                RequestBuffer.request(() -> {
                                                    try {
                                                        new MessageBuilder(KekBot.client).withChannel(channel).withContent("It appears that +" + KekBot.client.getUserByID(rawSplit[2]).getName() + " has either blocked me!").send();
                                                    } catch (DiscordException | MissingPermissionsException er) {
                                                        er.printStackTrace();
                                                    }
                                                });
                                            } catch (MissingPermissionsException e) {
                                                e.printStackTrace();
                                            } catch (NullPointerException e) {
                                                RequestBuffer.request(() -> {
                                                    try {
                                                        new MessageBuilder(KekBot.client).withChannel(channel).withContent("`" + rawSplit[2] + "`" + " is not a valid user ID!").send();
                                                    } catch (DiscordException | MissingPermissionsException er) {
                                                        er.printStackTrace();
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        EasyMessage.send(channel, "You can't expect me to send someone a message to this user without telling me what to send them!");
                                    }
                                } else {
                                    EasyMessage.send(channel, "You can't expect me to send someone a message without telling me who I'm send sending a message to!");
                                }
                                break;
                            case "channel":
                                if (rawSplit.length >= 3) {
                                    if (rawSplit.length == 4) {
                                        RequestBuffer.request(() -> {
                                            try {
                                                new MessageBuilder(KekBot.client).withChannel(KekBot.client.getChannelByID(rawSplit[2])).withContent(rawSplit[3]).send();
                                                new MessageBuilder(KekBot.client).withChannel(channel).withContent("Successfully sent message to: ``(" + KekBot.client.getChannelByID(rawSplit[2]).getGuild().getName() + ") #" + KekBot.client.getChannelByID(rawSplit[2]).getName() + "``!").send();
                                            } catch (DiscordException e) {
                                                e.printStackTrace();
                                            } catch (MissingPermissionsException e) {
                                                RequestBuffer.request(() -> {
                                                    try {
                                                        new MessageBuilder(KekBot.client).withChannel(channel).withContent("I don't have permissions to say messages in that channel! Aborting!").send();
                                                    } catch (DiscordException | MissingPermissionsException er) {
                                                        er.printStackTrace();
                                                    }
                                                });
                                            } catch (RuntimeException e) {
                                                RequestBuffer.request(() -> {
                                                    try {
                                                        new MessageBuilder(KekBot.client).withChannel(channel).withContent("`" + rawSplit[2] + "`" + " is not a valid channel ID or I am not on a server with this channel ID!").send();
                                                    } catch (DiscordException | MissingPermissionsException er) {
                                                        er.printStackTrace();
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        EasyMessage.send(channel, "You can't expect me to send someone a message to this channel without telling me what to send it!");
                                    }
                                } else {
                                    EasyMessage.send(channel, "You can't expect me to send someone a message without telling me what channel to send a message to!");
                                }
                                break;
                        }
                    }
                }
                if (message.startsWith("tickets") || message.startsWith("ticket")) {
                    String rawSplit[] = message.split(" ", 3);
                    if (rawSplit[0].equals("tickets") || rawSplit[0].equals("ticket")) {
                        if (rawSplit.length == 1) {
                            EasyMessage.send(channel, "You have **" + XMLUtils.numberOfTickets() + (XMLUtils.numberOfTickets() == 1 ? "** ticket." : "** tickets."));
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
                                        EasyMessage.send(channel, "No ticket specified.");
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
                                        EasyMessage.send(channel, "No ticket specified.");
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
                                            EasyMessage.send(channel, "No reply message specified.");
                                        }
                                    } else {
                                        EasyMessage.send(channel, "No ticket specified.");
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
                        EasyMessage.send(channel, "__**KekBot**__\n*Your helpful meme-based bot!*\n" +
                                "```md\n" + pages.get(0) + "\n\n" + "[Page](1" + "/" + pages.size() + ")\n" +
                                "# Type \"help <number>\" to view that page!" + "```");
                    } else {
                        try {
                            if (Integer.valueOf(rawSplit[1]) > pages.size()) {
                                EasyMessage.send(channel, "Specified page does not exist!");
                            } else {
                                EasyMessage.send(channel, "__**KekBot**__\n*Your helpful meme-based bot!*\n" +
                                        "```md\n" + pages.get(Integer.valueOf(rawSplit[1]) - 1) + "\n\n[Page](" + rawSplit[1] + "/" + pages.size() + ")\n" +
                                        "# Type \"help <number>\" to view that page!" + "```");
                            }
                        } catch (NumberFormatException e) {
                            EasyMessage.send(channel, "\"" + rawSplit[1] + "\" is not a number!");
                        }
                    }
                }
            }

        }
    }


    @EventSubscriber
    public void onInviteEvent(InviteReceivedEvent event) {
        if (event.getMessage().getChannel().isPrivate() && (!event.getMessage().getAuthor().equals(KekBot.client.getOurUser()))) {
            RequestBuffer.request(() -> {
                try {
                    new MessageBuilder(KekBot.client).withChannel(event.getMessage().getChannel()).withContent("Thanks for the invite! However, I cannot simply join your server! You must allow me to connect to your server using the following link:" +
                            "\nhttps://discordapp.com/oauth2/authorize?&client_id=213151748855037953&scope=bot&permissions=0x00000008").send();
                } catch (DiscordException | MissingPermissionsException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @EventSubscriber
    public void onGuildJoinEvent(GuildCreateEvent event) {
        out.println(ft2.format(time) + "Joined/Created server: \"" + event.getGuild().getName() + "\" (ID: " + event.getGuild().getID() + ")");
        try {
            String server = event.getGuild().getID();
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
                EasyMessage.send(event.getGuild().getChannels().get(0), "Thanks for inviting me!");
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

        if (event.getGuild().equals(KekBot.client.getGuildByID("221910104495095808"))) {
            CommandRegistry.getForClient(KekBot.client).customRegister(new Command("customTest")
                    .withCategory(TEST)
                    .withDescription("Just a test command.")
                    .withUsage("{p}test")
                    .caseSensitive(true)
                    .onExecuted(context -> {
                        EasyMessage.send(context.getMessage().getChannel(), "Test Successful! Custom Comands now work!");
                    }), KekBot.client.getGuildByID("221910104495095808"));
        }
    }

    @EventSubscriber
    public void onTrackFinishEvent(TrackFinishEvent event) {
        if (event.getPlayer().getPlaylistSize() == 0) {
            Optional<IVoiceChannel> voiceChannel = event.getClient().getConnectedVoiceChannels().stream().filter(c -> c.getGuild().equals(event.getPlayer().getGuild())).findFirst();
            if (voiceChannel.isPresent()) {
                voiceChannel.get().leave();
            }
        }
    }

    @EventSubscriber
    public void onGuildUpdateEvent(GuildUpdateEvent event) {
        if (!event.getNewGuild().getName().equals(event.getOldGuild().getName())) {
            try {
                String server = event.getNewGuild().getID();
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


                if (!root.getChild("name").getText().equals(event.getNewGuild().getName())) {
                    root.getChild("name").setText(event.getNewGuild().getName());
                }


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
            } catch (IOException | JDOMException e) {
                e.printStackTrace();
            }
        }
    }

    @EventSubscriber
    public void onUserJoinGuild(UserJoinEvent event) {
        out.println(ft2.format(time) + event.getUser().getName() + " has joined " + event.getGuild().getName() + ".");
        try {
            String server = event.getGuild().getID();
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
                        String message = root.getChild("announce").getChild("welcome").getChild("message").getText().replace("{mention}", event.getUser().mention()).replace("{name}", event.getUser().getName());
                        EasyMessage.send(KekBot.client.getChannelByID(channelID), message);
                    }
                }
            }

            if (root.getChild("auto_role") != null) {
                if (root.getChild("auto_role").getChild("role") != null) {
                    Element finalRoot = root;
                    RequestBuffer.request(() -> {
                        try {
                            event.getUser().addRole(KekBot.client.getRoleByID(finalRoot.getChild("auto_role").getChild("role").getText()));
                        } catch (MissingPermissionsException e) {
                            EasyMessage.send(event.getGuild().getChannels().get(0), "Unable to automatically set role due to not having the **Manage Roles** permission.");
                        } catch (DiscordException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            EasyMessage.send(event.getGuild().getChannels().get(0), "I can no longer find the rank in which I was to automatically assign!");
                        }
                    });
                }
            }

        } catch (IOException | JDOMException e) {
            e.printStackTrace();
        }
    }

    @EventSubscriber
    public void onUserLeaveGuild(UserLeaveEvent event) {
        out.println(ft2.format(time) + event.getUser().getName() + " has left " + event.getGuild().getName() + ".");
        try {
            String server = event.getGuild().getID();
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
                            String message = root.getChild("announce").getChild("goodbye").getChild("message").getText().replace("{mention}", event.getUser().mention()).replace("{name}", event.getUser().getName());
                            EasyMessage.send(KekBot.client.getChannelByID(channelID), message);
                        } else {
                            String channelID = root.getChild("announce").getChild("goodbye").getChild("channel").getText();
                            String message = event.getUser().getName() + " has left the server!";
                            EasyMessage.send(KekBot.client.getChannelByID(channelID), message);
                        }
                    }
                }
            }

        } catch (IOException | JDOMException e) {
            e.printStackTrace();
        }
    }

    @EventSubscriber
    public void logSentMessages(MessageSendEvent event) {
        if (!event.getMessage().getChannel().isPrivate()) {
            out.println(ft2.format(time) + event.getMessage().getGuild().getName() + " - #" + event.getMessage().getChannel().getName() + " - " + event.getMessage().getAuthor().getName() + ": " + event.getMessage().getContent());
        } else {
            out.println("PM To: " + event.getMessage().getChannel().getName() + ": " + event.getMessage().getContent());
        }
    }
}
