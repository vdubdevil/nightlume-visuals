package ru.nightlume.ui.clickgui.hud.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import ru.nightlume.module.setting.impl.BooleanSetting;
import ru.nightlume.render.util.RoundedUtil;

public class InventoryElement extends HudElement {

    public final BooleanSetting showWhenEmpty = new BooleanSetting("Show When Empty", true);

    public InventoryElement() {
        super("Inventory HUD", 160, 40, 170, 74);
        addSettings(showWhenEmpty);
    }

    private boolean isInventoryEmpty(Minecraft mc) {
        for (int i = 9; i < 36; i++) {
            if (!mc.player.inventory.mainInventory.get(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public void render(MatrixStack stack, String style) {
        updateThemeVisibility(style);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean inEditor = mc.currentScreen instanceof ru.nightlume.ui.clickgui.hud.HudEditorScreen;
        if (!showWhenEmpty.getValue() && isInventoryEmpty(mc) && !inEditor) {
            setWidth(0);
            setHeight(0);
            return;
        }

        float s = getScaleValue();
        float x = getX();
        float y = getY();

        float baseW = 170;
        float baseH = 74;

        setWidth(baseW * s);
        setHeight(baseH * s);

        drawElementBackground(stack, style, x, y, baseW * s, baseH * s);

        stack.push();
        stack.translate(x, y, 0);
        stack.scale(s, s, 1.0f);
        mc.fontRenderer.drawStringWithShadow(stack, "Inventory", 4, 4, getResolvedTextColor());
        stack.pop();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                float slotX = 4 + col * 18;
                float slotY = 16 + row * 18;

                RoundedUtil.drawRound(x + (slotX + 1) * s, y + (slotY + 1) * s, 16 * s, 16 * s, 1.5f * s, 0x1AFFFFFF);

                int index = col + (row + 1) * 9;
                ItemStack item = mc.player.inventory.mainInventory.get(index);

                if (!item.isEmpty()) {
                    RenderSystem.pushMatrix();
                    RenderSystem.translatef(x, y, 0);
                    RenderSystem.scalef(s, s, 1.0f);
                    mc.getItemRenderer().renderItemAndEffectIntoGUI(item, (int) slotX, (int) slotY);
                    mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, item, (int) slotX, (int) slotY);
                    RenderSystem.popMatrix();
                }
            }
        }
    }
}