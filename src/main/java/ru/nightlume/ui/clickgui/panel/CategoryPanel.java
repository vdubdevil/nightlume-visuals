package ru.nightlume.ui.clickgui.panel;

import ru.nightlume.module.Category;
import ru.nightlume.module.Module;
import ru.nightlume.ui.clickgui.module.ModuleComponent;

import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {

    private final Category category;
    private final List<ModuleComponent> modules = new ArrayList<>();

    private int x;
    private int y;
    private int width;

    public CategoryPanel(Category category, int x, int y, int width) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;

        for (Module module : category.getModules()) {
            modules.add(new ModuleComponent(module));
        }
    }

    public Category getCategory() {
        return category;
    }

    public List<ModuleComponent> getModules() {
        return modules;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }
}