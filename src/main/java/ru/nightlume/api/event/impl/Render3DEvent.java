package ru.nightlume.api.event.impl;

import ru.nightlume.api.event.Event;

public class Render3DEvent extends Event {

    private final float partialTicks;

    public Render3DEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}