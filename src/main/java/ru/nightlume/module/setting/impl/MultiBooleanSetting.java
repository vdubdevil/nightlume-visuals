package ru.nightlume.module.setting.impl;

import ru.nightlume.module.setting.Setting;

import java.util.LinkedHashMap;
import java.util.Map;

public class MultiBooleanSetting extends Setting {

    private final LinkedHashMap<String, Boolean> values = new LinkedHashMap<>();

    public MultiBooleanSetting(String name) {
        super(name);
    }

    public MultiBooleanSetting add(String name, boolean value) {
        values.put(name, value);
        return this;
    }

    public boolean getValue(String name) {
        return values.getOrDefault(name, false);
    }

    public void setValue(String name, boolean value) {
        if (values.containsKey(name)) {
            values.put(name, value);
        }
    }

    public Map<String, Boolean> getValues() {
        return values;
    }
}