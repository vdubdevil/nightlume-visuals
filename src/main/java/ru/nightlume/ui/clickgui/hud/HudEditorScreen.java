package ru.nightlume.ui.clickgui.hud;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import ru.nightlume.Nightlume;
import ru.nightlume.common.manager.hud.HudManager;
import ru.nightlume.module.impl.system.ClickGuiModule;
import ru.nightlume.module.setting.Setting;
import ru.nightlume.module.setting.impl.BooleanSetting;
import ru.nightlume.module.setting.impl.ColorSetting;
import ru.nightlume.module.setting.impl.ModeSetting;
import ru.nightlume.module.setting.impl.NumberSetting;
import ru.nightlume.render.util.RenderUtil;
import ru.nightlume.render.util.RoundedUtil;
import ru.nightlume.ui.clickgui.hud.element.HudElement;

import java.awt.Color;

public class HudEditorScreen extends Screen {

    private HudElement draggingElement;
    private HudElement configElement;

    private float dragX;
    private float dragY;

    private boolean snapToGrid = true;
    private String currentStyle = "Glassmorphism";

    private ColorSetting expandedColorSetting;
    private int[] colorPickerBounds;

    private boolean draggingSV;
    private boolean draggingHue;
    private boolean draggingAlpha;
    private boolean typingHex;

    private String currentHexInput = "";

    private float pickerHue;
    private float pickerSat;
    private float pickerBright;
    private int pickerAlpha = 255;

    public HudEditorScreen() {
        super(new StringTextComponent("HUD Editor"));
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        colorPickerBounds = null;

        renderBackground(stack);
        updateStyle();

        if (snapToGrid) {
            int gridSize = 10;

            for (int i = 0; i < width; i += gridSize) {
                RenderUtil.drawRect(i, 0, 1, height, 0x11FFFFFF);
            }

            for (int i = 0; i < height; i += gridSize) {
                RenderUtil.drawRect(0, i, width, 1, 0x11FFFFFF);
            }
        }

        font.drawString(stack, "G: Grid | Shift: Center Snap | Right Click: Settings", 10, 10, 0xFFAAAAAA);

        HudManager hudManager = Nightlume.getInstance().getHudManager();
        int[] configBounds = getConfigWindowBounds();

        for (HudElement element : hudManager.getElements()) {
            if (!hudManager.isElementEnabled(element)) {
                continue;
            }

            if (configBounds != null && element != configElement) {
                if (intersects(
                        element.getX(),
                        element.getY(),
                        element.getWidth(),
                        element.getHeight(),
                        configBounds[0],
                        configBounds[1],
                        configBounds[2],
                        configBounds[3]
                )) {
                    continue;
                }
            }

            element.render(stack, currentStyle);
            element.render(stack, currentStyle);

            float x = element.getX();
            float y = element.getY();
            float w = element.getWidth();
            float h = element.getHeight();

            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
            int outlineColor = hovered || draggingElement == element || configElement == element
                    ? Nightlume.getInstance().getThemeManager().getAccentColor()
                    : 0x44FFFFFF;

            RenderUtil.drawRect(x, y, w, 1, outlineColor);
            RenderUtil.drawRect(x, y + h - 1, w, 1, outlineColor);
            RenderUtil.drawRect(x, y, 1, h, outlineColor);
            RenderUtil.drawRect(x + w - 1, y, 1, h, outlineColor);
        }

        if (configElement != null) {
            renderConfigWindow(stack, mouseX, mouseY);
        }

        if (expandedColorSetting != null && colorPickerBounds != null) {
            RenderSystem.disableDepthTest();
            stack.push();
            stack.translate(0, 0, 500);
            renderColorPickerOverlay(stack);
            stack.pop();
            RenderSystem.enableDepthTest();
        }

        super.render(stack, mouseX, mouseY, partialTicks);
    }

    private void updateStyle() {
        ClickGuiModule clickGui = (ClickGuiModule) Nightlume.getInstance().getModuleManager().getModule(ClickGuiModule.class);

        if (clickGui != null) {
            currentStyle = clickGui.style.getValue();
        }
    }

