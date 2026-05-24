package ru.nightlume.ui.clickgui.hud.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import ru.nightlume.Nightlume;
import ru.nightlume.module.impl.themes.ThemeModule;
import ru.nightlume.module.setting.Setting;
import ru.nightlume.module.setting.impl.BooleanSetting;
import ru.nightlume.module.setting.impl.ColorSetting;
import ru.nightlume.module.setting.impl.NumberSetting;
import ru.nightlume.render.util.BlurUtil;
import ru.nightlume.render.util.RoundedUtil;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public abstract class HudElement {

    private final String name;
    private float x;
    private float y;
    private float width;
    private float height;

    private final List<Setting> settings = new ArrayList<>();

    public final BooleanSetting themeSync = new BooleanSetting("Theme Sync", true);

    public final NumberSetting scale = new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1);

    public final ColorSetting customBg = new ColorSetting("Background Color", new Color(20, 20, 20, 170));
    public final ColorSetting customOutline = new ColorSetting("Outline Color", new Color(40, 40, 40, 255));
    public final ColorSetting customText = new ColorSetting("Text Color", new Color(255, 255, 255, 255));
    public final ColorSetting customAccent = new ColorSetting("Accent Color", new Color(80, 120, 255, 255));

    public final NumberSetting customRadius = new NumberSetting("Corner Radius", 4.0, 0.0, 8.0, 0.25);
    public final NumberSetting customOpacity = new NumberSetting("Opacity", 1.0, 0.1, 1.0, 0.05);

    public final ColorSetting customGlassTint = new ColorSetting("Glass Tint", new Color(255, 255, 255, 40));
    public final NumberSetting customBlurStrength = new NumberSetting("Blur Strength", 4.0, 1.0, 5.0, 0.25);
    public final NumberSetting customBorderOpacity = new NumberSetting("Border Opacity", 0.3, 0.0, 1.0, 0.05);

    public HudElement(String name, float x, float y, float width, float height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        addSettings(
                themeSync,
                scale,
                customBg,
                customOutline,
                customText,
                customAccent,
                customRadius,
                customOpacity,
                customGlassTint,
                customBlurStrength,
                customBorderOpacity
        );
    }

    public abstract void render(MatrixStack stack, String style);

    protected void updateThemeVisibility(String style) {
        boolean custom = !themeSync.getValue();
        boolean glass = style != null && style.equalsIgnoreCase("Glassmorphism");

        customBg.setVisible(custom);
        customOutline.setVisible(custom);
        customText.setVisible(custom);
        customAccent.setVisible(custom);
        customRadius.setVisible(custom);
        customOpacity.setVisible(custom);

        customGlassTint.setVisible(custom && glass);
        customBlurStrength.setVisible(custom && glass);
        customBorderOpacity.setVisible(custom && glass);
    }

    protected void updateThemeVisibility() {
        updateThemeVisibility("");
    }

    protected void drawElementBackground(MatrixStack stack, String style, float x, float y, float width, float height) {
        int bgColor = getResolvedBgColor();
        float radius = getResolvedRadius();

        if (style == null) {
            style = "Classic";
        }

        switch (style.toLowerCase()) {
            case "rounded":
                drawRoundedFrame(x, y, width, height, radius, bgColor, getResolvedOutlineColor());
                break;

            case "glassmorphism":
                float blurStrength = getResolvedBlurStrength();
                int tint = getResolvedGlassTint();

                BlurUtil.drawBlurredBackground(x, y, width, height, blurStrength);
                drawRoundedFrame(x, y, width, height, radius, tint, getResolvedGlassBorderColor());
                break;

            case "minimalism":
            case "classic":
            default:
                drawSolidRect(x, y, x + width, y + height, bgColor);
                drawOutline(x, y, width, height, getResolvedOutlineColor());
                break;
        }
    }

    protected void drawOutline(float x, float y, float width, float height, int color) {
        drawSolidRect(x, y, x + width, y + 1, color);
        drawSolidRect(x, y + height - 1, x + width, y + height, color);
        drawSolidRect(x, y, x + 1, y + height, color);
        drawSolidRect(x + width - 1, y, x + width, y + height, color);
    }

    protected int getResolvedBgColor() {
        if (themeSync.getValue()) {
            return applyGlobalOpacity(Nightlume.getInstance().getThemeManager().getBackgroundColor());
        }

        return applyCustomOpacity(customBg.getValue().getRGB());
    }

    protected int getResolvedOutlineColor() {
        if (themeSync.getValue()) {
            return applyGlobalOpacity(Nightlume.getInstance().getThemeManager().getOutlineColor());
        }

        return applyCustomOpacity(customOutline.getValue().getRGB());
    }

    protected int getResolvedTextColor() {
        if (themeSync.getValue()) {
            return Nightlume.getInstance().getThemeManager().getTextColor();
        }

        return customText.getValue().getRGB();
    }

    protected int getResolvedAccentColor() {
        if (themeSync.getValue()) {
            return Nightlume.getInstance().getThemeManager().getAccentColor();
        }

        return customAccent.getValue().getRGB();
    }

    protected int getResolvedGlassTint() {
        if (themeSync.getValue()) {
            return applyGlobalOpacity(Nightlume.getInstance().getThemeManager().getBlurTint());
        }

        return applyCustomOpacity(customGlassTint.getValue().getRGB());
    }

    protected int getResolvedGlassBorderColor() {
        int base = getResolvedOutlineColor();
        float alpha;

        if (themeSync.getValue()) {
            ThemeModule theme = (ThemeModule) Nightlume.getInstance().getModuleManager().getModule(ThemeModule.class);
            alpha = theme != null ? (float) theme.borderOpacity.getValue() : 0.3f;
        } else {
            alpha = (float) customBorderOpacity.getValue();
        }

        int a = (int) (alpha * 255.0f);
        return (base & 0x00FFFFFF) | (a << 24);
    }

    protected float getResolvedBlurStrength() {
        if (themeSync.getValue()) {
            ThemeModule theme = (ThemeModule) Nightlume.getInstance().getModuleManager().getModule(ThemeModule.class);
            return theme != null ? (float) theme.blurStrength.getValue() : 4.0f;
        }

        return (float) customBlurStrength.getValue();
    }

    protected float getResolvedRadius() {
        if (themeSync.getValue()) {
            return Nightlume.getInstance().getThemeManager().getCornerRadius();
        }

        return (float) customRadius.getValue();
    }

    protected float getScaleValue() {
        return (float) scale.getValue();
    }

    private int applyGlobalOpacity(int color) {
        float opacity = Nightlume.getInstance().getThemeManager().getGlobalOpacity();
        return applyOpacity(color, opacity);
    }

    private int applyCustomOpacity(int color) {
        return applyOpacity(color, (float) customOpacity.getValue());
    }

    private int applyOpacity(int color, float opacity) {
        int alpha = (color >> 24) & 255;
        alpha = (int) (alpha * opacity);

        if (alpha <= 0) {
            alpha = (int) (255 * opacity);
        }

        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    protected void drawRoundedFrame(float x, float y, float width, float height, float radius, int fillColor, int outlineColor) {
        if (width <= 2 || height <= 2) {
            RoundedUtil.drawRound(x, y, width, height, radius, fillColor);
            return;
        }

        RoundedUtil.drawRound(x, y, width, height, radius, outlineColor);

        float innerRadius = Math.max(0.0f, radius - 1.0f);

        RoundedUtil.drawRound(
                x + 1.0f,
                y + 1.0f,
                width - 2.0f,
                height - 2.0f,
                innerRadius,
                fillColor
        );
    }

    protected void drawSolidRect(float left, float top, float right, float bottom, int color) {
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(left, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, top);
        GL11.glVertex2f(left, top);
        GL11.glEnd();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void addSettings(Setting... settings) {
        if (settings == null) {
            return;
        }

        for (Setting setting : settings) {
            this.settings.add(setting);
        }
    }

    public String getName() {
        return name;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public List<Setting> getSettings() {
        return settings;
    }
}