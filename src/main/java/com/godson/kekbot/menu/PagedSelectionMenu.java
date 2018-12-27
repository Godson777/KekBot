package com.godson.kekbot.menu;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Menu;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.Checks;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class PagedSelectionMenu extends Menu {

    private final Color color;
    private final BiFunction<Integer, Integer, String> text;
    private final Consumer<Message> finalAction;
    private final int pages;
    private final boolean showPageNumbers;
    private final List<String> choices;
    private final int numberOfItems;
    private final int itemsPerPage;
    private final BiConsumer<Message, Integer> selectionAction;

    public static final String LEFT = "\u25C0";
    public static final String CANCEL = "❌";
    public static final String SELECTION_CANCEL = "❎";
    public static final String CHOOSE = "✅";
    public static final String RIGHT = "\u25B6";

    public static final String[] NUMBERS = new String[]{"1⃣", "2⃣", "3⃣", "4⃣", "5⃣", "6⃣", "7⃣", "8⃣", "9⃣", "\ud83d\udd1f"};


    protected PagedSelectionMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, BiFunction<Integer, Integer, String> text, long timeout, TimeUnit unit, Consumer<Message> finalAction,
                                 List<String> choices, int itemsPerPage, BiConsumer<Message, Integer> selectionAction, Color color, boolean showPageNumbers) {
        super(waiter, users, roles, timeout, unit);
        this.finalAction = finalAction;
        this.choices = choices;
        this.text = text;
        this.pages = (int)Math.ceil((double)this.choices.size() / (double)itemsPerPage);
        this.numberOfItems = choices.size();
        this.itemsPerPage = itemsPerPage;
        this.selectionAction = selectionAction;
        this.color = color;
        this.showPageNumbers = showPageNumbers;
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
        initialize(channel.sendMessage(renderPage(pageNum)), pageNum);
    }

    private void paginate(Message message, int pageNum) {
        if(pageNum < 1)
            pageNum = 1;
        else if (pageNum > pages)
            pageNum = pages;
        Message msg = renderPage(pageNum);
        initialize(message.editMessage(msg), pageNum);
    }

    private Message renderPage(int pageNum) {
        MessageBuilder mbuilder = new MessageBuilder();
        EmbedBuilder ebuilder = new EmbedBuilder();
        int start = (pageNum - 1) * this.itemsPerPage;
        int end = this.choices.size() < pageNum * this.itemsPerPage ? this.choices.size() : pageNum * this.itemsPerPage;
        int k;
            StringBuilder sbuilder = new StringBuilder();

            for(k = start; k < end; ++k) {
                sbuilder.append("\n").append("`" + (k + 1) + ".` ").append(this.choices.get(k));
            }

            ebuilder.setDescription(sbuilder.toString());

        ebuilder.setColor(color);
        if (this.showPageNumbers) {
            ebuilder.setFooter("Page " + pageNum + "/" + this.pages, null);
        }

        if (this.text != null) {
            mbuilder.append(this.text.apply(pageNum, this.pages));
        }

        mbuilder.setEmbed(ebuilder.build());

        return mbuilder.build();
    }

    private void initialize(RestAction<Message> action, int pageNum) {
        action.queue(m-> {
            m.addReaction(LEFT).queue();
            m.addReaction(CHOOSE).queue();
            m.addReaction(CANCEL).queue();
            m.addReaction(RIGHT).queue(v -> pagination(m, pageNum), t -> pagination(m, pageNum));
        });
    }

    private void pagination(Message message, int pageNum) {
        waiter.waitForEvent(MessageReactionAddEvent.class, (MessageReactionAddEvent event) -> {
            if (!event.getMessageId().equals(message.getId()))
                return false;
            if (!(LEFT.equals(event.getReactionEmote().getName())
                    || CANCEL.equals(event.getReactionEmote().getName())
                    || CHOOSE.equals(event.getReactionEmote().getName())
                    || RIGHT.equals(event.getReactionEmote().getName())))
                return false;
            return isValidUser(event.getUser(), event.getGuild());
        }, event -> {
            int newPageNum = pageNum;
            switch (event.getReactionEmote().getName()) {
                case LEFT:
                    if (newPageNum > 1) newPageNum--;
                    break;
                case RIGHT:
                    if (newPageNum < pages) newPageNum++;
                    break;
                case CHOOSE:
                    initializeSelection(message, pageNum);
                    return;
                case CANCEL:
                    finalAction.accept(message);
                    return;
            }
            try {
                event.getReaction().removeReaction(event.getUser()).queue();
            } catch (PermissionException ignored) {}
                paginate(message, newPageNum);
        }, timeout, unit, () -> finalAction.accept(message));
    }

    private int getItemsInPage(int pageNum) {
        int items = numberOfItems;
        for (int i = 0; i < pageNum - 1; i++) {
            items -= itemsPerPage;
        }
        if (items > itemsPerPage) return itemsPerPage;
        else return items;
    }

    private void initializeSelection(Message msg, int pageNum) {
        msg.clearReactions().queue((void_) -> {
            try {
                for(int i = 0; i < getItemsInPage(pageNum); ++i) {
                    if (i < getItemsInPage(pageNum) - 1) {
                        msg.addReaction(NUMBERS[i]).queue();
                    } else {
                        msg.addReaction(NUMBERS[i]).queue(v2 -> msg.addReaction(SELECTION_CANCEL).queue((v) -> this.waitReaction(msg, pageNum)));
                    }
                }
            } catch (PermissionException var4) {
                this.waitReaction(msg, pageNum);
            }
        });
    }

    private void waitReaction(Message m, int pageNum) {
        this.waiter.waitForEvent(MessageReactionAddEvent.class, (e) -> {
            this.finalAction.accept(m);
            return this.isValidReaction(m, e, pageNum);
        }, (e) -> {
            if (e.getReaction().getReactionEmote().getName().equals(SELECTION_CANCEL)) {
                m.clearReactions().queue();
            } else {
                selectionAction.accept(m, this.getNumber(e.getReaction().getReactionEmote().getName()) + (itemsPerPage * (pageNum - 1)));
            }

        }, this.timeout, this.unit, () -> {
            this.finalAction.accept(m);
        });
    }

    private boolean isValidReaction(Message m, MessageReactionAddEvent e, int pageNum) {
        if (!e.getMessageId().equals(m.getId())) {
            return false;
        } else if (!this.isValidUser(e.getUser(), e.getGuild())) {
            return false;
        } else if (e.getReaction().getReactionEmote().getName().equals(SELECTION_CANCEL)) {
            return true;
        } else {
            int num = this.getNumber(e.getReaction().getReactionEmote().getName());
            return num >= 0 && num <= getItemsInPage(pageNum);
        }
    }

    private int getNumber(String emoji) {
        for(int i = 0; i < NUMBERS.length; ++i) {
            if (NUMBERS[i].equals(emoji)) {
                return i + 1;
            }
        }

        return -1;
    }

    public static class Builder extends Menu.Builder<PagedSelectionMenu.Builder, PagedSelectionMenu> {

        private Consumer<Message> finalAction = m -> m.delete().queue();
        private int itemsPerPage;
        private BiConsumer<Message, Integer> selectionAction;
        private Color color;
        private BiFunction<Integer, Integer, String> text = (page, pages) -> "";
        private boolean showPageNumbers;

        private final List<String> choices = new LinkedList<>();


        @Override
        public PagedSelectionMenu build() {
            Checks.check(this.waiter != null, "Must set an EventWaiter");
            Checks.check(this.itemsPerPage <= 10, "Must have no more than ten choices per page.");
            Checks.check(this.selectionAction != null, "Must provide an selection consumer.");
            Checks.check(!this.choices.isEmpty(), "Must include at least one item to paginate.");
            return new PagedSelectionMenu(waiter, users, roles, text, timeout, unit, finalAction, choices, itemsPerPage, selectionAction, color, showPageNumbers);
        }

        public PagedSelectionMenu.Builder setSelectionAction(BiConsumer<Message, Integer> selectionAction) {
            this.selectionAction = selectionAction;
            return this;
        }

        public PagedSelectionMenu.Builder setText(String text) {
            this.text = (i0, i1) -> text;
            return this;
        }

        public PagedSelectionMenu.Builder setText(BiFunction<Integer, Integer, String> textBiFunction) {
            this.text = textBiFunction;
            return this;
        }

        public PagedSelectionMenu.Builder setFinalAction(Consumer<Message> finalAction) {
            this.finalAction = finalAction;
            return this;
        }

        public PagedSelectionMenu.Builder clearItems() {
            choices.clear();
            return this;
        }

        public PagedSelectionMenu.Builder showPageNumbers(boolean show) {
            showPageNumbers = show;
            return this;
        }

        public PagedSelectionMenu.Builder setColor(Color color) {
            this.color = color;
            return this;
        }

        public PagedSelectionMenu.Builder addChoices(String... choices) {
            this.choices.addAll(Arrays.asList(choices));
            return this;
        }

        public PagedSelectionMenu.Builder setChoices(String... choices) {
            this.choices.clear();
            this.choices.addAll(Arrays.asList(choices));
            return this;
        }

        public PagedSelectionMenu.Builder setItemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }
    }
}
