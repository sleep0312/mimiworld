package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.ThrownMonkeyStaffModel;
import com.mimiworld.mimiworld.entity.ThrownMonkeyStaffEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 飞行金箍棒渲染器：geo 中棒体沿 Z 轴躺平，这里只做 Y 轴朝向旋转，
 * 让棒头始终对准飞行方向（yaw 在实体 tick 中由速度向量计算）。
 */
public class ThrownMonkeyStaffRenderer extends GeoEntityRenderer<ThrownMonkeyStaffEntity> {

    public ThrownMonkeyStaffRenderer(EntityRendererProvider.Context context) {
        super(context, new ThrownMonkeyStaffModel());
        this.shadowRadius = 0.3F;
    }

    @Override
    protected void applyRotations(ThrownMonkeyStaffEntity entity, PoseStack poseStack,
                                  float ageInTicks, float rotationYaw, float partialTick) {
        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(180.0F - yaw)));
        // 绕棒体长轴轻微自转，飞行时棒头箍有视觉变化
        poseStack.mulPose(new Quaternionf().rotationZ(ageInTicks * 0.4F));
    }
}
