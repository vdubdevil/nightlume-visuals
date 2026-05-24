package ru.nightlume.render.font;

import java.awt.*;
import java.io.InputStream;

public class FontManager {

    public static CustomFontRenderer ICONS_20;

    public static void init() {

        try {

            InputStream stream =
                    FontManager.class.getResourceAsStream(
                            "/assets/nightlume/font/MaterialSymbolsRounded.ttf"
                    );

            Font font =
                    Font.createFont(Font.TRUETYPE_FONT, stream);

            ICONS_20 =
                    new CustomFontRenderer(
                            font.deriveFont(Font.PLAIN, 20f)
                    );

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}