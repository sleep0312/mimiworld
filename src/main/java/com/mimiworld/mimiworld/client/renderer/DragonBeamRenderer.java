package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.entity.DragonBeamEntity;
import com.mimiworld.mimiworld.entity.DibazongzuilongEntity;
import com.mimiworld.mimiworld.entity.LaziestFinalDragonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 最懒光束渲染器：把 {@link DragonBeamEntity} 渲染成一条从最懒终末龙嘴部直达
 * 锁定玩家的实心紫色光束，而不是用粒子拼。
 *
 * <p>做法：取消实体渲染调度器自带的位移，把坐标原点移到龙嘴的相机相对位置，
 * 用 {@link Quaternionf#rotationTo} 把局部 +Y 轴对齐「嘴→目标」方向，然后沿轴向
 * 铺两层四棱光柱：外层半透明紫光晕（半径脉动）+ 内层亮白紫核心。纹理复用原版
 * 守卫者光束（16×1024 竖向流光），乘以紫色 tint，V 坐标随时间滚动产生流动感，
 * 全程满亮度（自发光观感）。起点与终点都做插值，高速下光束依然顺滑。</p>
 */
public class DragonBeamRenderer extends EntityRenderer<DragonBeamEntity> {

    /** 原版守卫者光束纹理（竖向 16×1024 流光条），乘紫色 tint 即为末影紫光束 */
    private static final ResourceLocation BEAM_TEXTURE =
            new ResourceLocation("textures/entity/guardian_beam.png");
    private static final RenderType BEAM_RENDER_TYPE = RenderType.entitySmoothCutout(BEAM_TEXTURE);
    private static final int FULL_BRIGHT = 0x00F000F0;

    /** 四棱光柱四个角在局部 XZ 平面上的方向（对角摆放，旋转对称） */
    private static final float[] CORNER_X = {1.0F, 0.0F, -1.0F, 0.0F};
    private static final float[] CORNER_Z = {0.0F, 1.0F, 0.0F, -1.0F};

    public DragonBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonBeamEntity beam) {
        return BEAM_TEXTURE;
    }

    @Override
    public void render(DragonBeamEntity beam, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        Entity owner = beam.getOwner();
        Vec3 start;
        if (owner instanceof LaziestFinalDragonEntity laziest) {
            start = laziest.getMouthPosition(partialTick);
        } else if (owner instanceof DibazongzuilongEntity zuiLong) {
            start = zuiLong.getMouthPosition(partialTick);
        } else {
            return;
        }
        Player target = beam.getTargetPlayer();
        if (target == null || !target.isAlive() || target.isSpectator() || target.isCreative()) {
            return;
        }

        // 起点：龙嘴（按龙头骨骼在模型空间的位置换算，非实体中心）；终点：目标躯干，二者均做帧间插值
        Vec3 end = target.getPosition(partialTick).add(0.0D, target.getBbHeight() * 0.55D, 0.0D);

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        Vec3 startRel = start.subtract(camPos);
        Vec3 endRel = end.subtract(camPos);

        Vec3 dir = endRel.subtract(startRel);
        double length = dir.length();
        if (length < 0.1D) {
            return;
        }
        Vec3 axis = dir.scale(1.0D / length);

        // 渲染调度器已把原点平移到光束实体（吸附在龙中心），先抵消再移到龙嘴
        Vec3 selfRel = beam.getPosition(partialTick).subtract(camPos);
        poseStack.pushPose();
        poseStack.translate(-selfRel.x, -selfRel.y, -selfRel.z);
        poseStack.translate(startRel.x, startRel.y, startRel.z);
        poseStack.mulPose(new Quaternionf().rotationTo(
                new Vector3f(0.0F, 1.0F, 0.0F),
                new Vector3f((float) axis.x, (float) axis.y, (float) axis.z)));

        VertexConsumer consumer = bufferSource.getBuffer(BEAM_RENDER_TYPE);
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        float time = beam.level().getGameTime() + partialTick;
        int segments = (int) Mth.clamp(length * 2.2D, 8.0D, 64.0D);

        // 外层紫光晕：半径 0.18、脉动、半透明
        this.emitLayer(consumer, pose, normal, (float) length, segments, time,
                0.185F, 0.035F, 0.52F, 0.12F, 1.0F, 0.50F, 0.85F, 0.30F);
        // 中层紫束：半径 0.10
        this.emitLayer(consumer, pose, normal, (float) length, segments, time * 1.15F,
                0.105F, 0.020F, 0.66F, 0.28F, 1.0F, 0.80F, 1.35F, 0.55F);
        // 内层亮白紫核心：半径 0.05、接近不透明
        this.emitLayer(consumer, pose, normal, (float) length, segments, time * 1.4F,
                0.055F, 0.010F, 0.88F, 0.70F, 1.0F, 0.95F, 2.10F, 0.85F);

        poseStack.popPose();
    }

    /**
     * 沿局部 Y 轴（已对齐光束方向）铺一段四棱光柱。
     *
     * @param radius      基础半径（格）
     * @param pulse       半径脉动幅度
     * @param red/green/blue/Alpha 颜色与整体不透明度（0~1）
     * @param vScale      纹理 V 坐标沿光束方向的拉伸（越大流光越密）
     * @param vScrollMul  V 坐标滚动速度倍数
     */
    private void emitLayer(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, float length, int segments,
                           float time, float radius, float pulse,
                           float red, float green, float blue, float alpha,
                           float vScale, float endAlpha) {
        float vScroll = time * 0.55F;
        for (int i = 0; i < segments; i++) {
            float t0 = i / (float) segments;
            float t1 = (i + 1) / (float) segments;
            float y0 = t0 * length;
            float y1 = t1 * length;
            float rad0 = radius + Mth.sin(time * 0.35F + t0 * 6.0F) * pulse;
            float rad1 = radius + Mth.sin(time * 0.35F + t1 * 6.0F) * pulse;
            float fade0 = this.endFade(t0) * endAlpha + (1.0F - endAlpha);
            float fade1 = this.endFade(t1) * endAlpha + (1.0F - endAlpha);
            int a0 = (int) (Mth.clamp(alpha * fade0, 0.0F, 1.0F) * 255.0F);
            int a1 = (int) (Mth.clamp(alpha * fade1, 0.0F, 1.0F) * 255.0F);
            int ri = (int) (red * 255.0F);
            int gi = (int) (green * 255.0F);
            int bi = (int) (blue * 255.0F);

            for (int side = 0; side < 4; side++) {
                int next = (side + 1) % 4;
                float cx0 = CORNER_X[side];
                float cz0 = CORNER_Z[side];
                float cx1 = CORNER_X[next];
                float cz1 = CORNER_Z[next];

                float u0 = side * 0.25F;
                float u1 = next * 0.25F;
                float v0 = vScroll + y0 * vScale;
                float v1 = vScroll + y1 * vScale;

                float nx = (cx0 + cx1) * 0.5F;
                float nz = (cz0 + cz1) * 0.5F;
                float invLen = Mth.invSqrt(nx * nx + nz * nz);
                nx *= invLen;
                nz *= invLen;

                // 下环前角 → 下环次角 → 上环次角 → 上环前角（entitySmoothCutout 关闭背面剔除，顺序安全）
                consumer.vertex(pose, cx0 * rad0, y0, cz0 * rad0)
                        .color(ri, gi, bi, a0).uv(u0, v0)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT)
                        .normal(normal, nx, 0.0F, nz).endVertex();
                consumer.vertex(pose, cx1 * rad0, y0, cz1 * rad0)
                        .color(ri, gi, bi, a0).uv(u1, v0)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT)
                        .normal(normal, nx, 0.0F, nz).endVertex();
                consumer.vertex(pose, cx1 * rad1, y1, cz1 * rad1)
                        .color(ri, gi, bi, a1).uv(u1, v1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT)
                        .normal(normal, nx, 0.0F, nz).endVertex();
                consumer.vertex(pose, cx0 * rad1, y1, cz0 * rad1)
                        .color(ri, gi, bi, a1).uv(u0, v1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT)
                        .normal(normal, nx, 0.0F, nz).endVertex();
            }
        }
    }

    /** 光束两端各 8% 长度内不透明度线性淡入/淡出，避免硬切的切面。 */
    private float endFade(float t) {
        return Mth.clamp(Math.min(t / 0.08F, (1.0F - t) / 0.08F), 0.0F, 1.0F);
    }
}
