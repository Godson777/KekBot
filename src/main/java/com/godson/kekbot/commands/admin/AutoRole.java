package com.godson.kekbot.commands.admin;

import com.darichey.discord.api.Command;
import com.darichey.discord.api.CommandCategory;
import com.darichey.discord.api.FailureReason;
import com.godson.kekbot.GSONUtils;
import com.godson.kekbot.Settings.Settings;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;

public class AutoRole {
    public static Command autoRole = new Command("autorole")
            .withCategory(CommandCategory.ADMIN)
            .withDescription("Allows you to set a role in which KekBot will assign to all newcomers. You can also RESET this setting, disabling it.")
            .withUsage("{p}autorole <role | reset>")
            .userRequiredPermissions(Permission.MANAGE_ROLES)
            .onExecuted(context -> {
                String rawSplit[] = context.getMessage().getRawContent().split(" ", 2);
                TextChannel channel = context.getTextChannel();
                Guild guild = context.getGuild();
                Settings settings = GSONUtils.getSettings(guild);
                    if (rawSplit.length == 1) {
                        channel.sendMessageAsync("Which role am I gonna automatically give newcomers? :neutral_face:", null);
                    } else {
                        if (guild.getRolesByName(rawSplit[1]).size() == 0) {
                            channel.sendMessageAsync("Unable to find any roles by the name of \"" + rawSplit[1] + "\"!", null);
                        } else {
                            settings.setAutoRoleID(guild.getRolesByName(rawSplit[1]).get(0).getId());
                            channel.sendMessageAsync("Got it! I will now give newcomers the role \"" + guild.getRolesByName(rawSplit[1]).get(0).getName() + "\"!", null);
                        }
                    }
            })
            .onFailure((context, reason) -> {
                if (reason.equals(FailureReason.AUTHOR_MISSING_PERMISSIONS))
                    context.getTextChannel().sendMessageAsync(context.getMessage().getAuthor().getAsMention() + ", you do not have the `Manage Roles` permission!", null);
            });
}
