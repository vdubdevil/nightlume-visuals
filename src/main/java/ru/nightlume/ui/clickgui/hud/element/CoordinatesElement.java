package ru.nightlume.ui.clickgui.hud.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;

public class CoordinatesElement extends HudElement {

    public CoordinatesElement() {
        super("Coordinates", 10, 95, 105, 20);
    }

    @Override
    public void render(MatrixStack stack, String style) {
        updateThemeVisibility(style);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float s = getScaleValue();
        float x = getX();
        float y = getY();

        String text = "XYZ: "
                + (int) mc.player.getPosX() + " "
                + (int) mc.player.getPosY() + " "
                + (int) mc.player.getPosZ();

        float baseW = mc.fontRenderer.getStringWidth(text) + 12;
        float baseH = 20;

        setWidth(baseW * s);
        setHeight(baseH * s);

        drawElementBackground(stack, style, x, y, baseW * s, baseH * s);

        stack.push();
        stack.translate(x, y, 0);
        stack.scale(s, s, 1.0f);
        mc.fontRenderer.drawStringWithShadow(stack, text, 6, 6, getResolvedTextColor());
        stack.pop();
    }
}