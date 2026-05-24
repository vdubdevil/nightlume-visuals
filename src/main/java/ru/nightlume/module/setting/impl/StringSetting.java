package ru.nightlume.module.setting.impl;

import ru.nightlume.module.setting.Setting;

public class StringSetting extends Setting {

    private String value;

    public StringSetting(String name, String value) {
        super(name);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}