package ru.nightlume.module.setting.impl;

import ru.nightlume.module.setting.Setting;

public class NumberSetting extends Setting {

    private final double min;
    private final double max;
    private final double increment;
    private double value;

    public NumberSetting(String name, double value, double min, double max, double increment) {
        super(name);
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        value = Math.max(min, Math.min(max, value));
        this.value = roundToIncrement(value);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }

    private double roundToIncrement(double value) {
        if (increment <= 0.0D) {
            return value;
        }
        return Math.round(value / increment) * increment;
    }
}