package ru.nightlume.module.impl.misc;

import org.lwjgl.glfw.GLFW;
import ru.nightlume.module.Category;
import ru.nightlume.module.Module;
import ru.nightlume.module.setting.impl.BindSetting;
import ru.nightlume.module.setting.impl.BooleanSetting;
import ru.nightlume.module.setting.impl.NumberSetting;
import net.minecraft.client.settings.PointOfView;

public class AltLookModule extends Module {

    public final BindSetting holdKey = new BindSetting("Hold Key", GLFW.GLFW_KEY_LEFT_ALT);
    public final BooleanSetting thirdPerson = new BooleanSetting("Third Person", true);
    public final NumberSetting sensitivity = new NumberSetting("Sensitivity", 1.0, 0.2, 3.0, 0.1);

    private boolean looking;
    private boolean wasHolding;
    private float cameraYaw;
    private float cameraPitch;
    private PointOfView previousPointOfView;

    public AltLookModule() {
        super("Alt Look", Category.MISC);
        addSettings(holdKey, thirdPerson, sensitivity);
    }

    public boolean isLooking() {
        return isEnabled() && looking;
    }

    public void setLooking(boolean looking) {
        this.looking = looking;
    }

    public float getCameraYaw() {
        return cameraYaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }

    public void reset(float yaw, float pitch) {
        cameraYaw = yaw;
        cameraPitch = pitch;
    }

    public void rotate(double x, double y) {
        cameraYaw += (float) x * sensitivity.getValue();
        cameraPitch += (float) y * sensitivity.getValue();

        if (cameraPitch > 90.0F) cameraPitch = 90.0F;
        if (cameraPitch < -90.0F) cameraPitch = -90.0F;
    }
    public PointOfView getPreviousPointOfView() {
        return previousPointOfView;
    }

    public void setPreviousPointOfView(PointOfView previousPointOfView) {
        this.previousPointOfView = previousPointOfView;
    }

    public boolean wasHolding() {
        return wasHolding;
    }

    public void setWasHolding(boolean wasHolding) {
        this.wasHolding = wasHolding;
    }
}