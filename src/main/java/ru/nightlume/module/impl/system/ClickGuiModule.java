package ru.nightlume.module.impl.system;

import org.lwjgl.glfw.GLFW;
import ru.nightlume.module.Category;
import ru.nightlume.module.Module;
import ru.nightlume.module.setting.impl.BindSetting;
import ru.nightlume.module.setting.impl.ModeSetting;
import ru.nightlume.ui.clickgui.ClickGuiScreen;

public class ClickGuiModule extends Module {

    public final ModeSetting style = new ModeSetting("Style", "Rounded", "Glassmorphism", "Rounded", "Classic");
    public final ModeSetting menuStyle = new ModeSetting("Menu Style", "Box", "Box", "Array-list");
    public final BindSetting bind = new BindSetting("Bind", GLFW.GLFW_KEY_RIGHT_SHIFT);

    public ClickGuiModule() {
        super("ClickGui", Category.SYSTEM);
        setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
        addSettings(style, menuStyle, bind);
    }

    @Override
    public void onEnable() {
        if (mc.currentScreen == null) {
            mc.displayGuiScreen(new ClickGuiScreen());
        }
        setEnabled(false);
    }
}