    private void renderConfigWindow(MatrixStack stack, int mouseX, int mouseY) {
        int winW = 220;
        int activeSettingsCount = 0;

        for (Setting setting : configElement.getSettings()) {
            if (setting.isVisible()) {
                activeSettingsCount++;
            }
        }

        int winH = 34 + activeSettingsCount * 22;
        int winX = width / 2 - winW / 2;
        int winY = height / 2 - winH / 2;

        RenderUtil.drawRect(0, 0, width, height, 0x55000000);
        drawPanel(winX, winY, winW, winH);

        font.drawString(stack, configElement.getName().toUpperCase() + " SETTINGS", winX + 10, winY + 9, getTextColor());

        int y = winY + 28;

        for (Setting setting : configElement.getSettings()) {
            if (!setting.isVisible()) {
                continue;
            }

            boolean hovered = mouseX >= winX + 6 && mouseX <= winX + winW - 6 && mouseY >= y && mouseY <= y + 18;
            int moduleColor = hovered ? 0xFF2A2A2A : 0xFF1E1E1E;

            RoundedUtil.drawRound(winX + 6, y, winW - 12, 18, getRadius(), moduleColor);

            String text = setting.getName();

            if (setting instanceof BooleanSetting) {
                text += ": " + (((BooleanSetting) setting).getValue() ? "ON" : "OFF");
            } else if (setting instanceof ModeSetting) {
                text += ": " + ((ModeSetting) setting).getValue();
            } else if (setting instanceof NumberSetting) {
                NumberSetting number = (NumberSetting) setting;
                double percentage = (number.getValue() - number.getMin()) / (number.getMax() - number.getMin());

                RenderUtil.drawRect(winX + 10, y + 14, winW - 20, 2, 0xFF111111);
                RenderUtil.drawRect(winX + 10, y + 14, (int) ((winW - 20) * percentage), 2, getAccentColor());

                text += ": " + String.format("%.2f", number.getValue());
            } else if (setting instanceof ColorSetting) {
                ColorSetting color = (ColorSetting) setting;

                RenderUtil.drawRect(winX + winW - 22, y + 5, 12, 8, color.getValue().getRGB());

                if (expandedColorSetting == setting) {
                    colorPickerBounds = new int[]{winX + 6, y + 20, winW - 12};
                }
            }

            font.drawString(stack, text, winX + 10, y + 5, getTextColor());
            y += 22;
        }
    }

    private void renderColorPickerOverlay(MatrixStack stack) {
        int px = colorPickerBounds[0];
        int py = colorPickerBounds[1];
        int pw = colorPickerBounds[2];

        RoundedUtil.drawRound(px, py, pw, 92, getRadius(), 0xFF151515);

        if (!typingHex && !draggingSV && !draggingHue && !draggingAlpha) {
            Color c = expandedColorSetting.getValue();
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);

            pickerHue = hsb[0];
            pickerSat = hsb[1];
            pickerBright = hsb[2];
            pickerAlpha = c.getAlpha();
            currentHexInput = String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
        }

        int svX = px + 5;
        int svY = py + 5;
        int svW = pw - 10;
        int svH = 40;

        drawSVSquare(svX, svY, svW, svH, pickerHue);

        int knobX = (int) (svX + pickerSat * svW);
        int knobY = (int) (svY + (1.0f - pickerBright) * svH);
        RenderUtil.drawRect(knobX - 1, knobY - 1, 3, 3, 0xFFFFFFFF);

        int hueY = svY + svH + 5;

        for (int i = 0; i < svW; i++) {
            RenderUtil.drawRect(svX + i, hueY, 1, 6, Color.HSBtoRGB((float) i / svW, 1.0f, 1.0f));
        }

        int hueKnobX = (int) (svX + pickerHue * svW);
        RenderUtil.drawRect(hueKnobX - 1, hueY - 1, 3, 8, 0xFFFFFFFF);

