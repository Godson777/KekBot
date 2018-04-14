package com.godson.kekbot.menu;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Menu;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.Checks;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ShopMenu extends Menu {

    private final Consumer<Message> finalAction;
    private final int pages;
    private final List<byte[]> images;
    private final int numberOfItems;
    private final int itemsPerPage;
    private final BiConsumer<Message, Integer> selectionAction;

    public static final String LEFT = "\u25C0";
    public static final String CANCEL = "❌";
    public static final String SELECTION_CANCEL = "❎";
    public static final String CHOOSE = "✅";
    public static final String RIGHT = "\u25B6";

    public static final String[] NUMBERS = new String[]{"1⃣", "2⃣", "3⃣", "4⃣", "5⃣", "6⃣", "7⃣", "8⃣", "9⃣", "\ud83d\udd1f"};


    protected ShopMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit, Consumer<Message> finalAction,
                       List<byte[]> images, int numberOfItems, int itemsPerPage, BiConsumer<Message, Integer> selectionAction) {
        super(waiter, users, roles, timeout, unit);
        this.finalAction = finalAction;
        this.pages = images.size();
        this.images = images;
        this.numberOfItems = numberOfItems;
        this.itemsPerPage = itemsPerPage;
        this.selectionAction = selectionAction;
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
        initialize(channel.sendFile(images.get(pageNum-1), "shop.png", null), pageNum);
    }

    private void paginate(Message message, int pageNum) {
        if(pageNum < 1)
            pageNum = 1;
        else if (pageNum > pages)
            pageNum = pages;
        initialize(message.getChannel().sendFile(images.get(pageNum-1), "shop.png", null), pageNum);
        message.delete().queue();
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
            if (newPageNum != pageNum) {
                paginate(message, newPageNum);
            } else pagination(message, newPageNum);
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
                        msg.addReaction(NUMBERS[i]).queue(aVoid -> msg.addReaction(SELECTION_CANCEL).queue((v) -> this.waitReaction(msg, pageNum)));
                    }
                }
            } catch (PermissionException var4) {
                this.waitReaction(msg, pageNum);
            }
        });
    }

    private void waitReaction(Message m, int pageNum) {
        this.waiter.waitForEvent(MessageReactionAddEvent.class, (e) -> {
            return this.isValidReaction(m, e, pageNum);
        }, (e) -> {
            if (e.getReaction().getReactionEmote().getName().equals(SELECTION_CANCEL)) {
                m.clearReactions().queue();
            } else {
                selectionAction.accept(m, this.getNumber(e.getReaction().getReactionEmote().getName()) + (itemsPerPage * (pageNum - 1)));
                m.delete().queue();
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

    public static class Builder extends Menu.Builder<ShopMenu.Builder, ShopMenu> {

        private Consumer<Message> finalAction = m -> m.delete().queue();
        private int numberOfItems;
        private int itemsPerPage;
        private BiConsumer<Message, Integer> selectionAction;

        private final List<byte[]> images = new LinkedList<>();


        @Override
        public ShopMenu build() {
            Checks.check(this.waiter != null, "Must set an EventWaiter");
            Checks.check(this.numberOfItems > 0, "Must have at least one item to select.");
            Checks.check(this.itemsPerPage <= 10, "Must have no more than ten choices per page.");
            Checks.check(this.selectionAction != null, "Must provide an selection consumer.");
            Checks.check(!this.images.isEmpty(), "Must include at least one item to paginate.");
            return new ShopMenu(waiter, users, roles, timeout, unit, finalAction, images, numberOfItems, itemsPerPage, selectionAction);
        }

        public ShopMenu.Builder setSelectionAction(BiConsumer<Message, Integer> selectionAction) {
            this.selectionAction = selectionAction;
            return this;
        }

        public ShopMenu.Builder setFinalAction(Consumer<Message> finalAction) {
            this.finalAction = finalAction;
            return this;
        }

        public ShopMenu.Builder clearItems() {
            images.clear();
            return this;
        }

        public ShopMenu.Builder addImages(List<byte[]> images) {
            this.images.addAll(images);
            return this;
        }

        public ShopMenu.Builder setImages(List<byte[]> images) {
            this.images.clear();
            this.images.addAll(images);
            return this;
        }

        public ShopMenu.Builder setNumberOfItems(int numberOfItems) {
            this.numberOfItems = numberOfItems;
            return this;
        }

        public ShopMenu.Builder setItemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }
    }
}
