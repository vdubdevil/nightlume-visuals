package ru.nightlume.render.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class BlurUtil {

    private static final String FRAGMENT_SHADER =
            "#version 120\n" +
                    "uniform sampler2D texture;\n" +
                    "uniform vec2 texelSize;\n" +
                    "uniform float radius;\n" +
                    "void main() {\n" +
                    "    vec4 color = vec4(0.0);\n" +
                    "    float total = 0.0;\n" +
                    "    for(float x = -radius; x <= radius; x++) {\n" +
                    "        for(float y = -radius; y <= radius; y++) {\n" +
                    "            vec2 offset = vec2(x, y) * texelSize;\n" +
                    "            float weight = exp(-(x*x + y*y) / (2.0 * radius * radius));\n" +
                    "            color += texture2D(texture, gl_TexCoord[0].st + offset) * weight;\n" +
                    "            total += weight;\n" +
                    "        }\n" +
                    "    }\n" +
                    "    gl_FragColor = color / total;\n" +
                    "}";

    private static int programId = -1;

    public static void drawBlurredBackground(float x, float y, float width, float height, float radius) {
        Minecraft mc = Minecraft.getInstance();
        Framebuffer framebuffer = mc.getFramebuffer();

        if (programId == -1) {
            initShader();
        }

        int scaleFactor = (int) mc.getMainWindow().getGuiScaleFactor();
        int scissorX = (int) (x * scaleFactor);
        int scissorY = (int) (mc.getMainWindow().getFramebufferHeight() - (y + height) * scaleFactor);
        int scissorW = (int) (width * scaleFactor);
        int scissorH = (int) (height * scaleFactor);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.enableTexture();

        framebuffer.bindFramebufferTexture();

        GL20.glUseProgram(programId);

        int textureUniform = GL20.glGetUniformLocation(programId, "texture");
        int texelSizeUniform = GL20.glGetUniformLocation(programId, "texelSize");
        int radiusUniform = GL20.glGetUniformLocation(programId, "radius");

        GL20.glUniform1i(textureUniform, 0);
        GL20.glUniform2f(texelSizeUniform, 1.0F / mc.getMainWindow().getFramebufferWidth(), 1.0F / mc.getMainWindow().getFramebufferHeight());
        GL20.glUniform1f(radiusUniform, radius);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex2f(0, 0);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(0, mc.getMainWindow().getScaledHeight());
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex2f(mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight());
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex2f(mc.getMainWindow().getScaledWidth(), 0);
        GL11.glEnd();

        GL20.glUseProgram(0);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        RenderSystem.disableBlend();
    }

    private static void initShader() {
        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, FRAGMENT_SHADER);
        GL20.glCompileShader(fragmentShader);

        programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, fragmentShader);
        GL20.glLinkProgram(programId);
    }
}