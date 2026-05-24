package ru.nightlume.ui.clickgui.hud.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import ru.nightlume.module.setting.impl.BooleanSetting;

import java.util.ArrayList;
import java.util.List;

public class PotionsElement extends HudElement {

    public final BooleanSetting splitTimer = new BooleanSetting("Split Timer", true);

    public PotionsElement() {
        super("Potions HUD", 10, 40, 140, 30);
        addSettings(splitTimer);
    }

    @Override
    public void render(MatrixStack stack, String style) {
        updateThemeVisibility(style);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        List<EffectInstance> effects = new ArrayList<>(mc.player.getActivePotionEffects());

        if (effects.isEmpty() && mc.currentScreen instanceof ru.nightlume.ui.clickgui.hud.HudEditorScreen) {
            effects.add(new EffectInstance(Effects.SPEED, 1200, 1));
        }

        float s = getScaleValue();
        float x = getX();
        float y = getY();

        float relY = 0;
        float layoutWidth = splitTimer.getValue() ? 140 : 100;

        for (EffectInstance effect : effects) {
            String name = effect.getPotion().getDisplayName().getString();

            int seconds = Math.max(0, effect.getDuration() / 20);
            String duration = String.format("%02d:%02d", seconds / 60, seconds % 60);

            if (splitTimer.getValue()) {
                drawElementBackground(stack, style, x, y + relY * s, 100 * s, 24 * s);
                drawElementBackground(stack, style, x + 104 * s, y + relY * s, 36 * s, 24 * s);

                stack.push();
                stack.translate(x, y + relY * s, 0);
                stack.scale(s, s, 1.0f);
                mc.fontRenderer.drawStringWithShadow(stack, name, 6, 8, getResolvedTextColor());
                mc.fontRenderer.drawStringWithShadow(stack, duration, 110, 8, 0xFFAAAAAA);
                stack.pop();
            } else {
                String text = name + " [" + duration + "]";

                drawElementBackground(stack, style, x, y + relY * s, 100 * s, 24 * s);

                stack.push();
                stack.translate(x, y + relY * s, 0);
                stack.scale(s, s, 1.0f);
                mc.fontRenderer.drawStringWithShadow(stack, text, 6, 8, getResolvedTextColor());
                stack.pop();
            }

            relY += 28;
        }

        setWidth(layoutWidth * s);
        setHeight(Math.max(24 * s, (relY - 4) * s));
    }
}