package ru.nightlume.module.impl.misc;

import ru.nightlume.module.Category;
import ru.nightlume.module.Module;
import ru.nightlume.module.setting.impl.BooleanSetting;
import ru.nightlume.module.setting.impl.ModeSetting;
import ru.nightlume.module.setting.impl.NumberSetting;

public class ItemPhysicsModule extends Module {

    public final ModeSetting style = new ModeSetting(
            "Style",
            "Default",
            "Default",
            "Lying",
            "Flat 2D"
    );

    public final NumberSetting scale = new NumberSetting(
            "Scale",
            1.0,
            0.5,
            2.0,
            0.05
    );

    public final NumberSetting lyingAngle = new NumberSetting(
            "Lying Angle",
            90.0,
            0.0,
            180.0,
            1.0
    );

    public final NumberSetting lyingRotation = new NumberSetting(
            "Lying Rotation",
            0.0,
            -180.0,
            180.0,
            1.0
    );

    public final BooleanSetting lyingRandomRotation = new BooleanSetting(
            "Random Rotation",
            true
    );

    public final NumberSetting lyingOffsetY = new NumberSetting(
            "Ground Offset",
            0.02,
            -0.2,
            0.3,
            0.01
    );

    public final BooleanSetting flatFaceCamera = new BooleanSetting(
            "Face Camera",
            true
    );

    public final NumberSetting flatRotationSpeed = new NumberSetting(
            "Rotation Speed",
            0.0,
            0.0,
            10.0,
            0.1
    );

    public final NumberSetting flatOffsetY = new NumberSetting(
            "Float Offset",
            0.1,
            -0.2,
            0.5,
            0.01
    );

    public final BooleanSetting stackSpread = new BooleanSetting(
            "Stack Spread",
            true
    );

    public ItemPhysicsModule() {
        super("Item Physics", Category.MISC);
        addSettings(
                style,
                scale,
                lyingAngle,
                lyingRotation,
                lyingRandomRotation,
                lyingOffsetY,
                flatFaceCamera,
                flatRotationSpeed,
                flatOffsetY,
                stackSpread
        );
    }

    public void updateVisibility() {
        boolean defaultStyle = style.is("Default");
        boolean lying = style.is("Lying");
        boolean flat = style.is("Flat 2D");

        scale.setVisible(!defaultStyle);

        lyingAngle.setVisible(lying);
        lyingRotation.setVisible(lying);
        lyingRandomRotation.setVisible(lying);
        lyingOffsetY.setVisible(lying);

        flatFaceCamera.setVisible(flat);
        flatRotationSpeed.setVisible(flat);
        flatOffsetY.setVisible(flat);

        stackSpread.setVisible(!defaultStyle);
    }

    public boolean isDefault() {
        return style.is("Default");
    }

    public boolean isLying() {
        return style.is("Lying");
    }

    public boolean isFlat2D() {
        return style.is("Flat 2D");
    }

    public float getScale() {
        return (float) scale.getValue();
    }

    public float getLyingAngle() {
        return (float) lyingAngle.getValue();
    }

    public float getLyingRotation() {
        return (float) lyingRotation.getValue();
    }

    public float getLyingOffsetY() {
        return (float) lyingOffsetY.getValue();
    }

    public float getFlatRotationSpeed() {
        return (float) flatRotationSpeed.getValue();
    }

    public float getFlatOffsetY() {
        return (float) flatOffsetY.getValue();
    }
}