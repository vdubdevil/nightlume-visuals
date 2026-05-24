package ru.nightlume.render.util;

import net.minecraft.client.gui.AbstractGui;
import com.mojang.blaze3d.matrix.MatrixStack;

public class RenderUtil {

    private static final MatrixStack STACK = new MatrixStack();

    public static void drawRect(float x,
                                float y,
                                float width,
                                float height,
                                int color) {

        AbstractGui.fill(
                STACK,
                (int) x,
                (int) y,
                (int) (x + width),
                (int) (y + height),
                color
        );
    }
}