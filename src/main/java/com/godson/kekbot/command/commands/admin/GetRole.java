package com.godson.kekbot.command.commands.admin;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandEvent;
import com.godson.kekbot.settings.Settings;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.core.entities.Role;

import java.util.List;
import java.util.stream.Collectors;

public class GetRole extends Command {

    public GetRole() {
        name = "getrole";
        description = "Gives you a role from a list of available roles (if any)";
        usage.add("getrole list");
        usage.add("getrole <role>");
        category = new Category("Admin");
    }

    @Override
    public void onExecuted(CommandEvent event) {
        if (event.getArgs().length < 1) {
            event.getChannel().sendMessage("No arguments specified, check `" + event.getPrefix() + "help " + name + "` for more help.").queue();
            return;
        }

        Settings settings = Settings.getSettings(event.getGuild());
        String role = event.combineArgs();

        if (role.equalsIgnoreCase("list")) {
            Paginator.Builder builder = new Paginator.Builder();
            List<net.dv8tion.jda.core.entities.Role> roles = settings.getFreeRoles().stream().map(r -> event.getGuild().getRoleById(r)).collect(Collectors.toList());
            roles.forEach(r -> builder.addItems(r.getName()));
            builder.setEventWaiter(KekBot.waiter);
            builder.setFinalAction(m -> m.clearReactions().queue());
            builder.waitOnSinglePage(true);
            builder.setUsers(event.getAuthor());
            builder.setText("");
            builder.useNumberedItems(true);
            builder.build().display(event.getChannel());
            return;
        }

        List<Role> check = event.getGuild().getRolesByName(role, false);
        if (check.size() == 0) {
            event.getChannel().sendMessage("Unable to find any roles by the name of `" + role + "`!").queue();
            return;
        }

        if (!settings.getFreeRoles().contains(check.get(0).getId())) {
            event.getChannel().sendMessage("This isn't a role I can give to you.").queue();
            return;
        }

        if (event.getMember().getRoles().contains(check.get(0))) {
            event.getGuild().getController().removeRolesFromMember(event.getMember(), check.get(0)).reason("getrole command").queue();
            event.getChannel().sendMessage("Done, you no longer have the `" + role + "` role.").queue();
            return;
        }

        event.getGuild().getController().addRolesToMember(event.getMember(), check.get(0)).reason("getrole command").queue();
        event.getChannel().sendMessage("Done, you now have the `" + role + "` role.").queue();
    }
}
