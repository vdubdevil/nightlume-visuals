package ru.nightlume.module.setting.impl;

import ru.nightlume.module.setting.Setting;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {

    private final List<String> modes;
    private String value;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name);
        this.modes = Arrays.asList(modes);
        this.value = defaultMode;
    }

    public List<String> getModes() {
        return modes;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (modes.contains(value)) {
            this.value = value;
        }
    }

    public boolean is(String mode) {
        return value.equalsIgnoreCase(mode);
    }
}