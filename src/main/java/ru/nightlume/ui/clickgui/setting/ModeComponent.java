package ru.nightlume.ui.clickgui.setting;

import ru.nightlume.module.setting.impl.ModeSetting;
import java.util.List;

public class ModeComponent {

    private final ModeSetting setting;

    private float x;
    private float y;
    private float width;
    private float height;

    public ModeComponent(ModeSetting setting) {
        this.setting = setting;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            List<String> modes = setting.getModes();
            int currentIndex = modes.indexOf(setting.getValue());
            int nextIndex = (currentIndex + 1) % modes.size();
            setting.setValue(modes.get(nextIndex));
        }
    }

    private boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public ModeSetting getSetting() { return setting; }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setWidth(float width) { this.width = width; }
    public void setHeight(float height) { this.height = height; }
}