package ru.nightlume.ui.clickgui.hud.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import ru.nightlume.module.setting.impl.ModeSetting;
import ru.nightlume.render.util.RoundedUtil;

public class ArmorHudElement extends HudElement {

    public final ModeSetting layout = new ModeSetting("Layout", "Horizontal", "Horizontal", "Vertical");
    public final ModeSetting horizontalDurability = new ModeSetting("Durability", "None", "None", "Percent");
    public final ModeSetting verticalDurability = new ModeSetting("Durability", "None", "None", "Percent", "Text");

    public ArmorHudElement() {
        super("Armor HUD", 10, 120, 110, 32);
        addSettings(layout, horizontalDurability, verticalDurability);
    }

    @Override
    public void render(MatrixStack stack, String style) {
        updateThemeVisibility(style);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean vertical = layout.is("Vertical");

        horizontalDurability.setVisible(!vertical);
        verticalDurability.setVisible(vertical);

        ModeSetting durability = vertical ? verticalDurability : horizontalDurability;
        boolean showDurability = !durability.is("None");

        float s = getScaleValue();
        float x = getX();
        float y = getY();

        float baseW;
        float baseH;

        if (vertical) {
            baseW = showDurability ? 82 : 32;
            baseH = 110;
        } else {
            baseW = 110;
            baseH = showDurability ? 44 : 32;
        }

        setWidth(baseW * s);
        setHeight(baseH * s);

        drawElementBackground(stack, style, x, y, baseW * s, baseH * s);

        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0.0f);
        RenderSystem.scalef(s, s, 1.0f);

        for (int armorIndex = 3; armorIndex >= 0; armorIndex--) {
            int visualIndex = 3 - armorIndex;
            ItemStack item = mc.player.inventory.armorInventory.get(armorIndex);

            int slotX = vertical ? 6 : 6 + visualIndex * 26;
            int slotY = vertical ? 6 + visualIndex * 26 : 6;

            RoundedUtil.drawRound(slotX, slotY, 20, 20, 2, 0x22FFFFFF);

            if (!item.isEmpty()) {
                mc.getItemRenderer().renderItemAndEffectIntoGUI(item, slotX + 2, slotY + 2);
                mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, item, slotX + 2, slotY + 2);

                if (showDurability && item.isDamageable()) {
                    String value = getDurabilityText(item, vertical, durability);

                    if (vertical) {
                        mc.fontRenderer.drawStringWithShadow(stack, value, 32, slotY + 6, getResolvedTextColor());
                    } else {
                        int textWidth = mc.fontRenderer.getStringWidth(value);
                        int textX = slotX + 10 - textWidth / 2;
                        mc.fontRenderer.drawStringWithShadow(stack, value, textX, 29, getResolvedTextColor());
                    }
                }
            }
        }

        RenderSystem.popMatrix();
    }

    private String getDurabilityText(ItemStack item, boolean vertical, ModeSetting durability) {
        int max = item.getMaxDamage();
        int left = max - item.getDamage();

        if (max <= 0) {
            return "100%";
        }

        int percent = (int) ((left / (float) max) * 100.0f);

        if (vertical && durability.is("Text")) {
            return left + "/" + max;
        }

        return percent + "%";
    }
}