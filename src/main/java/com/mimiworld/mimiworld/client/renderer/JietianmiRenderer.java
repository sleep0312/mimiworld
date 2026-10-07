package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.JietianmiModel;
import com.mimiworld.mimiworld.entity.JietianmiEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 界天米渲染器：GeckoLib 独眼飞行模型整体放大 1.2 倍，阴影半径 0.7。
 *
 * <p>额外渲染守卫者式蓄力光束：蓄力期间从独眼到同步目标之间绘制两束交叉四边形，
 * 复用原版 guardian_beam.png（cutoutNoCull），粗细/透明度随蓄力进度增长并沿轴向滚动 UV，
 * 对齐 attack 动画的凝视→发射节奏。</p>
 */
public class JietianmiRenderer extends GeoEntityRenderer<JietianmiEntity> {

    /** 复用原版守卫者光束贴图（条状渐变光束，UV 重复包裹） */
    private static final ResourceLocation BEAM_TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/guardian_beam.png");
    private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(BEAM_TEXTURE);

    /** 独眼在模型局部坐标中的高度：瞳孔约 y20.5px，放大 1.2 倍 ≈ 1.54 格 */
    private static final float EYE_LOCAL_Y = 1.52F;

    /** 光束纵向分段数 */
    private static final int BEAM_SEGMENTS = 16;

    /** 满亮度（守卫者光束原做法） */
    private static final int FULL_BRIGHT = 0xF000F0;

    public JietianmiRenderer(EntityRendererProvider.Context context) {
        super(context, new JietianmiModel());
        this.withScale(1.2F);
        this.shadowRadius = 0.7F;
    }

    @Override
    public void render(JietianmiEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        this.renderBeam(entity, partialTicks, poseStack, bufferSource);
    }

    /**
     * 绘制蓄力光束。render() 入口处 PoseStack 已平移到实体插值位置（无旋转），
     * 故起点用局部坐标 (0, EYE_LOCAL_Y, 0)，终点取“目标插值位置 − 实体插值位置”。
     */
    private void renderBeam(JietianmiEntity entity, float partialTicks,
                            PoseStack poseStack, MultiBufferSource bufferSource) {
        int ticks = entity.getBeamTicks();
        if (ticks <= 0) {
            return;
        }
        Entity target = entity.level().getEntity(entity.getBeamTargetId());
        if (target == null) {
            return;
        }

        double sx = Mth.lerp(partialTicks, entity.xOld, entity.getX());
        double sy = Mth.lerp(partialTicks, entity.yOld, entity.getY()) + EYE_LOCAL_Y;
        double sz = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
        double tx = Mth.lerp(partialTicks, target.xOld, target.getX());
        double ty = Mth.lerp(partialTicks, target.yOld, target.getY()) + target.getBbHeight() * 0.5D;
        double tz = Mth.lerp(partialTicks, target.zOld, target.getZ());

        Vector3f axis = new Vector3f((float) (tx - sx), (float) (ty - sy), (float) (tz - sz));
        float length = axis.length();
        if (length < 0.01F) {
            return;
        }
        axis.normalize();

        // 与光束轴垂直的两个正交方向（构成交叉四边形）
        Vector3f side = new Vector3f();
        side.cross(axis, new Vector3f(0.0F, 1.0F, 0.0F));
        if (side.lengthSquared() < 1.0E-6F) {
            side.set(1.0F, 0.0F, 0.0F);
        }
        side.normalize();
        Vector3f up = new Vector3f();
        up.cross(side, axis).normalize();

        float progress = entity.getBeamProgress(partialTicks);
        // 前 10% 时间淡入，避免光束凭空出现
        float alpha = Mth.clamp(progress * 10.0F, 0.0F, 1.0F);
        // 基础粗细随蓄力增长
        float baseWidth = 0.04F + 0.08F * progress;
        float time = entity.level().getGameTime() + partialTicks;
        float scroll = time * 0.02F;
        float vLength = length * 0.5F;

        poseStack.pushPose();
        poseStack.translate(0.0F, EYE_LOCAL_Y, 0.0F);
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        VertexConsumer consumer = bufferSource.getBuffer(BEAM_RENDER_TYPE);

        for (int i = 0; i < BEAM_SEGMENTS; i++) {
            float s0 = (float) i / BEAM_SEGMENTS;
            float s1 = (float) (i + 1) / BEAM_SEGMENTS;
            float r0 = ringRadius(baseWidth, s0, time);
            float r1 = ringRadius(baseWidth, s1, time);
            float x0 = axis.x * length * s0;
            float y0 = axis.y * length * s0;
            float z0 = axis.z * length * s0;
            float x1 = axis.x * length * s1;
            float y1 = axis.y * length * s1;
            float z1 = axis.z * length * s1;
            float v0 = scroll + vLength * s0;
            float v1 = scroll + vLength * s1;
            // 交叉四边形 A（沿 side 展宽）与 B（沿 up 展宽）
            this.beamQuad(consumer, pose, normal,
                    x0, y0, z0, x1, y1, z1, r0, r1, v0, v1, alpha, side);
            this.beamQuad(consumer, pose, normal,
                    x0, y0, z0, x1, y1, z1, r0, r1, v0, v1, alpha, up);
        }

        poseStack.popPose();
    }

    /** 光束越靠近目标越粗，并沿轴向轻微脉动。 */
    private float ringRadius(float baseWidth, float along, float time) {
        float taper = 0.55F + 0.45F * along;
        float pulse = 1.0F + 0.12F * Mth.sin(time * 0.5F + along * 8.0F);
        return baseWidth * taper * pulse;
    }

    /** 沿轴线某一段、按 dir 方向展宽的一个四边形（cull 关闭，顶点顺序不影响双面可见）。 */
    private void beamQuad(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
                          float x0, float y0, float z0,
                          float x1, float y1, float z1,
                          float r0, float r1, float v0, float v1, float alpha,
                          Vector3f dir) {
        float r = 1.0F;
        float g = 0.92F;
        float b = 0.72F;
        // v0 = 起点 +dir，v1 = 起点 -dir，v2 = 终点 +dir，v3 = 终点 -dir
        this.vertex(consumer, pose, normal, x0 + dir.x * r0, y0 + dir.y * r0, z0 + dir.z * r0,
                r, g, b, alpha, 0.0F, v0, dir);
        this.vertex(consumer, pose, normal, x1 + dir.x * r1, y1 + dir.y * r1, z1 + dir.z * r1,
                r, g, b, alpha, 0.0F, v1, dir);
        this.vertex(consumer, pose, normal, x1 - dir.x * r1, y1 - dir.y * r1, z1 - dir.z * r1,
                r, g, b, alpha, 1.0F, v1, dir);
        this.vertex(consumer, pose, normal, x0 - dir.x * r0, y0 - dir.y * r0, z0 - dir.z * r0,
                r, g, b, alpha, 1.0F, v0, dir);
    }

    private void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
                        float x, float y, float z,
                        float r, float g, float b, float a, float u, float v, Vector3f normalDir) {
        consumer.vertex(pose, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(FULL_BRIGHT)
                .normal(normal, normalDir.x, normalDir.y, normalDir.z)
                .endVertex();
    }
}
