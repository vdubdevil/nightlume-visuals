package ru.nightlume.api.event.impl;

import ru.nightlume.api.event.Event;

public class KeyEvent extends Event {

    private final int key;

    public KeyEvent(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}