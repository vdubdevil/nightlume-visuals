package ru.nightlume.render.util;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

public class RoundedUtil {

    public static void drawRound(float x,
                                 float y,
                                 float width,
                                 float height,
                                 float radius,
                                 int color) {

        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glBegin(GL11.GL_POLYGON);
        GL11.glColor4f(red, green, blue, alpha);

        for (int i = 180; i >= 90; i -= 3) {
            GL11.glVertex2d(
                    x + radius + Math.cos(Math.toRadians(i)) * radius,
                    y + height - radius + Math.sin(Math.toRadians(i)) * radius
            );
        }

        for (int i = 90; i >= 0; i -= 3) {
            GL11.glVertex2d(
                    x + width - radius + Math.cos(Math.toRadians(i)) * radius,
                    y + height - radius + Math.sin(Math.toRadians(i)) * radius
            );
        }

        for (int i = 360; i >= 270; i -= 3) {
            GL11.glVertex2d(
                    x + width - radius + Math.cos(Math.toRadians(i)) * radius,
                    y + radius + Math.sin(Math.toRadians(i)) * radius
            );
        }

        for (int i = 270; i >= 180; i -= 3) {
            GL11.glVertex2d(
                    x + radius + Math.cos(Math.toRadians(i)) * radius,
                    y + radius + Math.sin(Math.toRadians(i)) * radius
            );
        }

        GL11.glEnd();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_CULL_FACE);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRoundTop(float x,
                                    float y,
                                    float width,
                                    float height,
                                    float radius,
                                    int color) {

        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glBegin(GL11.GL_POLYGON);
        GL11.glColor4f(red, green, blue, alpha);

        GL11.glVertex2d(x, y + height);

        GL11.glVertex2d(x + width, y + height);

        for (int i = 360; i >= 270; i -= 3) {
            GL11.glVertex2d(
                    x + width - radius + Math.cos(Math.toRadians(i)) * radius,
                    y + radius + Math.sin(Math.toRadians(i)) * radius
            );
        }

        for (int i = 270; i >= 180; i -= 3) {
            GL11.glVertex2d(
                    x + radius + Math.cos(Math.toRadians(i)) * radius,
                    y + radius + Math.sin(Math.toRadians(i)) * radius
            );
        }

        GL11.glEnd();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_CULL_FACE);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}