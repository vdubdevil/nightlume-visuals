package ru.nightlume.api.event.impl;

import ru.nightlume.api.event.Event;

public class ChatReceiveEvent extends Event {

    private final String message;

    public ChatReceiveEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}