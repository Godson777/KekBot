package com.godson.kekbot.command.commands;


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
import net.dv8tion.jda.core.entities.Guild;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;

public class TestCommand extends Command {

    public TestCommand() {
        name = "test";
        description = "test";
        category = new Category("Test");
    }

    @Override
    public void onExecuted(CommandEvent event) throws Throwable {

        /**
         * Ports all settings to rethinkdb.
         */
        /*
        try {
            for (Guild guild : KekBot.jda.getGuilds()) {
                Settings settings = GSONUtils.getLegacySettings(guild);
                settings.toggleBroadcasts(false);
                TagManager tagManager = GSONUtils.getLegacyTagManager(guild);
                QuoteManager quoteManager = GSONUtils.getLegacyQuotes(guild);
                KekBot.r.table("Settings").insert(KekBot.r.hashMap("Guild ID", event.getGuild().getId())
                        .with("Prefix", settings.getPrefix())
                        .with("AutoRole ID", settings.getAutoRoleID())
                        .with("Announce Settings", settings.announceSettings)
                        .with("Tags", tagManager)
                        .with("Quotes", quoteManager.getList())).run(KekBot.conn);
                return;
            }
        } catch (Exception e) {
            event.getChannel().sendMessage(e.getLocalizedMessage()).queue();
            e.printStackTrace();
        }
        event.getChannel().sendMessage("Successfully ported all server specific settings, quotes, and tags to rethinkdb.").queue();

        Gson gson = new Gson();
        try {
            for (File file : new File("profiles").listFiles()) {
                FileReader reader = new FileReader(file);
                Profile profile = gson.fromJson(reader, Profile.class);
                reader.close();
                MapObject object = KekBot.r.hashMap("User ID", file.getName().replace(".json", ""))
                        .with("Token", profile.token)
                        .with("Tokens", profile.getTokens())
                        .with("Backgrounds", profile.getBackgrounds())
                        .with("Current Background ID", profile.hasBackgroundEquipped() ? profile.getCurrentBackground().getID() : null)
                        .with("Badge", profile.getBadge())
                        .with("Topkeks", profile.getTopkeks())
                        .with("KXP", profile.getKXP())
                        .with("Max KXP", profile.getMaxKXP())
                        .with("Level", profile.getLevel())
                        .with("Subtitle", profile.getSubtitle())
                        .with("Bio", profile.getBio())
                        .with("Playlists", profile.getPlaylists());
                KekBot.r.table("Profiles").insert(object).run(KekBot.conn);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //Settings settings = GSONUtils.getSettings(event.getGuild());
        /*KekBot.r.db("KekBot").table("Settings").insert(KekBot.r.hashMap("Guild ID", event.getGuild().getId())
                .with("Prefix", settings.getPrefix())
                .with("AutoRole ID", settings.getAutoRoleID())
                .with("Announce Settings", settings.announceSettings)
                .with("Tags", GSONUtils.getTagManager(event.getGuild()))
                .with("Quotes", GSONUtils.getQuotes(event.getGuild()).getQuotes())).run(KekBot.conn);*/
        //Settings settings = Settings.getSettings(event.getGuild());
        //settings.save();


        //Edit an existing array of strings
        //List<String> responses = KekBot.r.table("Responses").get("PURGE_SUCCESS").getField("Responses").run(KekBot.conn);
        //KekBot.r.db("KekBot").table("Responses").update(KekBot.r.hashMap("Action", "PURGE_SUCCESS").with("Responses", responses)).run(KekBot.conn);
    }
}
