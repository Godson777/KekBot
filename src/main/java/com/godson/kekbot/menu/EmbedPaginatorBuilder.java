package com.godson.kekbot.menu;

import com.jagrosh.jdautilities.menu.MenuBuilder;
import com.jagrosh.jdautilities.menu.pagination.PaginatorBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class EmbedPaginatorBuilder extends MenuBuilder<EmbedPaginatorBuilder, EmbedPaginator> {

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

    public EmbedPaginatorBuilder setColor(Color color) {
        this.color = color;
        return this;
    }

    public EmbedPaginatorBuilder setFinalAction(Consumer<Message> finalAction) {
        this.finalAction = finalAction;
        return this;
    }

    public EmbedPaginatorBuilder showPageNumbers(boolean show) {
        showPageNumbers = show;
        return this;
    }

     public EmbedPaginatorBuilder waitOnSinglePage(boolean wait) {
        waitOnSinglePage = wait;
        return this;
     }

     public EmbedPaginatorBuilder clearItems() {
        embeds.clear();
        return this;
     }

     public EmbedPaginatorBuilder addItems(MessageEmbed... embeds) {
        this.embeds.addAll(Arrays.asList(embeds));
        return this;
     }

     public EmbedPaginatorBuilder setItems(MessageEmbed... embeds) {
        this.embeds.clear();
        this.embeds.addAll(Arrays.asList(embeds));
        return this;
     }
}
