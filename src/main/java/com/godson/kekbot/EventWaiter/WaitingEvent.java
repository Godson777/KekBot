package com.godson.kekbot.EventWaiter;

import net.dv8tion.jda.core.events.Event;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * @author John Grosh (jagrosh)
 * @param <T> the type of event
 */
public class WaitingEvent<T extends Event> {
    final private Predicate<T> condition;
    final private Consumer<T> action;

    public WaitingEvent(Predicate<T> condition, Consumer<T> action)
    {
        this.condition = condition;
        this.action = action;
    }

    public boolean attempt(T event)
    {
        if(condition.test(event))
        {
            action.accept(event);
            return true;
        }
        return false;
    }
}