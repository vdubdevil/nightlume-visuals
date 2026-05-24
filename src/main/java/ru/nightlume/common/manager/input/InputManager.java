package ru.nightlume.common.manager.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import ru.nightlume.api.event.EventBus;
import ru.nightlume.api.event.impl.KeyEvent;

public class InputManager {

    private final boolean[] keys = new boolean[512];

    public void update() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getMainWindow() == null) {
            return;
        }

        long handle = mc.getMainWindow().getHandle();

        for (int key = 32; key < keys.length; key++) {
            boolean down = InputMappings.isKeyDown(handle, key);

            if (down && !keys[key]) {
                EventBus.post(new KeyEvent(key));
            }

            keys[key] = down;
        }
    }
}