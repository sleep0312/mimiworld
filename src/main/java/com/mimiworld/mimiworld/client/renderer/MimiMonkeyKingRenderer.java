package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.MimiMonkeyKingModel;
import com.mimiworld.mimiworld.entity.MimiMonkeyKingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 米米猴王渲染器：绑定 {@link MimiMonkeyKingModel}，阴影半径 0.5 对齐 0.7 宽碰撞箱。
 *
 * <p>金箍棒是 geo 模型里 {@code staff} 骨骼的一部分（7 个方块，立方体坐标直接对齐
 * 右手掌心），随骨骼矩阵一起变换，因此始终与手部严丝合缝，不会再因为「另外用
 * {@code ItemRenderer} 渲染一个物品模型」而出现贴图坐标/显示变换对不上手的问题。</p>
 *
 * <p>掷棍脱手期间（{@link MimiMonkeyKingEntity#isStaffOut()}）把 {@code staff} 骨骼
 * 设为隐藏，避免与飞行的 ThrownMonkeyStaffEntity 同时出现两根棒；
 * GeckoLib 的 {@code renderCubesOfBone} 会跳过隐藏骨骼的方块。</p>
 */
public class MimiMonkeyKingRenderer extends GeoEntityRenderer<MimiMonkeyKingEntity> {

    public MimiMonkeyKingRenderer(EntityRendererProvider.Context context) {
        super(context, new MimiMonkeyKingModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public void renderRecursively(PoseStack poseStack, MimiMonkeyKingEntity animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                                  int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if ("staff".equals(bone.getName())) {
            bone.setHidden(animatable.isStaffOut());
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
    }
}
