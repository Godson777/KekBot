package com.godson.kekbot;

import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.Exceptions.ChannelNotFoundException;
import com.godson.kekbot.Exceptions.MessageNotFoundException;
import com.godson.kekbot.Settings.*;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.events.InviteReceivedEvent;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.guild.GuildUpdateEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.exceptions.BlockedException;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.hooks.ListenerAdapter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
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

import static java.lang.System.console;
import static java.lang.System.mapLibraryName;
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
        System.out.println("KekBot is ready to roll!");
        //Randomize the game the bot is playing.
        Timer gameStatusTimer = new Timer();
        gameStatusTimer.schedule(new GameStatus(), 0, TimeUnit.MINUTES.toMillis(10));
        //Set startup time
        start = Calendar.getInstance().getTime();
        for (JDA jda : KekBot.jdas) {
            jda.getGuilds().forEach(guild -> {
                out.println(ft2.format(time) + "Joined/Created server: \"" + guild.getName() + "\" (ID: " + guild.getId() + ")");
                Settings settings = GSONUtils.getSettings(guild);

                if (settings == null) {
                    settings = new Settings().setName(guild.getName());
                    settings.save(guild);
                }

                if (settings.getPrefix() != null) {
                    CommandRegistry.getForClient(jda).setPrefixForGuild(guild, settings.getPrefix());
                }

            /*if (guild.getId().equals("221910104495095808")) {
                CommandRegistry.getForClient(KekBot.jda).customRegister(new Command("customTest")
                        .withCategory(TEST)
                        .withDescription("Just a test command.")
                        .withUsage("{p}test")
                        .caseSensitive(true)
                        .onExecuted(context -> {
                            context.getTextChannel().sendMessageAsync("Test Successful! Custom Comands now work!", null);
                        }), KekBot.jda.getGuildById("221910104495095808"));
            }*/
            });
        }


    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        //Logging
        if (!event.isPrivate()) out.println(ft2.format(time) + event.getGuild().getName() + " - #" +
                event.getTextChannel().getName() + " - " + event.getAuthor().getUsername() + ": " + event.getMessage().getRawContent());
        else out.println("PM " + (event.getAuthor().equals(event.getJDA().getSelfInfo()) ? "From: " : "To: ")
                    + event.getAuthor().getUsername() + ": " + event.getMessage().getRawContent());
        //Rest of normal stuff

        String serverID = (!event.isPrivate() ? event.getGuild().getId() : null);
        String message = event.getMessage().getRawContent();
        TextChannel channel = event.getTextChannel();
        Guild server = event.getGuild();
        String prefix;
        String line = "";

        if (!event.isPrivate()) {
            if (message.equals("<@213151748855037953> reloadPrefixes") && event.getMessage().getAuthor().getId().equals("99405418077364224")) {
                List<Guild> servers = event.getJDA().getGuilds();
                for (Guild guild : event.getJDA().getGuilds()) {
                    CommandRegistry registry = CommandRegistry.getForClient(event.getJDA());
                    if (registry.getPrefixForGuild(guild) != null) {
                        if (GSONUtils.getSettings(guild).getPrefix() != null) {
                            registry.setPrefixForGuild(guild, GSONUtils.getSettings(guild).getPrefix());
                        } else {
                            registry.deletePrefixForGuild(guild);
                        }
                    }
                }
                channel.sendMessageAsync("Succesfully reset prefix for all " + servers.size() + "servers.", null);
            }
            //UserState state = KekBot.states.checkUserState(event.getMessage().getAuthor(), server);
            //if (state != null) {}
        }

        //THE FOLLOWING CHECKS IF MESSAGES RECEIVED ARE NOT PMS.
        if (!event.isPrivate()) {
            prefix = (CommandRegistry.getForClient(event.getJDA()).getPrefixForGuild(server) != null ? CommandRegistry.getForClient(event.getJDA()).getPrefixForGuild(server) :
            CommandRegistry.getForClient(event.getJDA()).getPrefix());

                    //command begin

                    if (!event.getAuthor().isBot()) {

                        if (message.equals(event.getJDA().getSelfInfo().getAsMention() + " prefix")) {
                            channel.sendMessageAsync("The prefix for __**" + server.getName() + "**__ is: **" + prefix + "**", null);
                        }

                        if (message.equals(event.getJDA().getSelfInfo().getAsMention() + " help")) {
                            List<String> commands = new ArrayList<String>();
                            List<String> pages = new ArrayList<String>();
                            CommandRegistry registry = CommandRegistry.getForClient(event.getJDA());
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

                            event.getMessage().getAuthor().getPrivateChannel().sendMessageAsync("__**KekBot**__\n*Your helpful meme-based bot!*\n" +
                                    "```md\n" + pages.get(0) + "\n\n" + "[Page](1" + "/" + pages.size() + ")\n" +
                                    "# Type \"help <number>\" to view that page!" + "```", null);
                            event.getTextChannel().sendMessageAsync(event.getMessage().getAuthor().getAsMention() + " Alright, check your PMs! :thumbsup:", null);
                        }

                        /*if (message.equals(prefix + "killvoice")) {
                            Optional<VoiceChannel> voiceChannel = KekBot.jda.getConnectedVoiceChannels().stream().filter(c -> c.getGuild().equals(server)).findFirst();
                            if (voiceChannel.isPresent()) {
                                voiceChannel.get().leave();
                                AudioPlayer.getAudioPlayerForGuild(server).clean();
                            }
                        }*/



                    }
                    //command end






            //THE FOLLOWING CHECKS IF MESSAGES RECEIVED ARE PMS.
        } else {
            //End of PM Based command

        }
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        PrivateChannel channel = event.getChannel();
        String message = event.getMessage().getRawContent();
        if (event.getMessage().getAuthor().getId().equals(GSONUtils.getConfig().getBotOwner())) {
            if (message.startsWith("message")) {
                String rawSplit[] = message.split(" ", 4);
                if (rawSplit.length == 1) {
                    channel.sendMessageAsync("You can't expect me to send someone a message without telling me who or where I'm sending a message to!", null);
                } else if (rawSplit.length >= 2) {
                    switch (rawSplit[1]) {
                        case "user":
                            if (rawSplit.length >= 3) {
                                if (rawSplit.length == 4) {
                                    try {
                                        event.getJDA().getUserById(rawSplit[2]).getPrivateChannel().sendMessageAsync(rawSplit[3], null);
                                        channel.sendMessageAsync("Successfully sent message to: __**" + event.getJDA().getUserById(rawSplit[2]).getUsername() + "**__!", null);
                                    } catch (BlockedException e) {
                                        channel.sendMessageAsync("It appears that +" + event.getJDA().getUserById(rawSplit[2]).getUsername() + " has either blocked me!", null);
                                    } catch (NullPointerException e) {
                                        channel.sendMessageAsync("`" + rawSplit[2] + "`" + " is not a valid user ID!", null);
                                    }
                                } else {
                                    channel.sendMessageAsync("You can't expect me to send someone a message to this user without telling me what to send them!", null);
                                }
                            } else {
                                channel.sendMessageAsync("You can't expect me to send someone a message without telling me who I'm send sending a message to!", null);
                            }
                            break;
                        case "channel":
                            if (rawSplit.length >= 3) {
                                if (rawSplit.length == 4) {
                                    try {
                                        event.getJDA().getTextChannelById(rawSplit[2]).sendMessageAsync(rawSplit[3], null);
                                        channel.sendMessageAsync("Successfully sent message to: ``(" + event.getJDA().getTextChannelById(rawSplit[2]).getGuild().getName() + ") #" + event.getJDA().getTextChannelById(rawSplit[2]).getName() + "``!", null);
                                    } catch (PermissionException e) {
                                        channel.sendMessageAsync("I don't have permissions to say messages in that channel! Aborting!", null);
                                    } catch (RuntimeException e) {
                                        channel.sendMessageAsync("`" + rawSplit[2] + "`" + " is not a valid channel ID or I am not on a server with this channel ID!", null);
                                    }
                                } else {
                                    channel.sendMessageAsync("You can't expect me to send someone a message to this channel without telling me what to send it!", null);
                                }
                            } else {
                                channel.sendMessageAsync("You can't expect me to send someone a message without telling me what channel to send a message to!", null);
                            }
                            break;
                    }
                }
            }
            if (message.startsWith("tickets") || message.startsWith("ticket")) {
                String rawSplit[] = message.split(" ", 3);
                if (rawSplit[0].equals("tickets") || rawSplit[0].equals("ticket")) {
                    TicketManager tickets = GSONUtils.getTicketManager();
                    if (rawSplit.length == 1) {
                        channel.sendMessageAsync("You have **" + tickets.getTickets().size() + (tickets.getTickets().size() == 1 ? "** ticket." : "** tickets."), null);
                    } else {
                        switch (rawSplit[1]) {
                            case "list":
                                int size = tickets.getTickets().size();
                                List<String> ticketsList = new ArrayList<>();
                                List<String> pages = new ArrayList<>();
                                String pageNumber = (rawSplit.length == 3 ? rawSplit[2] : null);

                                if (size != 0) {
                                    for (int i = 0; i < size; i++) {
                                        Ticket ticket = tickets.getTickets().get(i);
                                        String ticketTitle = ticket.getTitle();
                                        ticketsList.add(String.valueOf(i+1) + ". \"" + (ticketTitle.length() >= 24 ? ticketTitle.substring(0, 25) + "..." : ticketTitle) + "\"" + StringUtils.repeat(" ", 30-(ticketTitle.length() >= 20 ? 28 : ticketTitle.length())) + "<" + ticket.getStatus().getName() + ">");
                                    }
                                    try {
                                        if (pageNumber == null || Integer.valueOf(pageNumber) == 1) {
                                            if (ticketsList.size() <= 10) {
                                                channel.sendMessageAsync("```md\n" + StringUtils.join(ticketsList, "\n") + "```", null);
                                            } else {
                                                for (int i = 0; i < ticketsList.size(); i += 10) {
                                                    try {
                                                        pages.add(StringUtils.join(ticketsList.subList(i, i + 10), "\n"));
                                                    } catch (IndexOutOfBoundsException e) {
                                                        pages.add(StringUtils.join(ticketsList.subList(i, ticketsList.size()), "\n"));
                                                    }
                                                }
                                                channel.sendMessageAsync("```md\n" + pages.get(0) + "\n\n[Page](1" + "/" + pages.size() + ")" + "```", null);
                                            }
                                        } else {
                                            if (ticketsList.size() <= 10) {
                                                channel.sendMessageAsync("There are no other pages!", null);
                                            } else {
                                                for (int i = 0; i < ticketsList.size(); i += 10) {
                                                    try {
                                                        pages.add(StringUtils.join(ticketsList.subList(i, i + 10), "\n"));
                                                    } catch (IndexOutOfBoundsException e) {
                                                        pages.add(StringUtils.join(ticketsList.subList(i, ticketsList.size()), "\n"));
                                                    }
                                                }
                                                if (Integer.valueOf(pageNumber) > pages.size()) {
                                                    channel.sendMessageAsync("Specified page does not exist!", null);
                                                } else {
                                                    channel.sendMessageAsync("```md\n" + pages.get(Integer.valueOf(pageNumber) - 1) + "\n\n[Page](" + pageNumber + "/" + pages.size() + ")" + "```", null);
                                                }
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        channel.sendMessageAsync("\"" + pageNumber + "\" is not a number!", null);
                                    }
                                } else {
                                    channel.sendMessageAsync("There are no tickets to list!", null);
                                }
                                break;
                            case "view":
                                if (rawSplit.length == 3) {
                                    if (tickets.getTickets().size() == 0) {
                                        channel.sendMessageAsync("There are no tickets to view!", null);
                                    } else {
                                        try {
                                            if (Integer.valueOf(rawSplit[2]) <= tickets.getTickets().size()) {
                                                int ticketNumberInt = Integer.valueOf(rawSplit[2]) - 1;
                                                Ticket ticket = tickets.getTickets().get(ticketNumberInt);
                                                String ticketTitle = ticket.getTitle();
                                                String ticketContents = ticket.getContents();
                                                String ticketAuthor = "";
                                                String ticketGuild = "";
                                                for (JDA jda : KekBot.jdas) {
                                                    try {
                                                        User author = jda.getUserById(ticket.getAuthorID());
                                                        Guild authorGuild = jda.getGuildById(ticket.getGuildID());
                                                        ticketAuthor = author.getUsername() + "#" + author.getDiscriminator();
                                                        ticketGuild = authorGuild.getName();
                                                    } catch (NullPointerException e) {
                                                        //do nothing
                                                    }
                                                }
                                                String ticketStatus = ticket.getStatus().getName();

                                                channel.sendMessageAsync("Title: **" + ticketTitle + "**" +
                                                        "\nStatus: **" + ticketStatus + "**" +
                                                        "\nAuthor: **" + ticketAuthor + "** (ID: **" + ticket.getAuthorID() + ")" + "**" +
                                                        "\nServer: **" + ticketGuild + "** (ID: **" + ticket.getGuildID() + "**)" +
                                                        "\n\nContents: \n" + ticketContents, null);
                                            }
                                        } catch (NumberFormatException e) {
                                            channel.sendMessageAsync("\"" + rawSplit[2] + "\" is not a valid number!", null);
                                        }
                                    }
                                } else {
                                    channel.sendMessageAsync("No ticket specified.", null);
                                }
                                break;
                            case "close":
                                if (rawSplit.length == 3) {
                                    if (tickets.getTickets().size() == 0) {
                                        channel.sendMessageAsync("There are no tickets to close!", null);
                                    } else {
                                        try {
                                            if (Integer.valueOf(rawSplit[2]) <= tickets.getTickets().size()) {
                                                Ticket ticket = tickets.getTickets().get(Integer.valueOf(rawSplit[2])-1);
                                                channel.sendMessageAsync("Ticket Closed.", null);
                                                for (JDA jda : KekBot.jdas) {
                                                    jda.getUserById(ticket.getAuthorID()).getPrivateChannel().sendMessageAsync("Your ticket (**" + ticket.getTitle() + "**) has been closed.", null);
                                                }
                                                tickets.closeTicket(ticket);
                                                tickets.save();
                                            }
                                        } catch (NumberFormatException e) {
                                            channel.sendMessageAsync("\"" + rawSplit[2] + "\" is not a valid number!", null);
                                        }
                                    }
                                } else {
                                    channel.sendMessageAsync("No ticket specified.", null);
                                }
                                break;
                            case "reply":
                                if (rawSplit.length == 3) {
                                    String parameters[] = rawSplit[2].split(" ", 2);
                                    if (parameters.length == 2) {
                                        if (tickets.getTickets().size() == 0) {
                                            channel.sendMessageAsync("You don't have any tickets to view!", null);
                                        } else {
                                            try {
                                                if (Integer.valueOf(parameters[0]) <= tickets.getTickets().size()) {
                                                    Ticket ticket = tickets.getTickets().get(Integer.valueOf(parameters[0])-1);
                                                    tickets.replyToTicketManager(ticket, parameters[1], event.getAuthor());
                                                    channel.sendMessageAsync("Reply Sent!", null);
                                                }
                                            } catch (NumberFormatException e) {
                                                channel.sendMessageAsync("\"" + parameters[0] + "\" is not a valid number!", null);
                                            }
                                        }
                                    } else {
                                        channel.sendMessageAsync("No reply message specified.", null);
                                    }
                                } else {
                                    channel.sendMessageAsync("No ticket specified.", null);
                                }
                        }
                    }
                }
            }
            //End of Owner-Only PM Based command
        }



        if (event.getMessage().getContent().startsWith("help")) {
            String rawSplit[] = event.getMessage().getContent().split(" ", 2);
            List<String> commands = new ArrayList<String>();
            List<String> pages = new ArrayList<String>();
            CommandRegistry registry = CommandRegistry.getForClient(event.getJDA());
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
                    channel.sendMessageAsync("__**KekBot**__\n*Your helpful meme-based bot!*\n" +
                            "```md\n" + pages.get(0) + "\n\n" + "[Page](1" + "/" + pages.size() + ")\n" +
                            "# Type \"help <number>\" to view that page!" + "```", null);
                } else {
                    try {
                        if (Integer.valueOf(rawSplit[1]) > pages.size()) {
                            channel.sendMessageAsync("Specified page does not exist!", null);
                        } else {
                            channel.sendMessageAsync("__**KekBot**__\n*Your helpful meme-based bot!*\n" +
                                    "```md\n" + pages.get(Integer.valueOf(rawSplit[1]) - 1) + "\n\n[Page](" + rawSplit[1] + "/" + pages.size() + ")\n" +
                                    "# Type \"help <number>\" to view that page!" + "```", null);
                        }
                    } catch (NumberFormatException e) {
                        channel.sendMessageAsync("\"" + rawSplit[1] + "\" is not a number!", null);
                    }
                }
            }
        }
    }

    @Override
    public void onInviteReceived(InviteReceivedEvent event) {
        if (event.isPrivate() && (!event.getAuthor().equals(event.getJDA().getSelfInfo())))
            event.getMessage().getChannel().sendMessageAsync("Thanks for the invite! However, I cannot simply join your server! You must allow me to connect to your server using the following link:" +
                            "\nhttps://discordapp.com/oauth2/authorize?&client_id=213151748855037953&scope=bot&permissions=0x00000008", null);

    }

    /*@Override
    public void onGuildJoin(GuildJoinEvent event) {
        out.println(ft2.format(time) + "Joined/Created server: \"" + event.getGuild().getName() + "\" (ID: " + event.getGuild().getId() + ")");
        try {
            String server = event.getGuild().getId();
            Document document = null;
            Element root = null;
            File xmlFile = new File("Settings\\" + server + ".xml");
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
                root = new Element("Settings");
                event.getGuild().getTextChannels().get(0).sendMessageAsync("Thanks for inviting me!");
            }

            if (root.getChild("name") != null) {
                if (!root.getChild("name").getText().equals(event.getGuild().getName())) {
                    root.getChild("name").setText(event.getGuild().getName());

                    document.setContent(root);
                    try {
                        FileWriter writer = new FileWriter("Settings\\" + server + ".xml");
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
                    FileWriter writer = new FileWriter("Settings\\" + server + ".xml");
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
            CommandRegistry.getForClient(KekBot.jda).setPrefixForGuild(event.getGuild(), XMLUtils.getPrefix(event.getGuild()));
        }

        if (event.getGuild().equals(KekBot.jda.getGuildById("221910104495095808"))) {
            CommandRegistry.getForClient(KekBot.jda).customRegister(new Command("customTest")
                    .withCategory(TEST)
                    .withDescription("Just a test command.")
                    .withUsage("{p}test")
                    .caseSensitive(true)
                    .onExecuted(context -> {
                        context.getTextChannel().sendMessageAsync("Test Successful! Custom Comands now work!");
                    }), KekBot.jda.getGuildById("221910104495095808"));
        }
    }*/

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
            File xmlFile = new File("Settings\\" + server + ".xml");
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
                root = new Element("Settings");
            }


            if (!root.getChild("name").getText().equals(event.getGuild().getName())) {
                root.getChild("name").setText(event.getGuild().getName());
                document.setContent(root);
                try {
                    FileWriter writer = new FileWriter("Settings\\" + server + ".xml");
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
        /*try {
            String server = event.getGuild().getId();
            Document document = null;
            Element root = null;
            File xmlFile = new File("Settings\\" + server + ".xml");
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
                root = new Element("Settings");
            }


            if (root.getChild("announce") != null) {
                if (root.getChild("announce").getChild("welcome") != null) {
                    if (root.getChild("announce").getChild("welcome").getChild("channel") != null && root.getChild("announce").getChild("welcome").getChild("message") != null) {
                        String channelID = root.getChild("announce").getChild("welcome").getChild("channel").getText();
                        String message = root.getChild("announce").getChild("welcome").getChild("message").getText().replace("{mention}", event.getUser().getAsMention()).replace("{name}", event.getUser().getUsername());
                        KekBot.jda.getTextChannelById(channelID).sendMessageAsync(message, null);
                    }
                }
            }

            if (root.getChild("auto_role") != null) {
                if (root.getChild("auto_role").getChild("role") != null) {
                        try {
                            event.getGuild().getRolesForUser(event.getUser()).add(event.getGuild().getRoleById(root.getChild("auto_role").getChild("role").getText()));
                        } catch (PermissionException e) {
                            event.getGuild().getTextChannels().get(0).sendMessageAsync("Unable to automatically set role due to not having the **Manage Roles** permission.", null);
                        } catch (NullPointerException e) {
                            event.getGuild().getTextChannels().get(0).sendMessageAsync("I can no longer find the rank in which I was to automatically assign!", null);
                        }
                }
            }

        } catch (IOException | JDOMException e) {
            e.printStackTrace();
        }*/

        Settings settings = GSONUtils.getSettings(event.getGuild());

        if (settings.getAutoRoleID() != null) {
            try {
                event.getGuild().getManager().addRoleToUser(event.getUser(), event.getGuild().getRoleById(settings.getAutoRoleID())).update();
            } catch (PermissionException e) {
                event.getGuild().getTextChannels().get(0).sendMessageAsync("Unable to automatically set role due to not having the **Manage Roles** permission.", null);
            } catch (NullPointerException e) {
                event.getGuild().getTextChannels().get(0).sendMessageAsync("I can no longer find the rank in which I was to automatically assign!", null);
            }
        }

        if (settings.welcomeEnabled()) {
            try {
                settings.getWelcomeChannel(event.getJDA()).sendMessageAsync(settings.getWelcomeMessage().replace("{mention}", event.getUser().getAsMention()).replace("{name|", event.getUser().getUsername()), null);
            } catch (MessageNotFoundException e) {
                settings.setWelcomeMessage(null).toggleWelcome(false).save(event.getGuild());
                for (TextChannel channel : event.getGuild().getTextChannels()) {
                    try {
                        channel.sendMessageAsync("**WARNING:** KekBot could not find this server's welcome message! Please set it using `$announce welcome message <message>`!", null);
                        break;
                    } catch (PermissionException er) {
                        //¯\_(ツ)_/¯
                    }
                }
            } catch (ChannelNotFoundException | NullPointerException e) {
                settings.setWelcomeChannel(null).toggleWelcome(false).save(event.getGuild());
                for (TextChannel channel : event.getGuild().getTextChannels()) {
                    try {
                        channel.sendMessageAsync("**WARNING:** KekBot could not find this server's welcome channel! Please set it using `$announce welcome channel <#channel>`!", null);
                        break;
                    } catch (PermissionException er) {
                        //¯\_(ツ)_/¯
                    }
                }
            }
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        out.println(ft2.format(time) + event.getUser().getUsername() + " has left " + event.getGuild().getName() + ".");
        /*try {
            String server = event.getGuild().getId();
            Document document = null;
            Element root = null;
            File xmlFile = new File("Settings\\" + server + ".xml");
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
                root = new Element("Settings");
            }


            if (root.getChild("announce") != null) {
                if (root.getChild("announce").getChild("goodbye") != null) {
                    if (root.getChild("announce").getChild("goodbye").getChild("channel") != null) {
                        if (root.getChild("announce").getChild("goodbye").getChild("message") != null) {
                            String channelID = root.getChild("announce").getChild("goodbye").getChild("channel").getText();
                            String message = root.getChild("announce").getChild("goodbye").getChild("message").getText().replace("{mention}", event.getUser().getAsMention()).replace("{name}", event.getUser().getUsername());
                            event.getJDA().getTextChannelById(channelID).sendMessageAsync(message, null);
                        } else {
                            String channelID = root.getChild("announce").getChild("goodbye").getChild("channel").getText();
                            String message = event.getUser().getUsername() + " has left the server!";
                            event.getJDA().getTextChannelById(channelID).sendMessageAsync(message, null);
                        }
                    }
                }
            }

        } catch (IOException | JDOMException e) {
            e.printStackTrace();
        }*/
        Settings settings = GSONUtils.getSettings(event.getGuild());

        if (settings.farewellEnabled()) {
            try {
                settings.getFarewellChannel(event.getJDA()).sendMessageAsync(settings.getFarewellMessage().replace("{mention}", event.getUser().getAsMention()).replace("{name}", event.getUser().getUsername()), null);
            } catch (MessageNotFoundException e) {
                settings.setFarewellMessage(null).toggleFarewell(false).save(event.getGuild());
                for (TextChannel channel : event.getGuild().getTextChannels()) {
                    try {
                        channel.sendMessageAsync("**WARNING:** KekBot could not find this server's welcome message! Please set it using `$announce welcome message <message>`!", null);
                        break;
                    } catch (PermissionException er) {
                        //¯\_(ツ)_/¯
                    }
                }
            } catch (ChannelNotFoundException | NullPointerException e) {
                settings.setFarewellChannel(null).toggleFarewell(false).save(event.getGuild());
                for (TextChannel channel : event.getGuild().getTextChannels()) {
                    try {
                        channel.sendMessageAsync("**WARNING:** KekBot could not find this server's welcome channel! Please set it using `$announce welcome channel <#channel>`!", null);
                        break;
                    } catch (PermissionException er) {
                        //¯\_(ツ)_/¯
                    }
                }
            }
        }
    }
}
