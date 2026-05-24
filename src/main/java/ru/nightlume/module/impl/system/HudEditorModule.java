package ru.nightlume.module.impl.system;

import ru.nightlume.module.Category;
import ru.nightlume.module.Module;
import ru.nightlume.module.setting.impl.BooleanSetting;

public class HudEditorModule extends Module {

    public final BooleanSetting watermark = new BooleanSetting("Watermark", true);
    public final BooleanSetting potions = new BooleanSetting("Potions", false);
    public final BooleanSetting inventory = new BooleanSetting("Inventory", false);
    public final BooleanSetting targetHud = new BooleanSetting("Target HUD", false);
    public final BooleanSetting coordinates = new BooleanSetting("Coordinates", false);
    public final BooleanSetting ping = new BooleanSetting("Ping", false);
    public final BooleanSetting armorHud = new BooleanSetting("Armor HUD", false);
    public final BooleanSetting handHud = new BooleanSetting("Hand HUD", false);

    public HudEditorModule() {
        super("Hud Editor", Category.SYSTEM);
        addSettings(watermark, potions, inventory, targetHud, ping, armorHud, handHud, coordinates);
    }
}