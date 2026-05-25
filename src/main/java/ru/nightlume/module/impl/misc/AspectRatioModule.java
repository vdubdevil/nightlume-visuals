package ru.nightlume.module.impl.misc;

import ru.nightlume.module.Category;
import ru.nightlume.module.Module;
import ru.nightlume.module.setting.impl.ModeSetting;
import ru.nightlume.module.setting.impl.NumberSetting;

public class AspectRatioModule extends Module {

    public final ModeSetting mode = new ModeSetting(
            "Mode",
            "Default",
            "Default",
            "16:9",
            "16:10",
            "21:9",
            "4:3",
            "Custom"
    );

    public final NumberSetting customWidth = new NumberSetting(
            "Custom Width",
            16.0,
            1.0,
            32.0,
            0.1
    );

    public final NumberSetting customHeight = new NumberSetting(
            "Custom Height",
            9.0,
            1.0,
            32.0,
            0.1
    );

    public AspectRatioModule() {
        super("Aspect Ratio", Category.MISC);
        addSettings(mode, customWidth, customHeight);
    }

    public void updateVisibility() {
        boolean custom = mode.is("Custom");

        customWidth.setVisible(custom);
        customHeight.setVisible(custom);
    }

    public boolean isDefault() {
        return mode.is("Default");
    }

    public float getRatio() {
        if (mode.is("16:9")) return 16.0f / 9.0f;
        if (mode.is("16:10")) return 16.0f / 10.0f;
        if (mode.is("21:9")) return 21.0f / 9.0f;
        if (mode.is("4:3")) return 4.0f / 3.0f;

        float width = (float) customWidth.getValue();
        float height = (float) customHeight.getValue();

        if (height <= 0.0f) {
            return 16.0f / 9.0f;
        }

        return width / height;
    }
}