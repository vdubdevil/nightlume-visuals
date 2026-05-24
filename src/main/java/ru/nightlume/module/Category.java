package ru.nightlume.module;

import java.util.ArrayList;
import java.util.List;

public enum Category {
    COMBAT("Combat"),
    RENDER("Render"),
    PLAYER("Player"),
    MISC("Misc"),
    SYSTEM("System"),
    HIDDEN("Hidden");

    private final String displayName;
    private final List<Module> modules = new ArrayList<>();

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
    public List<Module> getModules() { return modules; }
    public void addModule(Module module) { modules.add(module); }
}