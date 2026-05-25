package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Random;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import ru.nightlume.Nightlume;
import ru.nightlume.module.impl.misc.ItemPhysicsModule;

public class ItemRenderer extends EntityRenderer<ItemEntity> {

    private final net.minecraft.client.renderer.ItemRenderer itemRenderer;
    private final Random random = new Random();

    public ItemRenderer(EntityRendererManager renderManagerIn, net.minecraft.client.renderer.ItemRenderer itemRendererIn) {
        super(renderManagerIn);
        this.itemRenderer = itemRendererIn;
        this.shadowSize = 0.15F;
        this.shadowOpaque = 0.75F;
    }

    private int getModelCount(ItemStack stack) {
        int i = 1;

        if (stack.getCount() > 48) {
            i = 5;
        } else if (stack.getCount() > 32) {
            i = 4;
        } else if (stack.getCount() > 16) {
            i = 3;
        } else if (stack.getCount() > 1) {
            i = 2;
        }

        return i;
    }

    public void render(ItemEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.push();

        ItemStack itemstack = entityIn.getItem();
        int seed = itemstack.isEmpty() ? 187 : Item.getIdFromItem(itemstack.getItem()) + itemstack.getDamage();
        this.random.setSeed((long) seed);

        IBakedModel model = this.itemRenderer.getItemModelWithOverrides(itemstack, entityIn.world, (LivingEntity) null);
        boolean gui3d = model.isGui3d();
        int count = this.getModelCount(itemstack);

        ItemPhysicsModule physics = (ItemPhysicsModule) Nightlume.getInstance().getModuleManager().getModule(ItemPhysicsModule.class);
        boolean customPhysics = physics != null && physics.isEnabled() && !physics.isDefault();

        if (customPhysics) {
            physics.updateVisibility();
            renderCustomPhysics(entityIn, partialTicks, matrixStackIn, bufferIn, packedLightIn, itemstack, model, gui3d, count, physics);
        } else {
            renderVanilla(entityIn, partialTicks, matrixStackIn, bufferIn, packedLightIn, itemstack, model, gui3d, count);
        }

        matrixStackIn.pop();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }
    private void renderVanilla(ItemEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, ItemStack itemstack, IBakedModel model, boolean gui3d, int count) {
        float bob = MathHelper.sin(((float) entityIn.getAge() + partialTicks) / 10.0F + entityIn.hoverStart) * 0.1F + 0.1F;

        if (!this.shouldBob()) {
            bob = 0.0F;
        }

        float groundY = model.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.getY();
        matrixStackIn.translate(0.0D, bob + 0.25F * groundY, 0.0D);

        float hover = entityIn.getItemHover(partialTicks);
        matrixStackIn.rotate(Vector3f.YP.rotation(hover));

        renderItemStack(matrixStackIn, bufferIn, packedLightIn, itemstack, model, gui3d, count, true);
    }

    private void renderCustomPhysics(ItemEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, ItemStack itemstack, IBakedModel model, boolean gui3d, int count, ItemPhysicsModule physics) {
        matrixStackIn.scale(physics.getScale(), physics.getScale(), physics.getScale());

        if (physics.isLying()) {
            matrixStackIn.translate(0.0D, physics.getLyingOffsetY(), 0.0D);

            float yaw = physics.lyingRandomRotation.getValue() ? getStableYaw(entityIn) : physics.getLyingRotation();

            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(yaw));
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(physics.getLyingAngle()));
        }

        if (physics.isFlat2D()) {
            matrixStackIn.translate(0.0D, physics.getFlatOffsetY(), 0.0D);

            if (physics.flatFaceCamera.getValue()) {
                matrixStackIn.rotate(this.renderManager.getCameraOrientation());
            }

            float speed = physics.getFlatRotationSpeed();

            if (speed > 0.0F) {
                float rotation = ((entityIn.ticksExisted + partialTicks) * speed) % 360.0F;
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(rotation));
            }
        }

        renderItemStack(matrixStackIn, bufferIn, packedLightIn, itemstack, model, gui3d, count, physics.stackSpread.getValue());
    }

    private void renderItemStack(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, ItemStack itemstack, IBakedModel model, boolean gui3d, int count, boolean spread) {
        float scaleX = model.getItemCameraTransforms().ground.scale.getX();
        float scaleY = model.getItemCameraTransforms().ground.scale.getY();
        float scaleZ = model.getItemCameraTransforms().ground.scale.getZ();

        if (!gui3d) {
            float x = -0.0F * (float) (count - 1) * 0.5F * scaleX;
            float y = -0.0F * (float) (count - 1) * 0.5F * scaleY;
            float z = -0.09375F * (float) (count - 1) * 0.5F * scaleZ;
            matrixStackIn.translate(x, y, z);
        }

        for (int i = 0; i < count; ++i) {
            matrixStackIn.push();

            if (i > 0) {
                if (gui3d) {
                    float x = spread ? (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F : 0.0F;
                    float y = spread ? (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F : 0.0F;
                    float z = spread ? (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F : 0.0F;
                    matrixStackIn.translate(x, y, z);
                } else {
                    float x = spread ? (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F : 0.0F;
                    float y = spread ? (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F : 0.0F;
                    matrixStackIn.translate(x, y, 0.0D);
                }
            }

            this.itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND, false, matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, model);
            matrixStackIn.pop();

            if (!gui3d) {
                matrixStackIn.translate(0.0F * scaleX, 0.0F * scaleY, 0.09375F * scaleZ);
            }
        }
    }

    private float getStableYaw(ItemEntity entity) {
        int hash = entity.getEntityId() * 31 + 17;
        return Math.abs(hash % 360);
    }
    public ResourceLocation getEntityTexture(ItemEntity entity)
    {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }

    public boolean shouldSpreadItems()
    {
        return true;
    }

    public boolean shouldBob()
    {
        return true;
    }
}
