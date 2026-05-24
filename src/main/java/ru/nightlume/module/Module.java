package ru.nightlume.module;

import ru.nightlume.api.event.EventBus;
import ru.nightlume.api.interfaces.IMinecraft;
import ru.nightlume.module.setting.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module implements IMinecraft {

    private final String name;
    private final Category category;
    private final List<Setting> settings = new ArrayList<>();

    private boolean enabled;
    private int key;

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        if (enabled) {
            EventBus.register(this);
            onEnable();
        } else {
            EventBus.unregister(this);
            onDisable();
        }
    }

    public void addSettings(Setting... settings) {
        if (settings == null) {
            return;
        }

        Collections.addAll(this.settings, settings);
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}