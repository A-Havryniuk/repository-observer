package com.epam.rd.autocode.observer.git;

import java.util.List;

public class WebHookImpl implements WebHook{
    private final String branchName;
    private final Event.Type eventType;
    private final List<Event> events;

    public WebHookImpl(String branchName, Event.Type eventType, List<Event> events) {
        this.branchName = branchName;
        this.eventType = eventType;
        this.events = events;
    }

    @Override
    public String branch() {
        return branchName;
    }

    @Override
    public Event.Type type() {
        return eventType;
    }

    @Override
    public List<Event> caughtEvents() {
        return events;
    }

    @Override
    public void onEvent(Event event) {
        events.add(event);
    }
}
