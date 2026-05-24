package ru.nightlume.ui.clickgui.hud.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import ru.nightlume.render.util.RenderUtil;
import ru.nightlume.render.util.RoundedUtil;

public class TargetHudElement extends HudElement {

    public TargetHudElement() {
        super("Target HUD", 10, 150, 150, 45);
    }

    @Override
    public void render(MatrixStack stack, String style) {
        updateThemeVisibility(style);

        Minecraft mc = Minecraft.getInstance();
        LivingEntity target = null;
        boolean inEditor = mc.currentScreen instanceof ru.nightlume.ui.clickgui.hud.HudEditorScreen;

        if (mc.pointedEntity instanceof LivingEntity) {
            target = (LivingEntity) mc.pointedEntity;
        } else if (inEditor) {
            target = mc.player;
        }

        if (target == null) return;
        if (!inEditor && target.isInvisible()) return;

        float s = getScaleValue();
        float x = getX();
        float y = getY();

        float baseW = 150;
        float baseH = 45;

        setWidth(baseW * s);
        setHeight(baseH * s);

        drawElementBackground(stack, style, x, y, baseW * s, baseH * s);

        RoundedUtil.drawRound(x + 5 * s, y + 5 * s, 35 * s, 35 * s, getResolvedRadius(), 0x33FFFFFF);

        String name = target.getName().getString();
        String health = String.format("%.1f / %.1f", target.getHealth(), target.getMaxHealth());
        float healthPercent = Math.min(1.0f, Math.max(0.0f, target.getHealth() / target.getMaxHealth()));

        stack.push();
        stack.translate(x, y, 0);
        stack.scale(s, s, 1.0f);
        mc.fontRenderer.drawStringWithShadow(stack, name, 46, 6, getResolvedTextColor());
        mc.fontRenderer.drawStringWithShadow(stack, health, 46, 18, 0xFFAAAAAA);
        stack.pop();

        RenderUtil.drawRect(x + 46 * s, y + 30 * s, 96 * s, 6 * s, 0xFF222222);
        RenderUtil.drawRect(x + 46 * s, y + 30 * s, (int) (96 * s * healthPercent), 6 * s, getResolvedAccentColor());
    }
}