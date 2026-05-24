package ru.nightlume.ui.clickgui.hud.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import ru.nightlume.module.setting.impl.ModeSetting;
import ru.nightlume.render.util.RoundedUtil;

public class WatermarkElement extends HudElement {

    public final ModeSetting styleMode = new ModeSetting("Version", "v1", "v1", "v2");

    public WatermarkElement() {
        super("Watermark", 10, 10, 160, 20);
        addSettings(styleMode);
    }

    @Override
    public void render(MatrixStack stack, String style) {
        updateThemeVisibility(style);

        Minecraft mc = Minecraft.getInstance();

        float s = getScaleValue();
        float x = getX();
        float y = getY();

        String fps = mc.debug.split(" ")[0];
        boolean v1 = styleMode.is("v1");
        String text = v1 ? "NIGHTLUME | FPS: " + fps : "nightlume.pro   " + fps;

        float baseW = mc.fontRenderer.getStringWidth(text) + (v1 ? 12 : 24);
        float baseH = v1 ? 20 : 22;

        setWidth(baseW * s);
        setHeight(baseH * s);

        drawElementBackground(stack, style, x, y, baseW * s, baseH * s);

        stack.push();
        stack.translate(x, y, 0);
        stack.scale(s, s, 1.0f);

        if (v1) {
            mc.fontRenderer.drawStringWithShadow(stack, text, 6, 6, getResolvedTextColor());
        } else {
            stack.pop();

            RoundedUtil.drawRound(
                    x + 6 * s,
                    y + 7 * s,
                    8 * s,
                    8 * s,
                    2 * s,
                    getResolvedAccentColor()
            );

            stack.push();
            stack.translate(x, y, 0);
            stack.scale(s, s, 1.0f);
            mc.fontRenderer.drawStringWithShadow(stack, text, 18, 7, getResolvedTextColor());
        }

        stack.pop();
    }
}