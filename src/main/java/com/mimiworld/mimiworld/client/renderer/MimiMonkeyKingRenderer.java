package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.client.model.MimiMonkeyKingModel;
import com.mimiworld.mimiworld.client.model.MonkeyKingRuyiStaffModel;
import com.mimiworld.mimiworld.entity.MimiMonkeyKingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtils;

/**
 * 米米猴王渲染器：绑定 {@link MimiMonkeyKingModel}，阴影半径 0.5 对齐 0.7 宽碰撞箱。
 *
 * <p>手中金箍棒：geo 内的 staff 骨只是空挂载点，在该骨位置渲染
 * {@link ModItems#MONKEY_KING_RUYI_STAFF} 物品本身（GeoItemRenderer）。
 * GeckoLib 物品渲染器不读取 item json 的 display 变换，且内部会预平移 (0.5,0.5,0.5)，
 * 因此这里手动按 vanilla 物品变换链套上 thirdperson_righthand 显示设置
 * （与玩家第三视角手持姿势一致，之后继续在 item model json 中调整即可生效）。</p>
 *
 * <p>掷棍脱手期间（{@link MimiMonkeyKingEntity#isStaffOut()}）跳过手持棒，
 * 避免与飞行的 ThrownMonkeyStaffEntity 出现两根棒。</p>
 */
public class MimiMonkeyKingRenderer extends GeoEntityRenderer<MimiMonkeyKingEntity> {

    private static final ItemStack STAFF_STACK = new ItemStack(ModItems.MONKEY_KING_RUYI_STAFF.get());

    private GeoItemRenderer<?> staffItemRenderer;

    public MimiMonkeyKingRenderer(EntityRendererProvider.Context context) {
        super(context, new MimiMonkeyKingModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(MimiMonkeyKingEntity entity, float entityYaw, float partialTick,
                      PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        if (entity.isStaffOut()) {
            return;
        }
        this.model.getBone("staff").ifPresent(bone -> renderStaffInHand(poseStack, bufferSource, packedLight, bone));
    }

    private void renderStaffInHand(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, GeoBone bone) {
        Minecraft mc = Minecraft.getInstance();

        if (this.staffItemRenderer == null) {
            this.staffItemRenderer = new GeoItemRenderer<com.mimiworld.mimiworld.item.MonkeyKingRuyiStaffItem>(
                    new MonkeyKingRuyiStaffModel());
        }

        poseStack.pushPose();
        // 对齐 staff 骨当前帧的位置与旋转（含动画结果）
        RenderUtils.translateAndRotateMatrixForBone(poseStack, bone);

        // 读取 thirdperson_righthand 显示变换（item model json 改动实时生效）
        BakedModel baked = mc.getItemRenderer().getModel(STAFF_STACK, mc.level, null, 0);
        ItemTransform transform = baked.getTransforms().getTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);

        // 等效 vanilla 的 T(0.5)·D·T(-0.5) 物品显示变换；末尾再 -0.5 抵消 GeckoLib preRender 的 +0.5
        poseStack.translate(0.5D, 0.5D, 0.5D);
        transform.apply(false, poseStack);
        poseStack.translate(-1.0D, -1.0D, -1.0D);

        this.staffItemRenderer.renderByItem(STAFF_STACK, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
