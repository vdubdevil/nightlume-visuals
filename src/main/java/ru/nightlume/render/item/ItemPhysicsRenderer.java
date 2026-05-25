package ru.nightlume.render.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import ru.nightlume.Nightlume;
import ru.nightlume.module.impl.misc.ItemPhysicsModule;

public class ItemPhysicsRenderer {

    private static final Minecraft mc = Minecraft.getInstance();

    public static void apply(ItemEntity entity, MatrixStack stack, IBakedModel model) {
        ItemPhysicsModule module = (ItemPhysicsModule) Nightlume.getInstance()
                .getModuleManager()
                .getModule(ItemPhysicsModule.class);

        if (module == null || !module.isEnabled() || module.isDefault()) {
            return;
        }

        module.updateVisibility();

        ItemStack itemStack = entity.getItem();
        if (itemStack.isEmpty()) {
            return;
        }

        float scale = module.getScale();

        stack.scale(scale, scale, scale);

        if (module.isLying()) {
            applyLying(entity, stack, module);
            return;
        }

        if (module.isFlat2D()) {
            applyFlat2D(entity, stack, module);
        }
    }

    private static void applyLying(ItemEntity entity, MatrixStack stack, ItemPhysicsModule module) {
        stack.translate(0.0D, module.getLyingOffsetY(), 0.0D);

        float yaw = module.lyingRandomRotation.getValue()
                ? getStableRandomYaw(entity)
                : module.getLyingRotation();

        stack.rotate(Vector3f.YP.rotationDegrees(yaw));
        stack.rotate(Vector3f.XP.rotationDegrees(module.getLyingAngle()));
    }

    private static void applyFlat2D(ItemEntity entity, MatrixStack stack, ItemPhysicsModule module) {
        stack.translate(0.0D, module.getFlatOffsetY(), 0.0D);

        if (module.flatFaceCamera.getValue() && mc.getRenderManager() != null) {
            stack.rotate(mc.getRenderManager().getCameraOrientation());
        }

        float speed = module.getFlatRotationSpeed();
        if (speed > 0.0F) {
            float rotation = ((entity.ticksExisted + mc.getRenderPartialTicks()) * speed) % 360.0F;
            stack.rotate(Vector3f.ZP.rotationDegrees(rotation));
        }
    }

    private static float getStableRandomYaw(ItemEntity entity) {
        int id = entity.getEntityId();
        int hash = id * 31 + 17;
        return Math.abs(hash % 360);
    }
}