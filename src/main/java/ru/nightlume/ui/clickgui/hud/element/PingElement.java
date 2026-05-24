package ru.nightlume.ui.clickgui.hud.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import ru.nightlume.module.setting.impl.BooleanSetting;

public class PingElement extends HudElement {

    public final BooleanSetting coloredPing = new BooleanSetting("Colored Ping", true);

    public PingElement() {
        super("Ping", 10, 70, 75, 20);
        addSettings(coloredPing);
    }

    @Override
    public void render(MatrixStack stack, String style) {
        updateThemeVisibility(style);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;

        NetworkPlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUniqueID());
        int ping = info != null ? info.getResponseTime() : 0;

        float s = getScaleValue();
        float x = getX();
        float y = getY();

        String label = "Ping: ";
        String value = ping + "ms";

        float baseW = mc.fontRenderer.getStringWidth(label + value) + 12;
        float baseH = 20;

        setWidth(baseW * s);
        setHeight(baseH * s);

        drawElementBackground(stack, style, x, y, baseW * s, baseH * s);

        int pingColor = coloredPing.getValue() ? getPingColor(ping) : getResolvedTextColor();

        stack.push();
        stack.translate(x, y, 0);
        stack.scale(s, s, 1.0f);
        mc.fontRenderer.drawStringWithShadow(stack, label, 6, 6, getResolvedTextColor());
        mc.fontRenderer.drawStringWithShadow(stack, value, 6 + mc.fontRenderer.getStringWidth(label), 6, pingColor);
        stack.pop();
    }

    private int getPingColor(int ping) {
        if (ping <= 60) return 0xFF55FF55;
        if (ping <= 120) return 0xFFFFFF55;
        if (ping <= 200) return 0xFFFFAA00;
        return 0xFFFF5555;
    }
}