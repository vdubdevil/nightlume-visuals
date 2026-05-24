package ru.nightlume.ui.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import ru.nightlume.Nightlume;
import ru.nightlume.common.manager.theme.ThemeManager;
import ru.nightlume.module.Category;
import ru.nightlume.module.Module;
import ru.nightlume.module.impl.system.ClickGuiModule;
import ru.nightlume.module.impl.system.HudEditorModule;
import ru.nightlume.module.impl.themes.ThemeModule;
import ru.nightlume.module.setting.Setting;
import ru.nightlume.module.setting.impl.BindSetting;
import ru.nightlume.module.setting.impl.BooleanSetting;
import ru.nightlume.module.setting.impl.ColorSetting;
import ru.nightlume.module.setting.impl.ModeSetting;
import ru.nightlume.module.setting.impl.NumberSetting;
import ru.nightlume.render.font.FontManager;
import ru.nightlume.render.font.Icons;
import ru.nightlume.ui.clickgui.hud.HudEditorScreen;
import ru.nightlume.ui.clickgui.module.ModuleComponent;
import ru.nightlume.ui.clickgui.panel.CategoryPanel;
import ru.nightlume.render.util.BlurUtil;
import ru.nightlume.render.util.ColorUtil;
import ru.nightlume.render.util.RenderUtil;
import ru.nightlume.render.util.RoundedUtil;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {

    public enum Style { MINIMALISM, ROUNDED, GLASSMORPHISM }
    public enum Layout { PANEL, BOX }

    private static Style style = Style.GLASSMORPHISM;
    private static Layout layout = Layout.BOX;
    private static int bind = GLFW.GLFW_KEY_RIGHT_SHIFT;
    private static String currentHudTab = "Editor";

    private final List<CategoryPanel> panels = new ArrayList<>();
    private final List<ModuleComponent> expandedModules = new ArrayList<>();

    private Category currentCategory = Category.COMBAT;

    private boolean listeningBind;
    private BindSetting activeBindSetting;
    private boolean showThemeEditor = false;

    private ColorSetting expandedColorSetting = null;
    private int[] colorPickerBounds = null;
    private boolean draggingSV = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;
    private boolean typingHex = false;
    private String currentHexInput = "";

    private float pickerHue = 0.0f;
    private float pickerSat = 0.0f;
    private float pickerBright = 0.0f;
    private int pickerAlpha = 255;

    public ClickGuiScreen() {
        super(new StringTextComponent("Nightlume"));
    }

    @Override
    protected void init() {
        panels.clear();
        int panelWidth = 110;
        int spacing = 10;

        int validCategories = 0;
        for (Category c : Category.values()) if (c != Category.HIDDEN) validCategories++;

        int totalWidth = validCategories * panelWidth + (validCategories - 1) * spacing;
        int startX = (width - totalWidth) / 2;
        int y = height / 2 - 110;

        for (Category category : Category.values()) {
            if (category == Category.HIDDEN) continue;
            panels.add(new CategoryPanel(category, startX, y, panelWidth));
            startX += panelWidth + spacing;
        }
        super.init();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        colorPickerBounds = null;

        if (Nightlume.getInstance() != null) {
            ClickGuiModule clickGuiModule = (ClickGuiModule) Nightlume.getInstance().getModuleManager().getModule(ClickGuiModule.class);
            if (clickGuiModule != null) {
                String modStyle = clickGuiModule.style.getValue();
                if (modStyle.equalsIgnoreCase("Rounded")) style = Style.ROUNDED;
                else if (modStyle.equalsIgnoreCase("Glassmorphism")) style = Style.GLASSMORPHISM;
                else style = Style.MINIMALISM;

                String modLayout = clickGuiModule.menuStyle.getValue();
                if (modLayout.equalsIgnoreCase("Box")) layout = Layout.BOX;
                else layout = Layout.PANEL;
            }
        }

        renderBackground(stack);

        if (style == Style.GLASSMORPHISM) {
            ThemeModule tm = (ThemeModule) Nightlume.getInstance().getModuleManager().getModule(ThemeModule.class);
            float blurStr = tm != null ? (float) tm.blurStrength.getValue() : 4.0f;
            BlurUtil.drawBlurredBackground(0, 0, this.width, this.height, blurStr);
            RenderUtil.drawRect(0, 0, width, height, applyOpacity(0x44000000));
        }

        if (layout == Layout.BOX) {
            renderBox(stack, mouseX, mouseY);
        } else {
            FontManager.ICONS_20.drawString(Icons.SYSTEM, 30, 30, 0xFFFFFFFF);
            renderPanels(stack, mouseX, mouseY);
            if (!showThemeEditor) {
                for (ModuleComponent expanded : expandedModules) renderSettings(stack, mouseX, mouseY, expanded);
            }
        }

        renderThemeButton(stack, mouseX, mouseY);

        if (showThemeEditor) {
            renderThemeEditor(stack, mouseX, mouseY);
        }

        // Z-Index Overlap Fix: Disables depth test so the color picker renders on top of EVERYTHING
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

    private int applyOpacity(int color) {
        float globalOp = Nightlume.getInstance().getThemeManager().getGlobalOpacity();
        int alpha = (int) (((color >> 24) & 0xFF) * globalOp);
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private int getSafeTextColor(int backgroundColor) {
        int targetText = Nightlume.getInstance().getThemeManager().getTextColor();
        return ColorUtil.ensureReadability(backgroundColor, targetText);
    }

    private void renderThemeButton(MatrixStack stack, int mouseX, int mouseY) {
        int btnX = layout == Layout.BOX ? (width / 2 + 290 - 32) : width - 40;
        int btnY = layout == Layout.BOX ? (height / 2 + 160 - 32) : height - 40;

        boolean hovered = mouseX >= btnX && mouseX <= btnX + 24 && mouseY >= btnY && mouseY <= btnY + 24;
        float radius = Nightlume.getInstance().getThemeManager().getCornerRadius();
        int color = hovered ? Nightlume.getInstance().getThemeManager().getAccentColor() : applyOpacity(0xFF2A2A2A);

        RoundedUtil.drawRound(btnX, btnY, 24, 24, radius, color);
        font.drawString(stack, "T", btnX + 9, btnY + 8, getSafeTextColor(color));
    }

    private void renderThemeEditor(MatrixStack stack, int mouseX, int mouseY) {
        ThemeModule tm = (ThemeModule) Nightlume.getInstance().getModuleManager().getModule(ThemeModule.class);
        if (tm == null) return;
        tm.updateVisibility();

        int winW = 200;
        int activeSettings = 0;
        for (Setting s : tm.getSettings()) if (s.isVisible()) activeSettings++;
        int winH = 30 + (activeSettings * 20);

        int winX = width / 2 - winW / 2;
        int winY = height / 2 - winH / 2;

        RenderUtil.drawRect(0, 0, width, height, 0x55000000);
        drawPanel(stack, winX, winY, winW, winH);

        font.drawString(stack, "THEME CONFIGURATION", winX + 12, winY + 10, getSafeTextColor(Nightlume.getInstance().getThemeManager().getBackgroundColor()));

        int y = winY + 28;
        for (Setting setting : tm.getSettings()) {
            if (!setting.isVisible()) continue;

            drawModule(stack, winX + 6, y, winW - 12, 18, applyOpacity(0xFF1E1E1E));
            String text = setting.getName();

            if (setting instanceof NumberSetting) {
                NumberSetting num = (NumberSetting) setting;
                double percentage = (num.getValue() - num.getMin()) / (num.getMax() - num.getMin());
                RenderUtil.drawRect(winX + 10, y + 14, winW - 20, 2, 0xFF111111);
                RenderUtil.drawRect(winX + 10, y + 14, (int)((winW - 20) * percentage), 2, Nightlume.getInstance().getThemeManager().getAccentColor());
                text += ": " + String.format("%.2f", num.getValue());
            } else if (setting instanceof ColorSetting) {
                ColorSetting colorSet = (ColorSetting) setting;
                RenderUtil.drawRect(winX + winW - 20, y + 5, 10, 8, colorSet.getValue().getRGB());
                if (expandedColorSetting == setting) {
                    colorPickerBounds = new int[]{winX + 6, y + 18, winW - 12};
                }
            }

            font.drawString(stack, text, winX + 10, y + 4, getSafeTextColor(0xFF1E1E1E));
            y += 20;
        }
    }

    private void syncTheme() {
        ThemeModule tm = (ThemeModule) Nightlume.getInstance().getModuleManager().getModule(ThemeModule.class);
        ThemeManager mgr = Nightlume.getInstance().getThemeManager();
        if (tm != null && mgr != null) {
            mgr.setBackgroundColor(tm.bgColor.getValue().getRGB());
            mgr.setOutlineColor(tm.outlineColor.getValue().getRGB());
            mgr.setTextColor(tm.textColor.getValue().getRGB());
            mgr.setAccentColor(tm.accentColor.getValue().getRGB());
            mgr.setCornerRadius((float) tm.cornerRadius.getValue());
            mgr.setGlobalOpacity((float) tm.globalOpacity.getValue());
            mgr.setBlurTint(tm.glassTint.getValue().getRGB());
        }
    }

    private void renderColorPickerOverlay(MatrixStack stack) {
        int px = colorPickerBounds[0];
        int py = colorPickerBounds[1];
        int pw = colorPickerBounds[2];
        int ph = 92;

        drawModule(stack, px, py, pw, ph, applyOpacity(0xFF151515));

        if (!typingHex && !draggingSV && !draggingHue && !draggingAlpha) {
            Color c = expandedColorSetting.getValue();
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            pickerHue = hsb[0];
            pickerSat = hsb[1];
            pickerBright = hsb[2];
            pickerAlpha = c.getAlpha();
            currentHexInput = String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
        }

        int svX = px + 5, svY = py + 5, svW = pw - 10, svH = 40;
        drawSVSquare(svX, svY, svW, svH, pickerHue);

        int knobX = (int) (svX + (pickerSat * svW));
        int knobY = (int) (svY + ((1.0f - pickerBright) * svH));
        RenderUtil.drawRect(knobX - 1, knobY - 1, 3, 3, 0xFFFFFFFF);

        int hueY = svY + svH + 5;
        for (int i = 0; i < svW; i++) {
            RenderUtil.drawRect(svX + i, hueY, 1, 6, Color.HSBtoRGB((float)i/svW, 1f, 1f));
        }
        int hKnobX = (int) (svX + (pickerHue * svW));
        RenderUtil.drawRect(hKnobX - 1, hueY - 1, 3, 8, 0xFFFFFFFF);

        int alphaY = hueY + 10;
        float alphaPct = pickerAlpha / 255f;
        RenderUtil.drawRect(svX, alphaY, svW, 6, 0xFF222222);
        RenderUtil.drawRect(svX, alphaY, (int)(svW * alphaPct), 6, Color.HSBtoRGB(pickerHue, pickerSat, pickerBright));
        int aKnobX = (int) (svX + (alphaPct * svW));
        RenderUtil.drawRect(aKnobX - 1, alphaY - 1, 3, 8, 0xFFFFFFFF);

        int hexY = alphaY + 11;
        RenderUtil.drawRect(svX, hexY, svW, 12, typingHex ? 0xFF222222 : 0xFF1C1C1C);
        font.drawString(stack, "HEX: #" + currentHexInput, svX + 4, hexY + 2, typingHex ? Nightlume.getInstance().getThemeManager().getAccentColor() : 0xFFAAAAAA);
    }

    private void drawSVSquare(float x, float y, float w, float h, float hue) {
        int color = Color.HSBtoRGB(hue, 1.0f, 1.0f);
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glVertex2f(x, y);
        GL11.glColor4f(0f, 0f, 0f, 1f);
        GL11.glVertex2f(x, y + h);
        GL11.glColor4f(0f, 0f, 0f, 1f);
        GL11.glVertex2f(x + w, y + h);
        GL11.glColor4f(r, g, b, 1f);
        GL11.glVertex2f(x + w, y);
        GL11.glEnd();

        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    private void renderBox(MatrixStack stack, int mouseX, int mouseY) {
        int winWidth = 580;
        int winHeight = 320;
        int winX = width / 2 - winWidth / 2;
        int winY = height / 2 - winHeight / 2;

        drawPanel(stack, winX, winY, winWidth, winHeight);

        int mainBg = Nightlume.getInstance().getThemeManager().getBackgroundColor();
        font.drawString(stack, "NIGHTLUME ENVIRONMENT", winX + 20, winY + 15, getSafeTextColor(mainBg));

        int catSpacing = 24;
        int totalCatWidth = (Category.values().length - 1) * catSpacing;
        int startCatX = winX + (winWidth / 2) - (totalCatWidth / 2);

        int i = 0;
        for (Category category : Category.values()) {
            if (category == Category.HIDDEN) continue;
            int cx = startCatX + (i * catSpacing);
            int cy = winY + 15;
            boolean isCurrent = category == currentCategory;
            RenderUtil.drawRect(cx, cy, 14, 14, isCurrent ? Nightlume.getInstance().getThemeManager().getAccentColor() : applyOpacity(0xFF555555));
            i++;
        }

        String tabName = currentCategory.getDisplayName().toUpperCase();
        int tabWidth = font.getStringWidth(tabName);
        font.drawString(stack, tabName, winX + (winWidth / 2) - (tabWidth / 2), winY + 45, getSafeTextColor(mainBg));

        int colWidth = 160;
        int colSpacing = 20;
        int startColX = winX + 30;
        int[] colY = new int[]{winY + 75, winY + 75, winY + 75};

        CategoryPanel activePanel = null;
        for (CategoryPanel panel : panels) {
            if (panel.getCategory() == currentCategory) {
                activePanel = panel;
                break;
            }
        }

        if (activePanel != null) {
            int modIndex = 0;
            for (ModuleComponent component : activePanel.getModules()) {
                int col = modIndex % 3;
                int x = startColX + col * (colWidth + colSpacing);
                int y = colY[col];

                Module module = component.getModule();
                boolean hovered = mouseX >= x && mouseX <= x + colWidth && mouseY >= y && mouseY <= y + 22;

                int color = module.isEnabled() ? (hovered ? applyOpacity(0xFF3A3A3A) : applyOpacity(0xFF2F2F2F)) : (hovered ? applyOpacity(0xFF262626) : applyOpacity(0xFF1C1C1C));

                drawModule(stack, x, y, colWidth, 22, color);

                RenderUtil.drawRect(x + 6, y + 5, 12, 12, module.isEnabled() ? Nightlume.getInstance().getThemeManager().getAccentColor() : applyOpacity(0xFF555555));
                font.drawString(stack, module.getName(), x + 24, y + 7, getSafeTextColor(color));
                font.drawString(stack, component.isExpanded() ? "^" : "v", x + colWidth - 14, y + 7, getSafeTextColor(color));

                colY[col] += 26;

                if (component.isExpanded()) {
                    if (module instanceof HudEditorModule) {
                        int halfW = colWidth / 2;
                        boolean isEd = currentHudTab.equals("Editor");

                        drawModule(stack, x, colY[col], halfW - 2, 16, isEd ? applyOpacity(0xFF3A3A3A) : applyOpacity(0xFF1E1E1E));
                        font.drawString(stack, "Editor", x + (halfW / 2) - (font.getStringWidth("Editor") / 2), colY[col] + 4, getSafeTextColor(0xFF1E1E1E));

                        drawModule(stack, x + halfW + 2, colY[col], halfW - 2, 16, !isEd ? applyOpacity(0xFF3A3A3A) : applyOpacity(0xFF1E1E1E));
                        font.drawString(stack, "Elements", x + halfW + (halfW / 2) - (font.getStringWidth("Elements") / 2), colY[col] + 4, getSafeTextColor(0xFF1E1E1E));

                        colY[col] += 20;

                        if (isEd) {
                            boolean btnHover = mouseX >= x && mouseX <= x + colWidth && mouseY >= colY[col] && mouseY <= colY[col] + 20;
                            boolean canOpen = canOpenHudEditor();
                            int btnColor = !canOpen ? applyOpacity(0xFF151515) : btnHover ? applyOpacity(0xFF555555) : applyOpacity(0xFF3A3A3A);
                            int textColor = !canOpen ? 0xFF666666 : getSafeTextColor(btnColor);

                            drawModule(stack, x, colY[col], colWidth, 20, btnColor);
                            font.drawString(stack, "OPEN EDITOR", x + (colWidth / 2) - (font.getStringWidth("OPEN EDITOR") / 2), colY[col] + 6, textColor);
                        } else {
                            renderSettingsLoopBox(stack, module, x, col, colY, colWidth, mouseX, mouseY);
                        }
                    } else {
                        renderSettingsLoopBox(stack, module, x, col, colY, colWidth, mouseX, mouseY);
                    }
                }
                modIndex++;
            }
        }
    }

    private void renderSettingsLoopBox(MatrixStack stack, Module module, int x, int col, int[] colY, int colWidth, int mouseX, int mouseY) {
        for (Setting setting : module.getSettings()) {
            if (!setting.isVisible()) continue;
            boolean setHovered = mouseX >= x && mouseX <= x + colWidth && mouseY >= colY[col] && mouseY <= colY[col] + 18;
            int setColor = setHovered ? applyOpacity(0xFF2A2A2A) : applyOpacity(0xFF1E1E1E);
            drawModule(stack, x, colY[col], colWidth, 18, setColor);

            String text = setting.getName();
            if (setting instanceof BooleanSetting) {
                text += ": " + (((BooleanSetting) setting).getValue() ? "ON" : "OFF");
            } else if (setting instanceof ModeSetting) {
                text += ": " + ((ModeSetting) setting).getValue();
            } else if (setting instanceof BindSetting) {
                text += ": " + (activeBindSetting == setting ? "..." : getKeyName(((BindSetting) setting).getKey()));
            } else if (setting instanceof NumberSetting) {
                NumberSetting num = (NumberSetting) setting;
                double percentage = (num.getValue() - num.getMin()) / (num.getMax() - num.getMin());
                RenderUtil.drawRect(x + 4, colY[col] + 14, colWidth - 8, 2, 0xFF111111);
                RenderUtil.drawRect(x + 4, colY[col] + 14, (int)((colWidth - 8) * percentage), 2, Nightlume.getInstance().getThemeManager().getAccentColor());
                text += ": " + String.format("%.2f", num.getValue());
            } else if (setting instanceof ColorSetting) {
                ColorSetting colorSet = (ColorSetting) setting;
                RenderUtil.drawRect(x + colWidth - 18, colY[col] + 5, 10, 8, colorSet.getValue().getRGB());
                if (expandedColorSetting == setting) {
                    colorPickerBounds = new int[]{x, colY[col] + 18, colWidth};
                }
            }

            font.drawString(stack, text, x + 8, colY[col] + 4, getSafeTextColor(setColor));
            colY[col] += 20;
        }
        colY[col] += 6;
    }

    private void renderPanels(MatrixStack stack, int mouseX, int mouseY) {
        for (CategoryPanel panel : panels) {
            int x = panel.getX();
            int y = panel.getY();
            int width = 100;
            int panelHeight = 300;

            drawPanel(stack, x, y, width, panelHeight);
            int mainBg = Nightlume.getInstance().getThemeManager().getBackgroundColor();
            font.drawString(stack, panel.getCategory().getDisplayName(), x + 6, y + 8, getSafeTextColor(mainBg));

            int moduleY = y + 26;
            for (ModuleComponent component : panel.getModules()) {
                Module module = component.getModule();
                boolean hovered = mouseX >= x + 4 && mouseX <= x + width - 4 && mouseY >= moduleY && mouseY <= moduleY + 14;
                int color = module.isEnabled() ? (hovered ? applyOpacity(0xFF3A3A3A) : applyOpacity(0xFF2F2F2F)) : (hovered ? applyOpacity(0xFF262626) : applyOpacity(0xFF1C1C1C));

                drawModule(stack, x + 4, moduleY, width - 8, 14, color);
                font.drawString(stack, module.getName(), x + 9, moduleY + 4, getSafeTextColor(color));

                if (expandedModules.contains(component)) {
                    font.drawString(stack, ">", x + width - 12, moduleY + 4, getSafeTextColor(color));
                }

                moduleY += 18;
            }
        }
    }

    private void renderSettings(MatrixStack stack, int mouseX, int mouseY, ModuleComponent component) {
        if (component.getSettingsX() == 0 && component.getSettingsY() == 0) {
            component.setSettingsX(width / 2 + 320);
            component.setSettingsY(height / 2 - 110);
        }

        int x = component.getSettingsX();
        int y = component.getSettingsY();
        int width = 160;
        int height = 24;

        if (component.getModule() instanceof HudEditorModule) {
            height += 24;
            if (currentHudTab.equals("Editor")) height += 26;
            else height += component.getModule().getSettings().size() * 18;
        } else {
            height += component.getModule().getSettings().size() * 18;
        }

        drawPanel(stack, x, y, width, height);
        font.drawString(stack, component.getModule().getName(), x + 12, y + 8, getSafeTextColor(Nightlume.getInstance().getThemeManager().getBackgroundColor()));

        int settingY = y + 22;

        if (component.getModule() instanceof HudEditorModule) {
            int halfW = width / 2;
            boolean isEd = currentHudTab.equals("Editor");

            drawModule(stack, x + 4, settingY, halfW - 6, 16, isEd ? applyOpacity(0xFF3A3A3A) : applyOpacity(0xFF1E1E1E));
            font.drawString(stack, "Editor", x + 4 + ((halfW - 6) / 2) - (font.getStringWidth("Editor") / 2), settingY + 4, getSafeTextColor(0xFF1E1E1E));

            drawModule(stack, x + halfW + 2, settingY, halfW - 6, 16, !isEd ? applyOpacity(0xFF3A3A3A) : applyOpacity(0xFF1E1E1E));
            font.drawString(stack, "Elements", x + halfW + 2 + ((halfW - 6) / 2) - (font.getStringWidth("Elements") / 2), settingY + 4, getSafeTextColor(0xFF1E1E1E));

            settingY += 20;

            if (isEd) {
                boolean btnHover = mouseX >= x + 4 && mouseX <= x + width - 4 && mouseY >= settingY && mouseY <= settingY + 20;
                boolean canOpen = canOpenHudEditor();
                int btnColor = !canOpen ? applyOpacity(0xFF151515) : btnHover ? applyOpacity(0xFF555555) : applyOpacity(0xFF3A3A3A);
                int textColor = !canOpen ? 0xFF666666 : getSafeTextColor(btnColor);

                drawModule(stack, x + 4, settingY, width - 8, 20, btnColor);
                font.drawString(stack, "OPEN EDITOR", x + (width / 2) - (font.getStringWidth("OPEN EDITOR") / 2), settingY + 6, textColor);
            } else {
                renderSettingsLoopFloat(stack, component.getModule(), x, width, settingY);
            }
        } else {
            renderSettingsLoopFloat(stack, component.getModule(), x, width, settingY);
        }
    }

    private void renderSettingsLoopFloat(MatrixStack stack, Module module, int x, int width, int settingY) {
        for (Setting setting : module.getSettings()) {
            if (!setting.isVisible()) continue;
            int setColor = applyOpacity(0xFF1E1E1E);
            drawModule(stack, x + 6, settingY, width - 12, 16, setColor);
            String text = setting.getName();

            if (setting instanceof BooleanSetting) {
                text += ": " + (((BooleanSetting) setting).getValue() ? "ON" : "OFF");
            } else if (setting instanceof ModeSetting) {
                text += ": " + ((ModeSetting) setting).getValue();
            } else if (setting instanceof BindSetting) {
                text += ": " + (activeBindSetting == setting ? "..." : getKeyName(((BindSetting) setting).getKey()));
            } else if (setting instanceof NumberSetting) {
                NumberSetting num = (NumberSetting) setting;
                double percentage = (num.getValue() - num.getMin()) / (num.getMax() - num.getMin());
                RenderUtil.drawRect(x + 10, settingY + 13, width - 20, 2, 0xFF111111);
                RenderUtil.drawRect(x + 10, settingY + 13, (int)((width - 20) * percentage), 2, Nightlume.getInstance().getThemeManager().getAccentColor());
                text += ": " + String.format("%.2f", num.getValue());
            } else if (setting instanceof ColorSetting) {
                ColorSetting colorSet = (ColorSetting) setting;
                RenderUtil.drawRect(x + width - 20, settingY + 4, 10, 8, colorSet.getValue().getRGB());
                if (expandedColorSetting == setting) {
                    colorPickerBounds = new int[]{x + 6, settingY + 18, width - 12};
                }
            }

            font.drawString(stack, text, x + 10, settingY + 3, getSafeTextColor(setColor));
            settingY += 18;
        }
    }

    private void drawPanel(MatrixStack stack, int x, int y, int width, int height) {
        float radius = Nightlume.getInstance().getThemeManager().getCornerRadius();
        int bgColor = applyOpacity(Nightlume.getInstance().getThemeManager().getBackgroundColor());
        int outColor = applyOpacity(Nightlume.getInstance().getThemeManager().getOutlineColor());

        if (style == Style.MINIMALISM) {
            RenderUtil.drawRect(x, y, width, height, bgColor);
            RenderUtil.drawRect(x, y, width, 20, outColor);
            return;
        }

        if (style == Style.GLASSMORPHISM) {
            ThemeModule tm = (ThemeModule) Nightlume.getInstance().getModuleManager().getModule(ThemeModule.class);
            float borderOpacity = tm != null ? (float) tm.borderOpacity.getValue() : 0.3f;
            int tint = Nightlume.getInstance().getThemeManager().getBlurTint();

            RoundedUtil.drawRound(x, y, width, height, radius, tint);

            int borderAlpha = (int)(borderOpacity * 255);
            int syncedGlowColor = (outColor & 0x00FFFFFF) | (borderAlpha << 24);

            RenderUtil.drawRect(x, y, width, 1, syncedGlowColor);
            RenderUtil.drawRect(x, y + height - 1, width, 1, syncedGlowColor);
            RenderUtil.drawRect(x, y, 1, height, syncedGlowColor);
            RenderUtil.drawRect(x + width - 1, y, 1, height, syncedGlowColor);
            return;
        }

        RoundedUtil.drawRound(x, y, width, height, radius, bgColor);
        RoundedUtil.drawRound(x, y, width, 20, radius, outColor);
    }

    private void drawModule(MatrixStack stack, int x, int y, int width, int height, int color) {
        float radius = Nightlume.getInstance().getThemeManager().getCornerRadius();
        if (style == Style.MINIMALISM) {
            RenderUtil.drawRect(x, y, width, height, color);
            return;
        }
        RoundedUtil.drawRound(x, y, width, height, style == Style.GLASSMORPHISM ? radius + 2 : radius, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (expandedColorSetting != null && colorPickerBounds != null) {
            int px = colorPickerBounds[0], py = colorPickerBounds[1], pw = colorPickerBounds[2], ph = 92;
            if (mouseX >= px && mouseX <= px + pw && mouseY >= py && mouseY <= py + ph) {
                int svY = py + 5, svH = 40, hueY = svY + svH + 5, alphaY = hueY + 10, hexY = alphaY + 11;

                if (mouseY >= svY && mouseY <= svY + svH) draggingSV = true;
                else if (mouseY >= hueY && mouseY <= hueY + 6) draggingHue = true;
                else if (mouseY >= alphaY && mouseY <= alphaY + 6) draggingAlpha = true;
                else if (mouseY >= hexY && mouseY <= hexY + 12 && button == 0) {
                    typingHex = true;
                    currentHexInput = "";
                }

                if (!typingHex || draggingSV || draggingHue || draggingAlpha) {
                    if (mouseY < hexY) typingHex = false;
                    handleColorPickerDrag(mouseX, mouseY);
                }
                return true;
            } else {
                expandedColorSetting = null;
                typingHex = false;
            }
        }

        int btnX = layout == Layout.BOX ? (width / 2 + 290 - 32) : width - 40;
        int btnY = layout == Layout.BOX ? (height / 2 + 160 - 32) : height - 40;
        if (mouseX >= btnX && mouseX <= btnX + 24 && mouseY >= btnY && mouseY <= btnY + 24 && button == 0) {
            showThemeEditor = !showThemeEditor;
            return true;
        }

        if (showThemeEditor) {
            ThemeModule tm = (ThemeModule) Nightlume.getInstance().getModuleManager().getModule(ThemeModule.class);
            if (tm != null) {
                int winW = 200;
                int activeSettings = 0;
                for (Setting s : tm.getSettings()) if (s.isVisible()) activeSettings++;
                int winH = 30 + (activeSettings * 20);
                int winX = width / 2 - winW / 2;
                int winY = height / 2 - winH / 2;

                if (button == 0 && (mouseX < winX || mouseX > winX + winW || mouseY < winY || mouseY > winY + winH)) {
                    showThemeEditor = false;
                    return true;
                }

                int y = winY + 28;
                for (Setting setting : tm.getSettings()) {
                    if (!setting.isVisible()) continue;
                    if (mouseX >= winX + 6 && mouseX <= winX + winW - 6 && mouseY >= y && mouseY <= y + 18) {
                        if (setting instanceof NumberSetting && button == 0) {
                            NumberSetting num = (NumberSetting) setting;
                            double percentage = Math.min(1, Math.max(0, (mouseX - (winX + 10)) / (winW - 20)));
                            double val = num.getMin() + ((num.getMax() - num.getMin()) * percentage);
                            num.setValue(Math.round(val / num.getIncrement()) * num.getIncrement());
                            syncTheme();
                        } else if (setting instanceof ColorSetting && (button == 0 || button == 1)) {
                            expandedColorSetting = (ColorSetting) setting;
                            Color c = expandedColorSetting.getValue();
                            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                            pickerHue = hsb[0];
                            pickerSat = hsb[1];
                            pickerBright = hsb[2];
                            pickerAlpha = c.getAlpha();
                            typingHex = false;
                        }
                        return true;
                    }
                    y += 20;
                }
            }
            return true;
        }

        if (layout == Layout.BOX) {
            if (handleBoxClick(mouseX, mouseY, button)) return true;
        } else {
            for (int i = expandedModules.size() - 1; i >= 0; i--) {
                ModuleComponent component = expandedModules.get(i);
                int setX = component.getSettingsX();
                int setY = component.getSettingsY();
                int setWidth = 160;

                if (mouseX >= setX && mouseX <= setX + setWidth && mouseY >= setY && mouseY <= setY + 20 && button == 0) {
                    component.setDragging(true);
                    component.setDragX(mouseX - setX);
                    component.setDragY(mouseY - setY);
                    expandedModules.remove(component);
                    expandedModules.add(component);
                    return true;
                }

                int settingY = setY + 22;
                if (component.getModule() instanceof HudEditorModule) {
                    int halfW = setWidth / 2;
                    if (mouseY >= settingY && mouseY <= settingY + 16 && button == 0) {
                        if (mouseX >= setX + 4 && mouseX <= setX + halfW) {
                            currentHudTab = "Editor";
                            return true;
                        } else if (mouseX > setX + halfW && mouseX <= setX + setWidth - 4) {
                            currentHudTab = "Elements";
                            return true;
                        }
                    }
                    settingY += 20;

                    if (currentHudTab.equals("Editor")) {
                        if (mouseX >= setX + 4 && mouseX <= setX + setWidth - 4 && mouseY >= settingY && mouseY <= settingY + 20 && button == 0) {
                            openHudEditor();
                            return true;
                        }
                    } else {
                        if (handleSettingsClickPanel(component.getModule(), setX, setWidth, settingY, mouseX, mouseY, button)) return true;
                    }
                } else {
                    if (handleSettingsClickPanel(component.getModule(), setX, setWidth, settingY, mouseX, mouseY, button)) return true;
                }
            }

            for (CategoryPanel panel : panels) {
                int moduleY = panel.getY() + 26;
                for (ModuleComponent component : panel.getModules()) {
                    int x = panel.getX() + 4;
                    int width = panel.getWidth() - 8;

                    if (mouseX >= x && mouseX <= x + width && mouseY >= moduleY && mouseY <= moduleY + 14) {
                        if (button == 0 && !isUtilityModule(component.getModule())) {
                            component.toggle();
                        }

                        if (button == 1) {
                            if (expandedModules.contains(component)) {
                                expandedModules.remove(component);
                                component.setExpanded(false);
                            } else {
                                expandedModules.add(component);
                                component.setExpanded(true);
                            }
                        }

                        return true;
                    }
                    moduleY += 18;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleSettingsClickPanel(Module module, int setX, int setWidth, int settingY, double mouseX, double mouseY, int button) {
        for (Setting setting : module.getSettings()) {
            if (!setting.isVisible()) continue;
            if (mouseX >= setX + 6 && mouseX <= setX + setWidth - 6 && mouseY >= settingY && mouseY <= settingY + 16) {
                processSettingValue(setting, mouseX, setX + 10, setWidth - 20, button);
                return true;
            }
            settingY += 18;
        }
        return false;
    }

    private void processSettingValue(Setting setting, double mouseX, int trackX, int trackW, int button) {
        if (setting instanceof BooleanSetting && button == 0) {
            ((BooleanSetting) setting).setValue(!((BooleanSetting) setting).getValue());
        } else if (setting instanceof ModeSetting && button == 0) {
            ModeSetting mode = (ModeSetting) setting;
            int index = mode.getModes().indexOf(mode.getValue());
            mode.setValue(mode.getModes().get((index + 1) % mode.getModes().size()));
        } else if (setting instanceof BindSetting && button == 0) {
            activeBindSetting = (BindSetting) setting;
        } else if (setting instanceof NumberSetting && button == 0) {
            NumberSetting num = (NumberSetting) setting;
            double percentage = Math.min(1, Math.max(0, (mouseX - trackX) / trackW));
            double val = num.getMin() + ((num.getMax() - num.getMin()) * percentage);
            num.setValue(Math.round(val / num.getIncrement()) * num.getIncrement());
        } else if (setting instanceof ColorSetting && (button == 0 || button == 1)) {
            expandedColorSetting = (ColorSetting) setting;
            Color c = expandedColorSetting.getValue();
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            pickerHue = hsb[0];
            pickerSat = hsb[1];
            pickerBright = hsb[2];
            pickerAlpha = c.getAlpha();
            typingHex = false;
        }
        syncTheme();
    }

    private boolean canOpenHudEditor() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.world != null;
    }

    private boolean isUtilityModule(Module module) {
        return module instanceof ClickGuiModule || module instanceof HudEditorModule;
    }

    private void openHudEditor() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.world == null) {
            return;
        }

        mc.displayGuiScreen(new HudEditorScreen());
    }

    private boolean handleBoxClick(double mouseX, double mouseY, int button) {
        int winWidth = 580;
        int winHeight = 320;
        int winX = width / 2 - winWidth / 2;
        int winY = height / 2 - winHeight / 2;

        int catSpacing = 24;
        int totalCatWidth = (Category.values().length - 1) * catSpacing;
        int startCatX = winX + (winWidth / 2) - (totalCatWidth / 2);

        int i = 0;
        for (Category category : Category.values()) {
            if (category == Category.HIDDEN) continue;
            int cx = startCatX + (i * catSpacing);
            int cy = winY + 15;
            if (mouseX >= cx && mouseX <= cx + 14 && mouseY >= cy && mouseY <= cy + 14 && button == 0) {
                currentCategory = category;
                return true;
            }
            i++;
        }

        int colWidth = 160;
        int colSpacing = 20;
        int startColX = winX + 30;
        int[] colY = new int[]{winY + 75, winY + 75, winY + 75};

        CategoryPanel activePanel = null;
        for (CategoryPanel panel : panels) {
            if (panel.getCategory() == currentCategory) {
                activePanel = panel;
                break;
            }
        }

        if (activePanel != null) {
            int modIndex = 0;
            for (ModuleComponent component : activePanel.getModules()) {
                int col = modIndex % 3;
                int x = startColX + col * (colWidth + colSpacing);
                int y = colY[col];

                if (mouseX >= x && mouseX <= x + colWidth && mouseY >= y && mouseY <= y + 22) {
                    if (button == 0 && !isUtilityModule(component.getModule())) {
                        component.toggle();
                    }

                    if (button == 1) {
                        component.setExpanded(!component.isExpanded());
                    }

                    return true;
                }
                colY[col] += 26;

                if (component.isExpanded()) {
                    if (component.getModule() instanceof HudEditorModule) {
                        int halfW = colWidth / 2;
                        if (mouseY >= colY[col] && mouseY <= colY[col] + 16 && button == 0) {
                            if (mouseX >= x && mouseX <= x + halfW) {
                                currentHudTab = "Editor";
                                return true;
                            } else if (mouseX > x + halfW && mouseX <= x + colWidth) {
                                currentHudTab = "Elements";
                                return true;
                            }
                        }
                        colY[col] += 20;

                        if (currentHudTab.equals("Editor")) {
                            if (mouseX >= x && mouseX <= x + colWidth && mouseY >= colY[col] && mouseY <= colY[col] + 20 && button == 0) {
                                openHudEditor();
                                return true;
                            }
                            colY[col] += 26;
                        } else {
                            if (handleSettingsClickBox(component.getModule(), x, colWidth, colY[col], mouseX, mouseY, button)) return true;
                            for (Setting s : component.getModule().getSettings()) if (s.isVisible()) colY[col] += 20;
                            colY[col] += 6;
                        }
                    } else {
                        if (handleSettingsClickBox(component.getModule(), x, colWidth, colY[col], mouseX, mouseY, button)) return true;
                        for (Setting s : component.getModule().getSettings()) if (s.isVisible()) colY[col] += 20;
                        colY[col] += 6;
                    }
                }
                modIndex++;
            }
        }
        return false;
    }

    private boolean handleSettingsClickBox(Module module, int x, int colWidth, int startY, double mouseX, double mouseY, int button) {
        int y = startY;
        for (Setting setting : module.getSettings()) {
            if (!setting.isVisible()) continue;
            if (mouseX >= x && mouseX <= x + colWidth && mouseY >= y && mouseY <= y + 18) {
                processSettingValue(setting, mouseX, x + 4, colWidth - 8, button);
                return true;
            }
            y += 20;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSV = false;
        draggingHue = false;
        draggingAlpha = false;

        if (layout != Layout.BOX) {
            for (ModuleComponent component : expandedModules) component.setDragging(false);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (expandedColorSetting != null && colorPickerBounds != null) {
            if (draggingSV || draggingHue || draggingAlpha) {
                handleColorPickerDrag(mouseX, mouseY);
                return true;
            }
        }

        if (showThemeEditor) {
            ThemeModule tm = (ThemeModule) Nightlume.getInstance().getModuleManager().getModule(ThemeModule.class);
            if (tm != null) {
                int winW = 200;
                int activeSettings = 0;
                for (Setting s : tm.getSettings()) if (s.isVisible()) activeSettings++;
                int winX = width / 2 - winW / 2;
                int winY = height / 2 - (30 + (activeSettings * 20)) / 2;

                int y = winY + 28;
                for (Setting setting : tm.getSettings()) {
                    if (!setting.isVisible()) continue;
                    if (setting instanceof NumberSetting && mouseX >= winX + 6 && mouseX <= winX + winW - 6 && mouseY >= y && mouseY <= y + 18) {
                        NumberSetting num = (NumberSetting) setting;
                        double percentage = Math.min(1, Math.max(0, (mouseX - (winX + 10)) / (winW - 20)));
                        double val = num.getMin() + ((num.getMax() - num.getMin()) * percentage);
                        num.setValue(Math.round(val / num.getIncrement()) * num.getIncrement());
                        syncTheme();
                        return true;
                    }
                    y += 20;
                }
            }
            return true;
        }

        if (layout == Layout.BOX) {
            int winWidth = 580;
            int winHeight = 320;
            int winX = width / 2 - winWidth / 2;
            int winY = height / 2 - winHeight / 2;
            int colWidth = 160;
            int colSpacing = 20;
            int startColX = winX + 30;
            int[] colY = new int[]{winY + 75, winY + 75, winY + 75};

            for (CategoryPanel panel : panels) {
                if (panel.getCategory() != currentCategory) continue;
                int modIndex = 0;
                for (ModuleComponent component : panel.getModules()) {
                    int col = modIndex % 3;
                    int x = startColX + col * (colWidth + colSpacing);
                    colY[col] += 26;

                    if (component.isExpanded()) {
                        if (component.getModule() instanceof HudEditorModule) {
                            colY[col] += 20;
                            if (currentHudTab.equals("Editor")) colY[col] += 26;
                            else colY[col] = processBoxDrag(component.getModule(), x, colY[col], colWidth, mouseX, mouseY);
                        } else {
                            colY[col] = processBoxDrag(component.getModule(), x, colY[col], colWidth, mouseX, mouseY);
                        }
                    }
                    modIndex++;
                }
            }
        } else {
            for (ModuleComponent component : expandedModules) {
                int setX = component.getSettingsX();
                int setY = component.getSettingsY();
                int setWidth = 160;

                if (component.isDragging()) {
                    int newX = (int) (mouseX - component.getDragX());
                    int newY = (int) (mouseY - component.getDragY());
                    int setHeight = 60 + (component.getModule().getSettings().size() * 18);

                    if (!checkCollision(newX, newY, setWidth, setHeight)) {
                        component.setSettingsX(newX);
                        component.setSettingsY(newY);
                    }
                    return true;
                }

                int settingY = setY + 22;
                if (component.getModule() instanceof HudEditorModule) {
                    settingY += 20;
                    if (!currentHudTab.equals("Editor")) {
                        processFloatDrag(component.getModule(), setX, settingY, setWidth, mouseX, mouseY);
                    }
                } else {
                    processFloatDrag(component.getModule(), setX, settingY, setWidth, mouseX, mouseY);
                }
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void handleColorPickerDrag(double mouseX, double mouseY) {
        int px = colorPickerBounds[0], py = colorPickerBounds[1], pw = colorPickerBounds[2];
        int svX = px + 5, svY = py + 5, svW = pw - 10, svH = 40;

        if (draggingSV) {
            pickerSat = Math.max(0, Math.min(1, (float)(mouseX - svX) / svW));
            pickerBright = 1.0f - Math.max(0, Math.min(1, (float)(mouseY - svY) / svH));
        } else if (draggingHue) {
            pickerHue = Math.max(0, Math.min(1, (float)(mouseX - svX) / svW));
        } else if (draggingAlpha) {
            pickerAlpha = (int) (Math.max(0, Math.min(1, (float)(mouseX - svX) / svW)) * 255);
        }

        Color updated = new Color(Color.HSBtoRGB(pickerHue, pickerSat, pickerBright));
        expandedColorSetting.setValue(new Color(updated.getRed(), updated.getGreen(), updated.getBlue(), pickerAlpha));
        currentHexInput = String.format("%02X%02X%02X", updated.getRed(), updated.getGreen(), updated.getBlue());
        syncTheme();
    }

    private int processBoxDrag(Module module, int x, int startY, int colWidth, double mouseX, double mouseY) {
        int y = startY;
        for (Setting setting : module.getSettings()) {
            if (!setting.isVisible()) continue;
            if (setting instanceof NumberSetting) {
                if (mouseX >= x && mouseX <= x + colWidth && mouseY >= y && mouseY <= y + 18) {
                    NumberSetting num = (NumberSetting) setting;
                    double percentage = Math.min(1, Math.max(0, (mouseX - (x + 4)) / (colWidth - 8)));
                    double val = num.getMin() + ((num.getMax() - num.getMin()) * percentage);
                    num.setValue(Math.round(val / num.getIncrement()) * num.getIncrement());
                }
            } else if (setting instanceof ColorSetting) {
                ColorSetting colorSet = (ColorSetting) setting;
                if (expandedColorSetting == colorSet && (draggingSV || draggingHue || draggingAlpha)) {
                    handleColorPickerDrag(mouseX, mouseY);
                }
            }
            y += 20;
        }
        return y + 6;
    }

    private void processFloatDrag(Module module, int setX, int settingY, int setWidth, double mouseX, double mouseY) {
        int y = settingY;
        for (Setting setting : module.getSettings()) {
            if (!setting.isVisible()) continue;
            if (setting instanceof NumberSetting) {
                if (mouseX >= setX + 6 && mouseX <= setX + setWidth - 6 && mouseY >= y && mouseY <= y + 16) {
                    NumberSetting num = (NumberSetting) setting;
                    double percentage = Math.min(1, Math.max(0, (mouseX - (setX + 10)) / (setWidth - 20)));
                    double val = num.getMin() + ((num.getMax() - num.getMin()) * percentage);
                    num.setValue(Math.round(val / num.getIncrement()) * num.getIncrement());
                }
            } else if (setting instanceof ColorSetting) {
                ColorSetting colorSet = (ColorSetting) setting;
                if (expandedColorSetting == colorSet && (draggingSV || draggingHue || draggingAlpha)) {
                    handleColorPickerDrag(mouseX, mouseY);
                }
            }
            y += 18;
        }
    }

    private boolean checkCollision(int targetX, int targetY, int targetW, int targetH) {
        if (layout == Layout.PANEL) {
            for (CategoryPanel panel : panels) {
                int px = panel.getX();
                int py = panel.getY();
                int pw = 100;
                int ph = 300;
                if (targetX < px + pw && targetX + targetW > px && targetY < py + ph && targetY + targetH > py) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (typingHex && expandedColorSetting != null) {
            String validChars = "0123456789abcdefABCDEF";
            if (validChars.indexOf(codePoint) != -1 && currentHexInput.length() < 6) {
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
                        syncTheme();
                    } catch (NumberFormatException ignored) {}
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

        if (activeBindSetting != null) {
            int key = (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) ? 0 : keyCode;
            activeBindSetting.setKey(key);
            if (activeBindSetting.getName().equalsIgnoreCase("Bind")) bind = key;
            activeBindSetting = null;
            return true;
        }

        if (listeningBind) {
            bind = keyCode;
            listeningBind = false;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (expandedColorSetting != null) {
                expandedColorSetting = null;
                return true;
            }
            if (showThemeEditor) {
                showThemeEditor = false;
                return true;
            }
            if (layout == Layout.PANEL && !expandedModules.isEmpty()) {
                expandedModules.clear();
                return true;
            }
            if (layout == Layout.BOX) {
                boolean closedAModule = false;
                for (CategoryPanel panel : panels) {
                    for (ModuleComponent component : panel.getModules()) {
                        if (component.isExpanded()) {
                            component.setExpanded(false);
                            closedAModule = true;
                        }
                    }
                }
                if (closedAModule) return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private String getKeyName(int keyCode) {
        if (keyCode == 0) return "NONE";
        switch (keyCode) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCTRL";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "RALT";
            case GLFW.GLFW_KEY_LEFT_ALT: return "LALT";
            case GLFW.GLFW_KEY_SPACE: return "SPACE";
            case GLFW.GLFW_KEY_ESCAPE: return "ESC";
            case GLFW.GLFW_KEY_ENTER: return "ENTER";
            case GLFW.GLFW_KEY_TAB: return "TAB";
        }
        String name = GLFW.glfwGetKeyName(keyCode, -1);
        if (name != null) return name.toUpperCase();
        return "KEY " + keyCode;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static int getBind() {
        return bind;
    }
}