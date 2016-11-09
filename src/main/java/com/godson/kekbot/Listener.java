package com.godson.kekbot;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.Exceptions.ChannelNotFoundException;
import com.godson.kekbot.Exceptions.MessageNotFoundException;
import com.godson.kekbot.Settings.Settings;
import com.godson.kekbot.Settings.Ticket;
import com.godson.kekbot.Settings.TicketManager;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.InviteReceivedEvent;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.events.guild.GuildUpdateEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.exceptions.BlockedException;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
                Settings settings = GSONUtils.getSettings(guild);

                if (settings == null) {
                    settings = new Settings().setName(guild.getName());
                    settings.save(guild);
                }

                if (settings.getPrefix() != null) {
                    CommandRegistry.getForClient(jda).setPrefixForGuild(guild, settings.getPrefix());
                }
            });
        }


    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isPrivate()) out.println(ft2.format(time) + event.getGuild().getName() + " - #" +
                event.getTextChannel().getName() + " - " + event.getAuthor().getUsername() + ": " + event.getMessage().getRawContent());
        else out.println("PM " + (event.getAuthor().equals(event.getJDA().getSelfInfo()) ? "To: " : "From: ")
                + event.getAuthor().getUsername() + ": " + event.getMessage().getRawContent());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        String message = event.getMessage().getRawContent();
        TextChannel channel = event.getChannel();
        Guild server = event.getGuild();
        String prefix;

            //UserState state = KekBot.states.checkUserState(event.getMessage().getAuthor(), server);
            //if (state != null) {}
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
                            event.getChannel().sendMessageAsync(event.getMessage().getAuthor().getAsMention() + " Alright, check your PMs! :thumbsup:", null);
                        }
                    }
                    //command end
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
                if (!category.equals(CommandCategory.BOT_OWNER)) commands.add("# " + category.toString());
                if (category.equals(CommandCategory.BOT_OWNER) && event.getAuthor().getId().equals(GSONUtils.getConfig().getBotOwner())) commands.add("# " + category.toString());
                for (Command command : registry.getCommands()) {
                    Set<String> aliases = command.getAliases();
                    if (command.getCategory() != null) {
                        if (command.getCategory().equals(category)) {
                            if (!category.equals(CommandCategory.BOT_OWNER))
                                commands.add("[$" + command.getName() +
                                        (aliases.size() != 0 ? " | " + StringUtils.join(aliases, " | ") : "") + "](" + command.getDescription() + ")");
                            if (category.equals(CommandCategory.BOT_OWNER) && event.getAuthor().getId().equals(GSONUtils.getConfig().getBotOwner()))
                                commands.add("[$" + command.getName() +
                                        (aliases.size() != 0 ? " | " + StringUtils.join(aliases, " | ") : "") + "](" + command.getDescription() + ")");
                        }
                    }
                }
                if (!category.equals(CommandCategory.BOT_OWNER)) commands.add("");
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


    public void onGuildJoin(GuildJoinEvent event) {
        event.getJDA().getUserById(GSONUtils.getConfig().getBotOwner()).getPrivateChannel().sendMessageAsync("Joined server: \"" + event.getGuild().getName() + "\" (ID: " + event.getGuild().getId() + ")", null);
        event.getGuild().getTextChannels().get(0).sendMessageAsync("Thanks for inviting me!", null);
        Settings settings = new Settings().setName(event.getGuild().getName());
        settings.save(event.getGuild());
        String joinSpeech = "Hi! I'm KekBot! Thanks for inviting me!" + "\n" +
                "Use $help to see a list of commands, and use $prefix to change my prefix!" + "\n" +
                "If you ever need help, join my discord server: " + "https://discord.gg/3nbqavE";
        for (TextChannel channel : event.getGuild().getTextChannels()) {
            try {
                channel.sendMessageAsync(joinSpeech, null);
                break;
            } catch (PermissionException er) {
                //¯\_(ツ)_/¯
            }
        }
    }

    @Override
    public void onGuildUpdate(GuildUpdateEvent event) {
        Settings settings = GSONUtils.getSettings(event.getGuild());
        if (!event.getGuild().getName().equals(settings.getGuildName())) {
            settings.setName(event.getGuild().getName());
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        out.println(ft2.format(time) + event.getUser().getUsername() + " has joined " + event.getGuild().getName() + ".");
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
