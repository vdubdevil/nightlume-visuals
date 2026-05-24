package ru.nightlume.module.setting.impl;

import ru.nightlume.module.setting.Setting;

public class BindSetting extends Setting {

    private int key;

    public BindSetting(String name, int key) {
        super(name);
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}