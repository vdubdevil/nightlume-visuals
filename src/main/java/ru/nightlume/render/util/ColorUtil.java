package ru.nightlume.render.util;

import java.awt.Color;

public class ColorUtil {

    public static int ensureReadability(int backgroundColor, int textColor) {
        Color bg = new Color(backgroundColor, true);
        Color text = new Color(textColor, true);

        double bgLuminance = getLuminance(bg);
        double textLuminance = getLuminance(text);

        double contrast = getContrastRatio(bgLuminance, textLuminance);

        if (contrast < 4.5) {
            if (bgLuminance > 0.5) {
                return new Color(20, 20, 20, text.getAlpha()).getRGB();
            } else {
                return new Color(235, 235, 235, text.getAlpha()).getRGB();
            }
        }

        return textColor;
    }

    private static double getLuminance(Color color) {
        double r = color.getRed() / 255.0;
        double g = color.getGreen() / 255.0;
        double b = color.getBlue() / 255.0;

        r = (r <= 0.03928) ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = (g <= 0.03928) ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = (b <= 0.03928) ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);

        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double getContrastRatio(double lum1, double lum2) {
        double lightest = Math.max(lum1, lum2);
        double darkest = Math.min(lum1, lum2);
        return (lightest + 0.05) / (darkest + 0.05);
    }
}