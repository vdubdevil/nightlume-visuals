package ru.nightlume.common.manager.theme;

import java.awt.Color;

public class ThemeManager {

    private int backgroundColor = new Color(20, 20, 20, 200).getRGB();
    private int outlineColor = new Color(40, 40, 40, 255).getRGB();
    private int textColor = new Color(255, 255, 255, 255).getRGB();
    private int iconColor = new Color(200, 200, 200, 255).getRGB();
    private int accentColor = new Color(80, 120, 255, 255).getRGB();

    private int blurTint = new Color(255, 255, 255, 40).getRGB();
    private float cornerRadius = 4.0f;
    private float globalOpacity = 1.0f;

    public int getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(int backgroundColor) { this.backgroundColor = backgroundColor; }

    public int getOutlineColor() { return outlineColor; }
    public void setOutlineColor(int outlineColor) { this.outlineColor = outlineColor; }

    public int getTextColor() { return textColor; }
    public void setTextColor(int textColor) { this.textColor = textColor; }

    public int getIconColor() { return iconColor; }
    public void setIconColor(int iconColor) { this.iconColor = iconColor; }

    public int getAccentColor() { return accentColor; }
    public void setAccentColor(int accentColor) { this.accentColor = accentColor; }

    public int getBlurTint() { return blurTint; }
    public void setBlurTint(int blurTint) { this.blurTint = blurTint; }

    public float getCornerRadius() { return cornerRadius; }
    public void setCornerRadius(float cornerRadius) { this.cornerRadius = cornerRadius; }

    public float getGlobalOpacity() { return globalOpacity; }
    public void setGlobalOpacity(float globalOpacity) { this.globalOpacity = globalOpacity; }
}