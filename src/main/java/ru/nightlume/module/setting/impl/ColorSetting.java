package ru.nightlume.module.setting.impl;

import ru.nightlume.module.setting.Setting;

import java.awt.Color;

public class ColorSetting extends Setting {

    private Color value;

    public ColorSetting(String name, Color value) {
        super(name);
        this.value = value;
    }

    public Color getValue() {
        return value;
    }

    public void setValue(Color value) {
        this.value = value;
    }
}