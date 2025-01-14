package earth.terrarium.adastra.client.renderers.entities.vehicles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.teamresourceful.resourcefullib.client.CloseablePoseStack;
import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.client.models.entities.vehicles.RocketModel;
import earth.terrarium.adastra.common.entities.vehicles.Rocket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RocketRenderer extends EntityRenderer<Rocket> {

    public static final ResourceLocation TIER_1_TEXTURE = new ResourceLocation(AdAstra.MOD_ID, "textures/entity/rocket/tier_1_rocket.png");
    public static final ResourceLocation TIER_2_TEXTURE = new ResourceLocation(AdAstra.MOD_ID, "textures/entity/rocket/tier_2_rocket.png");
    public static final ResourceLocation TIER_3_TEXTURE = new ResourceLocation(AdAstra.MOD_ID, "textures/entity/rocket/tier_3_rocket.png");
    public static final ResourceLocation TIER_4_TEXTURE = new ResourceLocation(AdAstra.MOD_ID, "textures/entity/rocket/tier_4_rocket.png");

    protected final EntityModel<Rocket> model;
    private final ResourceLocation texture;

    public RocketRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, ResourceLocation texture) {
        super(context);
        this.shadowRadius = 0.5f;
        this.model = new RocketModel<>(context.bakeLayer(layer));
        this.texture = texture;
    }

    @Override
    public void render(Rocket entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        try (var pose = new CloseablePoseStack(poseStack)) {
            // Shake rocket constantly
            if (!Minecraft.getInstance().isPaused() && (entity.isLaunching() || entity.hasLaunched())) {
                entityYaw += (float) (entity.level().random.nextGaussian() * 0.3);
            }

            pose.translate(0.0F, 1.55F, 0.0F);
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
            float xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
            pose.mulPose(Axis.ZP.rotationDegrees(-xRot));
            pose.scale(-1.0F, -1.0F, 1.0F);
            model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            VertexConsumer consumer = buffer.getBuffer(model.renderType(getTextureLocation(entity)));
            model.renderToBuffer(pose, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Rocket entity) {
        return texture;
    }

    public static class ItemRenderer extends BlockEntityWithoutLevelRenderer {

        private final ModelLayerLocation layer;
        private final ResourceLocation texture;

        private EntityModel<?> model;

        public ItemRenderer(ModelLayerLocation layer, ResourceLocation texture) {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
            this.layer = layer;
            this.texture = texture;
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
            if (model == null) {
                model = new RocketModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(layer));
            }
            var consumer = buffer.getBuffer(RenderType.entityCutoutNoCullZOffset(texture));
            try (var pose = new CloseablePoseStack(poseStack)) {
                pose.mulPose(Axis.ZP.rotationDegrees(180));
                pose.translate(0.0, -1.501, 0.0);
                model.renderToBuffer(pose, consumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }
}
