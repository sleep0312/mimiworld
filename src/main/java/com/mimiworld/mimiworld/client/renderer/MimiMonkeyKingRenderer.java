package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.client.model.MimiMonkeyKingModel;
import com.mimiworld.mimiworld.entity.MimiMonkeyKingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 米米猴王渲染器：绑定 {@link MimiMonkeyKingModel}，阴影半径 0.5 对齐 0.7 宽碰撞箱。
 *
 * <p>手中金箍棒：geo 内的 staff 骨只是空挂载点，在该骨位置渲染
 * {@link ModItems#MONKEY_KING_RUYI_STAFF} 物品本身。</p>
 *
 * <p><b>必须在模型渲染帧内绘制</b>：GeckoLib 的实体模型渲染（{@code defaultRender}）在内部
 * push/pop 里应用了实体朝向旋转（{@code applyRotations}）与全部骨骼矩阵；而
 * {@code EntityRenderDispatcher.render} 只做了位移、没有朝向旋转。若在 {@code super.render()}
 * 之后按调度器帧直接绘制，手棒会以固定的世界轴向偏移悬浮在猴王身侧，不随实体与动画转动。</p>
 *
 * <p><b>且必须等模型全部构建完再绘制</b>：1.20.1 的 {@code MultiBufferSource.BufferSource} 对
 * 所有「非方块层」的 RenderType 共用同一个 {@code BufferBuilder}（{@code this.builder}）。
 * GeckoLib 把整只猴王模型写进这同一个 buffer；若在 {@code renderRecursively} 中途（staff 骨处）
 * 调用 {@code ItemRenderer.renderStatic}，它会 {@code getBuffer(金箍棒 RenderType)}，
 * 因类型不同而强制把猴王的「半成品批次」提前 flush、再把同一个 builder 以金箍棒 RenderType
 * 重新 begin —— 于是猴王后半段顶点会被当作金箍棒几何、用金箍棒贴图绘制，表现为猴王纹理错乱。</p>
 *
 * <p>因此这里分两步：{@code renderRecursively} 只记录 staff 骨相对实体基础帧的局部矩阵；
 * 等到 {@code renderFinal}（此时模型已全部写完、猴王批次完整）再绘制手棒，
 * 此时切换 RenderType 只会把完整的猴王批次正确 flush，不再串色。</p>
 *
 * <p>绘制用 vanilla {@code ItemRenderer.renderStatic}：它会自行套用 item json 的 display
 * 变换（thirdperson_righthand），与玩家第三视角手持姿势完全一致。棒的角度/位置继续在
 * {@code models/item/monkey_king_ruyi_staff.json} 的 display 节点里调即可生效。</p>
 *
 * <p>掷棍脱手期间（{@link MimiMonkeyKingEntity#isStaffOut()}）跳过手持棒，
 * 避免与飞行的 ThrownMonkeyStaffEntity 出现两根棒。</p>
 */
public class MimiMonkeyKingRenderer extends GeoEntityRenderer<MimiMonkeyKingEntity> {

    private static final ItemStack STAFF_STACK = new ItemStack(ModItems.MONKEY_KING_RUYI_STAFF.get());

    /** staff 骨相对「实体基础帧」（调度器位移帧，等于 {@code entityRenderTranslations}）的局部矩阵。 */
    private Matrix4f staffLocalMatrix;

    public MimiMonkeyKingRenderer(EntityRendererProvider.Context context) {
        super(context, new MimiMonkeyKingModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public void renderRecursively(PoseStack poseStack, MimiMonkeyKingEntity animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                                  int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (!isReRender && !animatable.isStaffOut() && "staff".equals(bone.getName())) {
            // 此刻 poseStack 已处于 staff 骨的完整变换帧（朝向旋转 + 骨骼动画均在内）。
            // 换算成相对实体基础帧的局部矩阵，供 renderFinal 里重建同一位姿。
            Matrix4f boneMatrix = new Matrix4f(poseStack.last().pose());
            this.staffLocalMatrix = new Matrix4f(this.entityRenderTranslations).invert().mul(boneMatrix);
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void renderFinal(PoseStack poseStack, MimiMonkeyKingEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                            VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        Matrix4f local = this.staffLocalMatrix;
        this.staffLocalMatrix = null;
        if (local == null)
            return;

        // renderFinal 的 poseStack 已退回实体基础帧，补上局部矩阵即可还原到 staff 骨位姿。
        poseStack.pushPose();
        poseStack.mulPoseMatrix(local);
        Minecraft.getInstance().getItemRenderer().renderStatic(STAFF_STACK, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, animatable.level(), animatable.getId());
        poseStack.popPose();
    }
}