package com.godson.kekbot.menu;

import com.jagrosh.jdautilities.menu.Menu;
import com.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.RestAction;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EmbedPaginator extends Menu {


    private final Color color;
    private final Consumer<Message> finalAction;
    private final boolean showPageNumbers;
    private final boolean waitOnSinglePage;
    private final int pages;
    private final List<MessageEmbed> embeds;

    public static final String LEFT = "\u25C0";
    public static final String STOP = "\u23F9";
    public static final String RIGHT = "\u25B6";


    protected EmbedPaginator(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit, Color color, Consumer<Message> finalAction, boolean showPageNumbers, boolean waitOnSinglePage, List<MessageEmbed> embeds) {
        super(waiter, users, roles, timeout, unit);
        this.color = color;
        this.finalAction = finalAction;
        this.showPageNumbers = showPageNumbers;
        this.waitOnSinglePage = waitOnSinglePage;
        this.embeds = embeds;
        this.pages = embeds.size();
    }

    @Override
    public void display(MessageChannel channel) {
        paginate(channel, 1);
    }

    @Override
    public void display(Message message) {
        paginate(message, 1);
    }

    private void paginate(MessageChannel channel, int pageNum) {
        if(pageNum < 1)
            pageNum = 1;
        else if (pageNum > pages)
            pageNum = pages;
        Message msg = renderPage(pageNum);
        initialize(channel.sendMessage(msg), pageNum);
    }

    private void paginate(Message message, int pageNum) {
        if(pageNum < 1)
            pageNum = 1;
        else if (pageNum > pages)
            pageNum = pages;
        Message msg = renderPage(pageNum);
        initialize(message.editMessage(msg), pageNum);
    }


    private void initialize(RestAction<Message> action, int pageNum) {
        action.queue(m->{
            if(pages > 1) {
                m.addReaction(LEFT).queue();
                m.addReaction(STOP).queue();
                m.addReaction(RIGHT).queue(v -> pagination(m, pageNum), t -> pagination(m, pageNum));
            } else if(waitOnSinglePage) {
                m.addReaction(STOP).queue(v -> pagination(m, pageNum), t -> pagination(m, pageNum));
            } else {
                finalAction.accept(m);
            }
        });
    }

    private void pagination(Message message, int pageNum) {
        waiter.waitForEvent(MessageReactionAddEvent.class, (MessageReactionAddEvent event) -> {
            if(!event.getMessageId().equals(message.getId()))
                return false;
            if(!(LEFT.equals(event.getReactionEmote().getName())
                    || STOP.equals(event.getReactionEmote().getName())
                    || RIGHT.equals(event.getReactionEmote().getName())))
                return false;
            return isValidUser(event.getUser(), event.getGuild());
        }, event -> {
            int newPageNum = pageNum;
            switch(event.getReactionEmote().getName())
            {
                case LEFT:  if(newPageNum>1) newPageNum--; break;
                case RIGHT: if(newPageNum<pages) newPageNum++; break;
                case STOP: finalAction.accept(message); return;
            }
            try { event.getReaction().removeReaction(event.getUser()).queue(); } catch(PermissionException ignored) {}
            int n = newPageNum;
            message.editMessage(renderPage(newPageNum)).queue(m -> pagination(m, n));
        }, timeout, unit, () -> finalAction.accept(message));
    }

    private Message renderPage(int pageNum) {
        MessageBuilder mbuilder = new MessageBuilder();
        EmbedBuilder ebuilder = new EmbedBuilder();

        MessageEmbed embed = embeds.get(pageNum-1);
        embed.getFields().forEach(ebuilder::addField);
        if (embed.getColor() == null) ebuilder.setColor(color);
        else ebuilder.setColor(embed.getColor());
        if (embed.getThumbnail() != null) ebuilder.setThumbnail(embed.getThumbnail().getUrl());
        if (embed.getTimestamp() != null) ebuilder.setTimestamp(embed.getTimestamp());

        if(showPageNumbers)
            ebuilder.setFooter("Page " + pageNum + "/" + pages, null);
        mbuilder.setEmbed(ebuilder.build());
        return mbuilder.build();
    }
}
