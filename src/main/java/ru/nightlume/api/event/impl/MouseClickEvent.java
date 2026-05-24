package ru.nightlume.api.event.impl;

import ru.nightlume.api.event.Event;

public class MouseClickEvent extends Event {

    private final int button;

    public MouseClickEvent(int button) {
        this.button = button;
    }

    public int getButton() {
        return button;
    }
}