        int alphaY = hueY + 10;
        float alphaPct = pickerAlpha / 255.0f;

        RenderUtil.drawRect(svX, alphaY, svW, 6, 0xFF222222);
        RenderUtil.drawRect(svX, alphaY, (int) (svW * alphaPct), 6, Color.HSBtoRGB(pickerHue, pickerSat, pickerBright));

        int alphaKnobX = (int) (svX + alphaPct * svW);
        RenderUtil.drawRect(alphaKnobX - 1, alphaY - 1, 3, 8, 0xFFFFFFFF);

        int hexY = alphaY + 11;

        RenderUtil.drawRect(svX, hexY, svW, 12, typingHex ? 0xFF222222 : 0xFF1C1C1C);
        font.drawString(stack, "HEX: #" + currentHexInput, svX + 4, hexY + 2, typingHex ? getAccentColor() : 0xFFAAAAAA);
    }

    private void drawSVSquare(float x, float y, float width, float height, float hue) {
        int w = (int) width;
        int h = (int) height;

        for (int ix = 0; ix < w; ix++) {
            float sat = ix / (float) Math.max(1, w - 1);

            for (int iy = 0; iy < h; iy++) {
                float bright = 1.0f - (iy / (float) Math.max(1, h - 1));
                int color = Color.HSBtoRGB(hue, sat, bright);
                RenderUtil.drawRect(x + ix, y + iy, 1, 1, color);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (expandedColorSetting != null && colorPickerBounds != null) {
            int px = colorPickerBounds[0];
            int py = colorPickerBounds[1];
            int pw = colorPickerBounds[2];
            int ph = 92;

            if (mouseX >= px && mouseX <= px + pw && mouseY >= py && mouseY <= py + ph) {
                handleColorPickerClick(mouseX, mouseY, button);
                return true;
            }

            expandedColorSetting = null;
            typingHex = false;
        }

        if (configElement != null) {
            int winW = 220;
            int activeSettingsCount = 0;

            for (Setting setting : configElement.getSettings()) {
                if (setting.isVisible()) {
                    activeSettingsCount++;
                }
            }

            int winH = 34 + activeSettingsCount * 22;
            int winX = width / 2 - winW / 2;
            int winY = height / 2 - winH / 2;

            if (mouseX >= winX && mouseX <= winX + winW && mouseY >= winY && mouseY <= winY + winH) {
                int y = winY + 28;

                for (Setting setting : configElement.getSettings()) {
                    if (!setting.isVisible()) {
                        continue;
                    }

                    if (mouseX >= winX + 6 && mouseX <= winX + winW - 6 && mouseY >= y && mouseY <= y + 18) {
                        processSetting(setting, mouseX, winX + 10, winW - 20, button);
                        return true;
                    }

                    y += 22;
                }

                return true;
            }

            configElement = null;
            expandedColorSetting = null;
            typingHex = false;
            return true;
        }

        HudManager hudManager = Nightlume.getInstance().getHudManager();

        for (HudElement element : hudManager.getElements()) {
            if (!hudManager.isElementEnabled(element)) {
                continue;
            }

            if (mouseX >= element.getX() && mouseX <= element.getX() + element.getWidth()
                    && mouseY >= element.getY() && mouseY <= element.getY() + element.getHeight()) {
                if (button == 0) {
                    draggingElement = element;
                    dragX = (float) (mouseX - element.getX());
                    dragY = (float) (mouseY - element.getY());
                } else if (button == 1) {
                    configElement = element;
                    expandedColorSetting = null;
                    typingHex = false;
                }

                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleColorPickerClick(double mouseX, double mouseY, int button) {
        int px = colorPickerBounds[0];
        int py = colorPickerBounds[1];
        int pw = colorPickerBounds[2];

        int svY = py + 5;
        int svH = 40;
        int hueY = svY + svH + 5;
        int alphaY = hueY + 10;
        int hexY = alphaY + 11;

        draggingSV = false;
        draggingHue = false;
        draggingAlpha = false;

        if (mouseY >= svY && mouseY <= svY + svH) {
            draggingSV = true;
        } else if (mouseY >= hueY && mouseY <= hueY + 6) {
            draggingHue = true;
        } else if (mouseY >= alphaY && mouseY <= alphaY + 6) {
            draggingAlpha = true;
        } else if (mouseY >= hexY && mouseY <= hexY + 12 && button == 0) {
            typingHex = true;
            currentHexInput = "";
            return;
        }

        if (mouseY < hexY) {
            typingHex = false;
        }

        handleColorPickerDrag(mouseX, mouseY);
    }

    private void processSetting(Setting setting, double mouseX, int trackX, int trackWidth, int button) {
        if (setting instanceof BooleanSetting && button == 0) {
            ((BooleanSetting) setting).toggle();
            return;
        }

        if (setting instanceof ModeSetting && button == 0) {
            ModeSetting mode = (ModeSetting) setting;
            int index = mode.getModes().indexOf(mode.getValue());
            mode.setValue(mode.getModes().get((index + 1) % mode.getModes().size()));
            return;
        }

        if (setting instanceof NumberSetting && button == 0) {
            updateNumberSetting((NumberSetting) setting, mouseX, trackX, trackWidth);
            return;
        }

        if (setting instanceof ColorSetting && (button == 0 || button == 1)) {
            expandedColorSetting = (ColorSetting) setting;

            Color color = expandedColorSetting.getValue();
            float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

            pickerHue = hsb[0];
            pickerSat = hsb[1];
            pickerBright = hsb[2];
            pickerAlpha = color.getAlpha();
            currentHexInput = String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
            typingHex = false;
        }
    }

    private void updateNumberSetting(NumberSetting setting, double mouseX, int trackX, int trackWidth) {
        double percentage = Math.min(1.0, Math.max(0.0, (mouseX - trackX) / trackWidth));
        double value = setting.getMin() + (setting.getMax() - setting.getMin()) * percentage;

        setting.setValue(Math.round(value / setting.getIncrement()) * setting.getIncrement());
    }

    private void handleColorPickerDrag(double mouseX, double mouseY) {
        if (expandedColorSetting == null || colorPickerBounds == null) {
            return;
        }

        int px = colorPickerBounds[0];
        int py = colorPickerBounds[1];
        int pw = colorPickerBounds[2];

        int svX = px + 5;
        int svY = py + 5;
        int svW = pw - 10;
        int svH = 40;

        if (draggingSV) {
            pickerSat = Math.max(0.0f, Math.min(1.0f, (float) (mouseX - svX) / svW));
            pickerBright = 1.0f - Math.max(0.0f, Math.min(1.0f, (float) (mouseY - svY) / svH));
        } else if (draggingHue) {
            pickerHue = Math.max(0.0f, Math.min(1.0f, (float) (mouseX - svX) / svW));
        } else if (draggingAlpha) {
            pickerAlpha = (int) (Math.max(0.0f, Math.min(1.0f, (float) (mouseX - svX) / svW)) * 255.0f);
        }

        Color updated = new Color(Color.HSBtoRGB(pickerHue, pickerSat, pickerBright));
        expandedColorSetting.setValue(new Color(updated.getRed(), updated.getGreen(), updated.getBlue(), pickerAlpha));
        currentHexInput = String.format("%02X%02X%02X", updated.getRed(), updated.getGreen(), updated.getBlue());
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSV = false;
        draggingHue = false;
        draggingAlpha = false;

        if (button == 0) {
            draggingElement = null;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (expandedColorSetting != null && colorPickerBounds != null && (draggingSV || draggingHue || draggingAlpha)) {
            handleColorPickerDrag(mouseX, mouseY);
            return true;
        }

        if (configElement != null && button == 0) {
            int winW = 220;
            int activeSettingsCount = 0;

            for (Setting setting : configElement.getSettings()) {
                if (setting.isVisible()) {
                    activeSettingsCount++;
                }
            }

            int winX = width / 2 - winW / 2;
            int winY = height / 2 - (34 + activeSettingsCount * 22) / 2;
            int y = winY + 28;

            for (Setting setting : configElement.getSettings()) {
                if (!setting.isVisible()) {
                    continue;
                }

                if (setting instanceof NumberSetting
                        && mouseX >= winX + 6 && mouseX <= winX + winW - 6
                        && mouseY >= y && mouseY <= y + 18) {
                    updateNumberSetting((NumberSetting) setting, mouseX, winX + 10, winW - 20);
                    return true;
                }

                y += 22;
            }

            return true;
        }

        if (draggingElement != null && button == 0) {
            float newX = (float) (mouseX - this.dragX);
            float newY = (float) (mouseY - this.dragY);

            if (snapToGrid) {
                newX = Math.round(newX / 10.0f) * 10.0f;
                newY = Math.round(newY / 10.0f) * 10.0f;
            }

            if (isShiftPressed()) {
                newX = width / 2.0f - draggingElement.getWidth() / 2.0f;
            }

            draggingElement.setX(newX);
            draggingElement.setY(newY);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (typingHex && expandedColorSetting != null) {
            String valid = "0123456789abcdefABCDEF";

            if (valid.indexOf(codePoint) != -1 && currentHexInput.length() < 6) {
                currentHexInput += codePoint;

                if (currentHexInput.length() == 6) {
                    try {
                        int rgb = Integer.parseInt(currentHexInput, 16);
                        Color parsed = new Color(rgb);

                        float[] hsb = Color.RGBtoHSB(parsed.getRed(), parsed.getGreen(), parsed.getBlue(), null);

                        pickerHue = hsb[0];
                        pickerSat = hsb[1];
                        pickerBright = hsb[2];

                        expandedColorSetting.setValue(new Color(parsed.getRed(), parsed.getGreen(), parsed.getBlue(), pickerAlpha));
                    } catch (NumberFormatException ignored) {
                    }
                }

                return true;
            }
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typingHex) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !currentHexInput.isEmpty()) {
                currentHexInput = currentHexInput.substring(0, currentHexInput.length() - 1);
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                typingHex = false;
                return true;
            }

            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_G) {
            snapToGrid = !snapToGrid;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (expandedColorSetting != null) {
                expandedColorSetting = null;
                return true;
            }

            if (configElement != null) {
                configElement = null;
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawPanel(int x, int y, int width, int height) {
        int bg = Nightlume.getInstance().getThemeManager().getBackgroundColor();
        int outline = Nightlume.getInstance().getThemeManager().getOutlineColor();

        if (currentStyle.equalsIgnoreCase("Glassmorphism")) {
            bg = Nightlume.getInstance().getThemeManager().getBlurTint();
        }

        RoundedUtil.drawRound(x, y, width, height, getRadius(), outline);
        RoundedUtil.drawRound(x + 1, y + 1, width - 2, height - 2, Math.max(0.0f, getRadius() - 1.0f), bg);
    }

    private int getTextColor() {
        return Nightlume.getInstance().getThemeManager().getTextColor();
    }

    private int getAccentColor() {
        return Nightlume.getInstance().getThemeManager().getAccentColor();
    }

    private float getRadius() {
        return Nightlume.getInstance().getThemeManager().getCornerRadius();
    }
    private boolean isShiftPressed() {
        long handle = Minecraft.getInstance().getMainWindow().getHandle();

        return GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    private int[] getConfigWindowBounds() {
        if (configElement == null) {
            return null;
        }

        int winW = 220;
        int activeSettingsCount = 0;

        for (Setting setting : configElement.getSettings()) {
            if (setting.isVisible()) {
                activeSettingsCount++;
            }
        }

        int winH = 34 + activeSettingsCount * 22;
        int winX = width / 2 - winW / 2;
        int winY = height / 2 - winH / 2;

        return new int[]{winX, winY, winW, winH};
    }

    private boolean intersects(float x, float y, float w, float h, int rx, int ry, int rw, int rh) {
        return x < rx + rw && x + w > rx && y < ry + rh && y + h > ry;

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}