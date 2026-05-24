package ru.nightlume.ui.clickgui.module;

import ru.nightlume.module.Module;

public class ModuleComponent {

    private final Module module;
    private boolean expanded;

    private float x;
    private float y;
    private float width;
    private float height;

    private int settingsX;
    private int settingsY;
    private boolean dragging;
    private double dragX;
    private double dragY;

    public ModuleComponent(Module module) {
        this.module = module;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                toggle();
            } else if (button == 1) {
                expand();
            }
        }
    }

    private boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void toggle() {
        module.toggle();
    }

    public void expand() {
        expanded = !expanded;
    }

    public Module getModule() {
        return module;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setWidth(float width) { this.width = width; }
    public void setHeight(float height) { this.height = height; }

    public int getSettingsX() { return settingsX; }
    public void setSettingsX(int settingsX) { this.settingsX = settingsX; }

    public int getSettingsY() { return settingsY; }
    public void setSettingsY(int settingsY) { this.settingsY = settingsY; }

    public boolean isDragging() { return dragging; }
    public void setDragging(boolean dragging) { this.dragging = dragging; }

    public double getDragX() { return dragX; }
    public void setDragX(double dragX) { this.dragX = dragX; }

    public double getDragY() { return dragY; }
    public void setDragY(double dragY) { this.dragY = dragY; }
}