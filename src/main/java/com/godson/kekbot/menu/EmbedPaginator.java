package com.godson.kekbot.menu;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Menu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
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
        if (embed.getTitle() != null) ebuilder.setTitle(embed.getTitle());
        if (embed.getDescription() != null) ebuilder.setDescription(embed.getDescription());
        if (embed.getColor() == null) ebuilder.setColor(color);
        else ebuilder.setColor(embed.getColor());
        if (embed.getThumbnail() != null) ebuilder.setThumbnail(embed.getThumbnail().getUrl());
        if (embed.getTimestamp() != null) ebuilder.setTimestamp(embed.getTimestamp());
        if (embed.getAuthor() != null) ebuilder.setAuthor(embed.getAuthor().getName(), embed.getAuthor().getUrl(), embed.getAuthor().getIconUrl());

        if(showPageNumbers)
            ebuilder.setFooter("Page " + pageNum + "/" + pages + (embed.getFooter() != null ? " | " + embed.getFooter().getText() : "") , null);
        mbuilder.setEmbed(ebuilder.build());
        return mbuilder.build();
    }

    public static class Builder extends Menu.Builder<Builder, EmbedPaginator> {

        private Color color;
        private Consumer<Message> finalAction = m -> m.delete().queue();
        private boolean showPageNumbers = true;
        private boolean waitOnSinglePage = true;

        private final List<MessageEmbed> embeds = new LinkedList<>();


        @Override
        public EmbedPaginator build() {
            if (waiter == null) throw new IllegalArgumentException("Must set an EventWaiter.");
            if (embeds.isEmpty()) throw new IllegalArgumentException("Must include at least one item to paginate.");
            return new EmbedPaginator(waiter, users, roles, timeout, unit, color, finalAction, showPageNumbers, waitOnSinglePage, embeds);
        }

        public Builder setColor(Color color) {
            this.color = color;
            return this;
        }

        public Builder setFinalAction(Consumer<Message> finalAction) {
            this.finalAction = finalAction;
            return this;
        }

        public Builder showPageNumbers(boolean show) {
            showPageNumbers = show;
            return this;
        }

        public Builder waitOnSinglePage(boolean wait) {
            waitOnSinglePage = wait;
            return this;
        }

        public Builder clearItems() {
            embeds.clear();
            return this;
        }

        public Builder addItems(MessageEmbed... embeds) {
            this.embeds.addAll(Arrays.asList(embeds));
            return this;
        }

        public Builder setItems(MessageEmbed... embeds) {
            this.embeds.clear();
            this.embeds.addAll(Arrays.asList(embeds));
            return this;
        }
    }
}
