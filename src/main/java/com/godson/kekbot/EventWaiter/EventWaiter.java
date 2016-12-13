package com.godson.kekbot.EventWaiter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class EventWaiter implements EventListener {

    private final HashMap<Class, List<WaitingEvent>> waitingEvents;

    public EventWaiter()
    {
        waitingEvents = new HashMap<>();
    }

    public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action)
    {
        List<WaitingEvent> list;
        if(waitingEvents.containsKey(classType))
            list = waitingEvents.get(classType);
        else
        {
            list = new ArrayList<>();
            waitingEvents.put(classType, list);
        }
        list.add(new WaitingEvent<>(condition, action));
    }

    @Override
    public final void onEvent(Event event)
    {
        if(waitingEvents.containsKey(event.getClass()))
        {
            List<WaitingEvent> list = waitingEvents.get(event.getClass());
            List<WaitingEvent> ulist = new ArrayList<>(list);
            list.removeAll(ulist.stream().filter(i -> i.attempt(event)).collect(Collectors.toList()));
        }
    }
}