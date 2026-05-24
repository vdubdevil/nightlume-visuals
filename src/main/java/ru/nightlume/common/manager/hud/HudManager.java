package ru.nightlume.common.manager.hud;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import ru.nightlume.Nightlume;
import ru.nightlume.api.event.Subscribe;
import ru.nightlume.api.event.impl.Render2DEvent;
import ru.nightlume.module.impl.system.ClickGuiModule;
import ru.nightlume.module.impl.system.HudEditorModule;
import ru.nightlume.ui.clickgui.hud.element.*;

import java.util.ArrayList;
import java.util.List;

public class HudManager {

    private final List<HudElement> elements = new ArrayList<>();

    public HudManager() {
        elements.add(new WatermarkElement());
        elements.add(new PotionsElement());
        elements.add(new InventoryElement());
        elements.add(new TargetHudElement());
        elements.add(new CoordinatesElement());
        elements.add(new PingElement());
        elements.add(new ArmorHudElement());
        elements.add(new HandHudElement());
    }

    // Uses your custom MCP Event System instead of Forge
    @Subscribe
    public void onRender2D(Render2DEvent event) {
        Minecraft mc = Minecraft.getInstance();

        // Prevent double-rendering when the HudEditor is open
        if (mc.currentScreen instanceof ru.nightlume.ui.clickgui.hud.HudEditorScreen) return;

        // Prevent rendering when the F3 Debug menu is open
        if (mc.gameSettings.showDebugInfo) return;

        ClickGuiModule clickGui = (ClickGuiModule) Nightlume.getInstance().getModuleManager().getModule(ClickGuiModule.class);
        String style = clickGui != null ? clickGui.style.getValue() : "Glassmorphism";

        for (HudElement element : elements) {
            if (isElementEnabled(element)) {
                element.render(event.getMatrixStack(), style);
            }
        }
    }

    public void renderElements(MatrixStack stack, String currentStyle) {
        for (HudElement element : elements) {
            if (isElementEnabled(element)) {
                element.render(stack, currentStyle);
            }
        }
    }

    public boolean isElementEnabled(HudElement element) {
        HudEditorModule editorMod = (HudEditorModule) Nightlume.getInstance().getModuleManager().getModule(HudEditorModule.class);
        if (editorMod == null) return false;

        String name = element.getName().toLowerCase();
        if (name.contains("watermark")) return editorMod.watermark.getValue();
        if (name.contains("potion")) return editorMod.potions.getValue();
        if (name.contains("inventory")) return editorMod.inventory.getValue();
        if (name.contains("target")) return editorMod.targetHud.getValue();
        if (name.contains("coordinate")) return editorMod.coordinates.getValue();
        if (name.contains("ping")) return editorMod.ping.getValue();
        if (name.contains("armor")) return editorMod.armorHud.getValue();
        if (name.contains("hand")) return editorMod.handHud.getValue();

        return true;
    }

    public List<HudElement> getElements() {
        return elements;
    }
}