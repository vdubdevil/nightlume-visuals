package ru.nightlume.api.event.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import ru.nightlume.api.event.Event;

public class Render2DEvent extends Event {

    private final MatrixStack matrixStack;
    private final float partialTicks;

    public Render2DEvent(MatrixStack matrixStack, float partialTicks) {
        this.matrixStack = matrixStack;
        this.partialTicks = partialTicks;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}