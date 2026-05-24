package ru.nightlume.module.impl.themes;

import ru.nightlume.Nightlume;
import ru.nightlume.module.Category;
import ru.nightlume.module.Module;
import ru.nightlume.module.impl.system.ClickGuiModule;
import ru.nightlume.module.setting.impl.ColorSetting;
import ru.nightlume.module.setting.impl.NumberSetting;

import java.awt.Color;

public class ThemeModule extends Module {

    public final ColorSetting bgColor = new ColorSetting("Background Color", new Color(20, 20, 20, 200));
    public final ColorSetting outlineColor = new ColorSetting("Outline Color", new Color(40, 40, 40, 255));
    public final ColorSetting textColor = new ColorSetting("Text Color", new Color(255, 255, 255, 255));
    public final ColorSetting accentColor = new ColorSetting("Accent Color", new Color(80, 120, 255, 255));

    public final NumberSetting cornerRadius = new NumberSetting("Corner Radius", 4.0, 0.0, 5.0, 0.25);
    public final NumberSetting globalOpacity = new NumberSetting("Global Opacity", 1.0, 0.1, 1.0, 0.05);

    public final ColorSetting glassTint = new ColorSetting("Glass Tint", new Color(255, 255, 255, 40));
    public final NumberSetting blurStrength = new NumberSetting("Blur Strength", 4.0, 1.0, 5.0, 0.25);
    public final NumberSetting borderOpacity = new NumberSetting("Border Opacity", 0.3, 0.0, 1.0, 0.05);
    public final NumberSetting glowStrength = new NumberSetting("Glow Strength", 2.0, 0.0, 10.0, 0.5);

    public ThemeModule() {
        super("Global Theme", Category.HIDDEN);
        addSettings(bgColor, outlineColor, textColor, accentColor, cornerRadius, globalOpacity, glassTint, blurStrength, borderOpacity, glowStrength);
    }

    public void updateVisibility() {
        ClickGuiModule clickGui = (ClickGuiModule) Nightlume.getInstance().getModuleManager().getModule(ClickGuiModule.class);
        boolean isGlass = clickGui != null && clickGui.style.getValue().equalsIgnoreCase("Glassmorphism");
        boolean isClassic = clickGui != null && clickGui.style.getValue().equalsIgnoreCase("Classic");

        glassTint.setVisible(isGlass);
        blurStrength.setVisible(isGlass);
        borderOpacity.setVisible(isGlass);
        glowStrength.setVisible(isGlass);
        cornerRadius.setVisible(!isClassic);
    }

    public float getBlurRadius() { return (float) blurStrength.getValue(); }
    public float getBorderAlpha() { return (float) borderOpacity.getValue(); }
    public float getGlowRadius() { return (float) glowStrength.getValue(); }
}