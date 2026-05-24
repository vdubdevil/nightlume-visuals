package ru.nightlume.ui.clickgui.hud.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import ru.nightlume.module.setting.impl.BooleanSetting;

public class HandHudElement extends HudElement {

    public final BooleanSetting showWhenEmpty = new BooleanSetting("Show When Empty", true);

    public HandHudElement() {
        super("Hand HUD", 100, 120, 48, 24);
        addSettings(showWhenEmpty);
    }

    @Override
    public void render(MatrixStack stack, String style) {
        updateThemeVisibility(style);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack mainHand = mc.player.getHeldItemMainhand();
        ItemStack offHand = mc.player.getHeldItemOffhand();

        boolean empty = mainHand.isEmpty() && offHand.isEmpty();
        boolean inEditor = mc.currentScreen instanceof ru.nightlume.ui.clickgui.hud.HudEditorScreen;

        if (!showWhenEmpty.getValue() && empty && !inEditor) {
            setWidth(0);
            setHeight(0);
            return;
        }

        float s = getScaleValue();
        float x = getX();
        float y = getY();

        float baseW = 48;
        float baseH = 24;

        setWidth(baseW * s);
        setHeight(baseH * s);

        drawElementBackground(stack, style, x, y, baseW * s, baseH * s);

        RenderSystem.pushMatrix();
        RenderSystem.translatef(x + 6, y + 4, 0);
        RenderSystem.scalef(s, s, 1.0f);

        if (!mainHand.isEmpty()) {
            mc.getItemRenderer().renderItemAndEffectIntoGUI(mainHand, 0, 0);
            mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, mainHand, 0, 0);
        }

        if (!offHand.isEmpty()) {
            mc.getItemRenderer().renderItemAndEffectIntoGUI(offHand, 22, 0);
            mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, offHand, 22, 0);
        }

        RenderSystem.popMatrix();
    }
}