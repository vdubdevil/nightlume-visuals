package ru.nightlume.render.font;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class CustomFontRenderer {

    private final Font font;

    private final Map<Character, Glyph> glyphs = new HashMap<>();

    public CustomFontRenderer(Font font) {
        this.font = font;
        generateGlyphs();
    }

    private void generateGlyphs() {

        FontRenderContext context =
                new FontRenderContext(null, true, true);

        for (char character = 32; character < 256; character++) {

            Rectangle2D bounds =
                    font.getStringBounds(
                            String.valueOf(character),
                            context
                    );

            int width = Math.max(1, (int) Math.ceil(bounds.getWidth()));
            int height = Math.max(1, (int) Math.ceil(bounds.getHeight()));

            BufferedImage image =
                    new BufferedImage(
                            width,
                            height,
                            BufferedImage.TYPE_INT_ARGB
                    );

            Graphics2D graphics = image.createGraphics();

            graphics.setFont(font);
            graphics.setColor(Color.WHITE);

            graphics.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );

            graphics.drawString(
                    String.valueOf(character),
                    0,
                    graphics.getFontMetrics().getAscent()
            );

            graphics.dispose();

            int texture = uploadTexture(image);

            glyphs.put(character,
                    new Glyph(texture, width, height));
        }
    }

    public void drawString(String text,
                           float x,
                           float y,
                           int color) {

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GL11.glColor4f(red, green, blue, alpha);

        float currentX = x;

        for (char character : text.toCharArray()) {

            Glyph glyph = glyphs.get(character);

            if (glyph == null) {
                continue;
            }

            RenderSystem.bindTexture(glyph.texture);

            drawTexture(
                    currentX,
                    y,
                    glyph.width,
                    glyph.height
            );

            currentX += glyph.width - 2;
        }

        RenderSystem.disableBlend();
    }

    public int getWidth(String text) {

        int width = 0;

        for (char character : text.toCharArray()) {

            Glyph glyph = glyphs.get(character);

            if (glyph != null) {
                width += glyph.width - 2;
            }
        }

        return width;
    }

    private int uploadTexture(BufferedImage image) {

        int[] pixels =
                new int[image.getWidth() * image.getHeight()];

        image.getRGB(
                0,
                0,
                image.getWidth(),
                image.getHeight(),
                pixels,
                0,
                image.getWidth()
        );

        ByteBuffer buffer =
                BufferUtils.createByteBuffer(
                        image.getWidth() * image.getHeight() * 4
                );

        for (int pixel : pixels) {

            buffer.put((byte) ((pixel >> 16) & 255));
            buffer.put((byte) ((pixel >> 8) & 255));
            buffer.put((byte) (pixel & 255));
            buffer.put((byte) ((pixel >> 24) & 255));
        }

        buffer.flip();

        int texture = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MIN_FILTER,
                GL11.GL_LINEAR
        );

        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MAG_FILTER,
                GL11.GL_LINEAR
        );

        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA,
                image.getWidth(),
                image.getHeight(),
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                buffer
        );

        return texture;
    }

    private void drawTexture(float x,
                             float y,
                             float width,
                             float height) {

        Tessellator tessellator =
                Tessellator.getInstance();

        BufferBuilder buffer =
                tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS,
                DefaultVertexFormats.POSITION_TEX);

        buffer.pos(x, y + height, 0)
                .tex(0, 1)
                .endVertex();

        buffer.pos(x + width, y + height, 0)
                .tex(1, 1)
                .endVertex();

        buffer.pos(x + width, y, 0)
                .tex(1, 0)
                .endVertex();

        buffer.pos(x, y, 0)
                .tex(0, 0)
                .endVertex();

        tessellator.draw();
    }

    private static class Glyph {

        private final int texture;
        private final int width;
        private final int height;

        private Glyph(int texture,
                      int width,
                      int height) {

            this.texture = texture;
            this.width = width;
            this.height = height;
        }
    }
}