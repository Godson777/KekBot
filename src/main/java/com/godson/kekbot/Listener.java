package com.godson.kekbot;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.CommandRegistry;
import com.godson.kekbot.Exceptions.ChannelNotFoundException;
import com.godson.kekbot.Exceptions.MessageNotFoundException;
import com.godson.kekbot.Responses.Action;
import com.godson.kekbot.Settings.CustomCommand;
import com.godson.kekbot.Settings.Settings;
import com.godson.kekbot.Settings.Ticket;
import com.godson.kekbot.Settings.TicketManager;
import com.godson.kekbot.commands.community.AddResponse;
import com.godson.kekbot.commands.community.Suggestions;
import com.godson.kekbot.commands.community.Suggest;
import com.godson.kekbot.commands.test;
import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.update.GenericGuildUpdateEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.System.console;
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

                if (GSONUtils.numberOfCCommands(guild) > 0) {
                    List<CustomCommand> commands = GSONUtils.getCCommands(guild);
                    for (CustomCommand command : commands) {
                        try {
                            command.register(jda, guild);
                        } catch (IllegalArgumentException e) {
                            //ignore
                        }
                    }
                }
            });
            CommandRegistry registry = CommandRegistry.getForClient(jda);
            registry.customRegister(test.test, jda.getGuildById("221910104495095808"));
            registry.customRegister(Suggest.suggest, jda.getGuildById("221910104495095808"));
            registry.customRegister(AddResponse.addResponse, jda.getGuildById("221910104495095808"));
            registry.customRegister(Suggestions.suggestions, jda.getGuildById("221910104495095808"));
        }


    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromType(ChannelType.PRIVATE)) out.println(ft2.format(System.currentTimeMillis()) + /*"Shard " + event.getJDA().getShardInfo().getShardId() + " - " +*/ event.getGuild().getName() + " - #" +
                event.getTextChannel().getName() + " - " + event.getAuthor().getName() + ": " + event.getMessage().getRawContent());
        else out.println("PM " + (event.getAuthor().equals(event.getJDA().getSelfUser()) ? "To: " : "From: ")
                + event.getAuthor().getName() + ": " + event.getMessage().getRawContent());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        String message = event.getMessage().getRawContent();
        TextChannel channel = event.getChannel();
        Guild server = event.getGuild();
        String prefix;
        prefix = (CommandRegistry.getForClient(event.getJDA()).getPrefixForGuild(server) != null ? CommandRegistry.getForClient(event.getJDA()).getPrefixForGuild(server) :
                CommandRegistry.getForClient(event.getJDA()).getPrefix());

        //command begin

        if (!event.getAuthor().isBot()) {

            if (message.equals(event.getJDA().getSelfUser().getAsMention() + " prefix")) {
                channel.sendMessage("The prefix for __**" + server.getName() + "**__ is: **" + prefix + "**").queue();
            }

            if (message.equals(event.getJDA().getSelfUser().getAsMention() + " help")) {
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
                for (int i = 0; i < commands.size(); i += 25) {
                    try {
                        pages.add(StringUtils.join(commands.subList(i, i + 25), "\n"));
                    } catch (IndexOutOfBoundsException e) {
                        pages.add(StringUtils.join(commands.subList(i, commands.size()), "\n"));
                    }
                }

                event.getMessage().getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("__**KekBot**__\n*Your helpful meme-based bot!*\n" +
                        "```md\n" + pages.get(0) + "\n\n" + "[Page](1" + "/" + pages.size() + ")\n" +
                        "# Type \"help <number>\" to view that page!" + "```").queue());
                event.getChannel().sendMessage(event.getMessage().getAuthor().getAsMention() + " Alright, check your PMs! :thumbsup:").queue();
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
                    channel.sendMessage("You can't expect me to send someone a message without telling me who or where I'm sending a message to!").queue();
                } else if (rawSplit.length >= 2) {
                    switch (rawSplit[1]) {
                        case "user":
                            if (rawSplit.length >= 3) {
                                if (rawSplit.length == 4) {
                                    try {
                                        event.getJDA().getUserById(rawSplit[2]).getPrivateChannel().sendMessage(rawSplit[3]).queue();
                                        channel.sendMessage("Successfully sent message to: __**" + event.getJDA().getUserById(rawSplit[2]).getName() + "**__!").queue();
                                    } catch (PermissionException e) {
                                        channel.sendMessage("It appears that I'm not able to send messages to" + event.getJDA().getUserById(rawSplit[2]).getName() + "!").queue();
                                    } catch (NullPointerException e) {
                                        channel.sendMessage("`" + rawSplit[2] + "`" + " is not a valid user ID!").queue();
                                    }
                                } else {
                                    channel.sendMessage("You can't expect me to send someone a message to this user without telling me what to send them!").queue();
                                }
                            } else {
                                channel.sendMessage("You can't expect me to send someone a message without telling me who I'm send sending a message to!").queue();
                            }
                            break;
                        case "channel":
                            if (rawSplit.length >= 3) {
                                if (rawSplit.length == 4) {
                                    try {
                                        event.getJDA().getTextChannelById(rawSplit[2]).sendMessage(rawSplit[3]).queue();
                                        channel.sendMessage("Successfully sent message to: ``(" + event.getJDA().getTextChannelById(rawSplit[2]).getGuild().getName() + ") #" + event.getJDA().getTextChannelById(rawSplit[2]).getName() + "``!").queue();
                                    } catch (PermissionException e) {
                                        channel.sendMessage("I don't have permissions to say messages in that channel! Aborting!").queue();
                                    } catch (RuntimeException e) {
                                        channel.sendMessage("`" + rawSplit[2] + "`" + " is not a valid channel ID or I am not on a server with this channel ID!").queue();
                                    }
                                } else {
                                    channel.sendMessage("You can't expect me to send someone a message to this channel without telling me what to send it!").queue();
                                }
                            } else {
                                channel.sendMessage("You can't expect me to send someone a message without telling me what channel to send a message to!").queue();
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
                        channel.sendMessage("You have **" + tickets.getTickets().size() + (tickets.getTickets().size() == 1 ? "** ticket." : "** tickets.")).queue();
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
                                                channel.sendMessage("```md\n" + StringUtils.join(ticketsList, "\n") + "```").queue();
                                            } else {
                                                for (int i = 0; i < ticketsList.size(); i += 10) {
                                                    try {
                                                        pages.add(StringUtils.join(ticketsList.subList(i, i + 10), "\n"));
                                                    } catch (IndexOutOfBoundsException e) {
                                                        pages.add(StringUtils.join(ticketsList.subList(i, ticketsList.size()), "\n"));
                                                    }
                                                }
                                                channel.sendMessage("```md\n" + pages.get(0) + "\n\n[Page](1" + "/" + pages.size() + ")" + "```").queue();
                                            }
                                        } else {
                                            if (ticketsList.size() <= 10) {
                                                channel.sendMessage("There are no other pages!").queue();
                                            } else {
                                                for (int i = 0; i < ticketsList.size(); i += 10) {
                                                    try {
                                                        pages.add(StringUtils.join(ticketsList.subList(i, i + 10), "\n"));
                                                    } catch (IndexOutOfBoundsException e) {
                                                        pages.add(StringUtils.join(ticketsList.subList(i, ticketsList.size()), "\n"));
                                                    }
                                                }
                                                if (Integer.valueOf(pageNumber) > pages.size()) {
                                                    channel.sendMessage("Specified page does not exist!").queue();
                                                } else {
                                                    channel.sendMessage("```md\n" + pages.get(Integer.valueOf(pageNumber) - 1) + "\n\n[Page](" + pageNumber + "/" + pages.size() + ")" + "```").queue();
                                                }
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        channel.sendMessage("\"" + pageNumber + "\" is not a number!").queue();
                                    }
                                } else {
                                    channel.sendMessage("There are no tickets to list!").queue();
                                }
                                break;
                            case "view":
                                if (rawSplit.length == 3) {
                                    if (tickets.getTickets().size() == 0) {
                                        channel.sendMessage("There are no tickets to view!").queue();
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
                                                        ticketAuthor = author.getName() + "#" + author.getDiscriminator();
                                                        ticketGuild = authorGuild.getName();
                                                    } catch (NullPointerException e) {
                                                        //do nothing
                                                    }
                                                }
                                                String ticketStatus = ticket.getStatus().getName();

                                                channel.sendMessage("Title: **" + ticketTitle + "**" +
                                                        "\nStatus: **" + ticketStatus + "**" +
                                                        "\nAuthor: **" + ticketAuthor + "** (ID: **" + ticket.getAuthorID() + ")" + "**" +
                                                        "\nServer: **" + ticketGuild + "** (ID: **" + ticket.getGuildID() + "**)" +
                                                        "\n\nContents: \n" + ticketContents).queue();
                                            }
                                        } catch (NumberFormatException e) {
                                            channel.sendMessage("\"" + rawSplit[2] + "\" is not a valid number!").queue();
                                        }
                                    }
                                } else {
                                    channel.sendMessage("No ticket specified.").queue();
                                }
                                break;
                            case "close":
                                if (rawSplit.length == 3) {
                                    if (tickets.getTickets().size() == 0) {
                                        channel.sendMessage("There are no tickets to close!").queue();
                                    } else {
                                        try {
                                            if (Integer.valueOf(rawSplit[2]) <= tickets.getTickets().size()) {
                                                Ticket ticket = tickets.getTickets().get(Integer.valueOf(rawSplit[2])-1);
                                                channel.sendMessage("Ticket Closed.").queue();
                                                for (JDA jda : KekBot.jdas) {
                                                    jda.getUserById(ticket.getAuthorID()).getPrivateChannel().sendMessage("Your ticket (**" + ticket.getTitle() + "**) has been closed.").queue();
                                                }
                                                tickets.closeTicket(ticket);
                                                tickets.save();
                                            }
                                        } catch (NumberFormatException e) {
                                            channel.sendMessage("\"" + rawSplit[2] + "\" is not a valid number!").queue();
                                        }
                                    }
                                } else {
                                    channel.sendMessage("No ticket specified.").queue();
                                }
                                break;
                            case "reply":
                                if (rawSplit.length == 3) {
                                    String parameters[] = rawSplit[2].split(" ", 2);
                                    if (parameters.length == 2) {
                                        if (tickets.getTickets().size() == 0) {
                                            channel.sendMessage("You don't have any tickets to view!").queue();
                                        } else {
                                            try {
                                                if (Integer.valueOf(parameters[0]) <= tickets.getTickets().size()) {
                                                    Ticket ticket = tickets.getTickets().get(Integer.valueOf(parameters[0])-1);
                                                    tickets.replyToTicketManager(ticket, parameters[1], event.getAuthor());
                                                    channel.sendMessage("Reply Sent!").queue();
                                                }
                                            } catch (NumberFormatException e) {
                                                channel.sendMessage("\"" + parameters[0] + "\" is not a valid number!").queue();
                                            }
                                        }
                                    } else {
                                        channel.sendMessage("No reply message specified.").queue();
                                    }
                                } else {
                                    channel.sendMessage("No ticket specified.").queue();
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
            for (int i = 0; i < commands.size(); i += 25) {
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
                            "# Type \"help <number>\" to view that page!" + "```").queue();
                } else {
                    try {
                        if (Integer.valueOf(rawSplit[1]) > pages.size()) {
                            channel.sendMessage("Specified page does not exist!").queue();
                        } else {
                            channel.sendMessage("__**KekBot**__\n*Your helpful meme-based bot!*\n" +
                                    "```md\n" + pages.get(Integer.valueOf(rawSplit[1]) - 1) + "\n\n[Page](" + rawSplit[1] + "/" + pages.size() + ")\n" +
                                    "# Type \"help <number>\" to view that page!" + "```").queue();
                        }
                    } catch (NumberFormatException e) {
                        channel.sendMessage("\"" + rawSplit[1] + "\" is not a number!").queue();
                    }
                }
            }
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        if (!GSONUtils.getConfig().getBlockedUsers().contains(event.getGuild().getOwner().getUser().getId())) {
            for (JDA jda : KekBot.jdas) {
                try {
                    jda.getUserById(GSONUtils.getConfig().getBotOwner()).getPrivateChannel().sendMessage("Joined server: \"" + event.getGuild().getName() + "\" (ID: " + event.getGuild().getId() + ")").queue();
                    break;
                } catch (NullPointerException e) {
                    //do nothing.
                }
            }
            Settings settings = new Settings().setName(event.getGuild().getName());
            settings.save(event.getGuild());
            String joinSpeech = "Hi! I'm KekBot! Thanks for inviting me!" + "\n" +
                    "Use $help to see a list of commands, and use $prefix to change my prefix!" + "\n" +
                    "If you ever need help, join my discord server: " + "https://discord.gg/3nbqavE";
            for (TextChannel channel : event.getGuild().getTextChannels()) {
                try {
                    channel.sendMessage(joinSpeech).queue();
                    break;
                } catch (PermissionException er) {
                    //¯\_(ツ)_/¯
                }
            }
            String token = GSONUtils.getConfig().getdApiToken();
            if (token != null) {
                try {
                    Unirest.post("https://bots.discord.pw/api/bots/" + event.getJDA().getSelfUser().getId() + "/stats")
                            .header("Content-Type", "application/json")
                            .header("Authorization", token)
                            .body("{\n" +
                                    "    \"server_count\": " + event.getJDA().getGuilds().size() +
                                    "\n}").asJson();
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
            }
        } else {
            event.getGuild().leave().queue();
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        if (!GSONUtils.getConfig().getBlockedUsers().contains(event.getGuild().getOwner().getUser().getId())) {
            for (JDA jda : KekBot.jdas) {
                try {
                    jda.getUserById(GSONUtils.getConfig().getBotOwner()).getPrivateChannel().sendMessage("Left/Kicked from server: \"" + event.getGuild().getName() + "\" (ID: " + event.getGuild().getId() + ")").queue();
                    break;
                } catch (NullPointerException e) {
                    //do nothing.
                }
            }
            File folder = new File("settings/" + event.getGuild().getId());
            Utils.deleteDirectory(folder);
            String token = GSONUtils.getConfig().getdApiToken();
            if (token != null) {
                try {
                    Unirest.post("https://bots.discord.pw/api/bots/" + event.getJDA().getSelfUser().getId() + "/stats")
                            .header("Content-Type", "application/json")
                            .header("Authorization", token)
                            .body("{\n" +
                                    "    \"server_count\": " + event.getJDA().getGuilds().size() +
                                    "\n}").asJson();
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
            }
            if (!event.getGuild().getAudioManager().isConnected()) {
                KekBot.player.killConnection(event.getGuild());
            }
        }
    }

    @Override
    public void onGenericGuildUpdate(GenericGuildUpdateEvent event) {
        Settings settings = GSONUtils.getSettings(event.getGuild());
        if (!event.getGuild().getName().equals(settings.getGuildName())) {
            settings.setName(event.getGuild().getName());
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        out.println(ft2.format(System.currentTimeMillis()) + event.getMember().getEffectiveName() + " has joined " + event.getGuild().getName() + ".");
        Settings settings = GSONUtils.getSettings(event.getGuild());

        if (settings.getAutoRoleID() != null) {
            try {
                event.getGuild().getController().addRolesToMember(event.getMember(), event.getGuild().getRoleById(settings.getAutoRoleID())).queue();
            } catch (PermissionException e) {
                event.getGuild().getTextChannels().get(0).sendMessage("Unable to automatically set role due to not having the **Manage Roles** permission.").queue();
            } catch (NullPointerException e) {
                event.getGuild().getTextChannels().get(0).sendMessage("I can no longer find the rank in which I was to automatically assign!").queue();
            }
        }

        if (settings.welcomeEnabled()) {
            try {
                settings.getWelcomeChannel(event.getGuild()).sendMessage(settings.getWelcomeMessage().replace("{mention}", event.getMember().getAsMention()).replace("{name|", event.getMember().getEffectiveName())).queue();
            } catch (MessageNotFoundException e) {
                settings.setWelcomeMessage(null).toggleWelcome(false).save(event.getGuild());
                for (TextChannel channel : event.getGuild().getTextChannels()) {
                    try {
                        channel.sendMessage("**WARNING:** KekBot could not find this server's welcome message! Please set it using `$announce welcome message <message>`!").queue();
                        break;
                    } catch (PermissionException er) {
                        //¯\_(ツ)_/¯
                    }
                }
            } catch (ChannelNotFoundException | NullPointerException e) {
                settings.setWelcomeChannel(null).toggleWelcome(false).save(event.getGuild());
                for (TextChannel channel : event.getGuild().getTextChannels()) {
                    try {
                        channel.sendMessage("**WARNING:** KekBot could not find this server's welcome channel! Please set it using `$announce welcome channel <#channel>`!").queue();
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
        out.println(ft2.format(System.currentTimeMillis()) + event.getMember().getEffectiveName() + " has left " + event.getGuild().getName() + ".");
        Settings settings = GSONUtils.getSettings(event.getGuild());

        if (settings.farewellEnabled()) {
            try {
                settings.getFarewellChannel(event.getGuild()).sendMessage(settings.getFarewellMessage().replace("{mention}", event.getMember().getAsMention()).replace("{name}", event.getMember().getEffectiveName())).queue();
            } catch (MessageNotFoundException e) {
                settings.setFarewellMessage(null).toggleFarewell(false).save(event.getGuild());
                for (TextChannel channel : event.getGuild().getTextChannels()) {
                    try {
                        channel.sendMessage("**WARNING:** KekBot could not find this server's welcome message! Please set it using `$announce welcome message <message>`!").queue();
                        break;
                    } catch (PermissionException er) {
                        //¯\_(ツ)_/¯
                    }
                }
            } catch (ChannelNotFoundException | NullPointerException e) {
                settings.setFarewellChannel(null).toggleFarewell(false).save(event.getGuild());
                for (TextChannel channel : event.getGuild().getTextChannels()) {
                    try {
                        channel.sendMessage("**WARNING:** KekBot could not find this server's welcome channel! Please set it using `$announce welcome channel <#channel>`!").queue();
                        break;
                    } catch (PermissionException er) {
                        //¯\_(ツ)_/¯
                    }
                }
            }
        }
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getGuild().getAudioManager().isConnected()) {
            if (!KekBot.player.isMeme(event.getGuild())) {
                if (event.getChannelLeft().equals(event.getGuild().getAudioManager().getConnectedChannel())) {
                    if (event.getChannelLeft().getMembers().size() > 1) {
                        if (KekBot.player.getHost(event.getGuild()).equals(event.getMember().getUser())) {
                            Random random = new Random();
                            int user = random.nextInt(event.getChannelLeft().getMembers().size());
                            User newHost = event.getChannelLeft().getMembers().get(user).getUser();
                            while (newHost.isBot()) {
                                user = random.nextInt(event.getChannelLeft().getMembers().size());
                                newHost = event.getChannelLeft().getMembers().get(user).getUser();
                            }
                            KekBot.player.changeHost(event.getGuild(), newHost);
                            KekBot.player.announceToMusicSession(event.getGuild(), newHost.getName() + " is now the host of this music session.");
                        }
                    } else {
                        KekBot.player.announceToMusicSession(event.getGuild(), KekBot.respond(Action.MUSIC_EMPTY_CHANNEL));
                        KekBot.player.closeConnection(event.getGuild());
                    }
                }
            }
        }
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (event.getGuild().getAudioManager().isConnected()) {
            if (!KekBot.player.isMeme(event.getGuild())) {
                if (event.getChannelLeft().equals(event.getGuild().getAudioManager().getConnectedChannel())) {
                    if (event.getChannelLeft().getMembers().size() > 1) {
                        if (KekBot.player.getHost(event.getGuild()).equals(event.getMember().getUser())) {
                            Random random = new Random();
                            int user = random.nextInt(event.getChannelLeft().getMembers().size());
                            User newHost = event.getChannelLeft().getMembers().get(user).getUser();
                            while (newHost.isBot()) {
                                user = random.nextInt(event.getChannelLeft().getMembers().size());
                                newHost = event.getChannelLeft().getMembers().get(user).getUser();
                            }
                            KekBot.player.changeHost(event.getGuild(), newHost);
                            KekBot.player.announceToMusicSession(event.getGuild(), newHost.getName() + " is now the host of this music session.");
                        }
                    } else {
                        KekBot.player.announceToMusicSession(event.getGuild(), KekBot.respond(Action.MUSIC_EMPTY_CHANNEL));
                        KekBot.player.closeConnection(event.getGuild());
                    }
                }
            }
        }
    }
}
