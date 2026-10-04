package com.mimiworld.mimiworld.client.renderer;

import com.mimiworld.mimiworld.client.model.PamiCrawlerModel;
import com.mimiworld.mimiworld.entity.PamiCrawlerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

/**
 * 爪巴米者（Pami Crawler）实体渲染器：把 {@link PamiCrawlerModel} 绑定到实体。
 * 碰撞箱宽 0.7 但模型四脚外八撑开，阴影半径取 0.45 贴地。
 *
 * <p>引信燃烧期间（swell&gt;0）改用 {@link RenderType#energySwirl} 渲染——
 * 与原版苦力怕爆炸前的白色嘶嘶闪白同源（同一渲染层类型），鼓胀动画本身在 Blockbench
 * 的 animation.attack 里用 body 骨 scale 做出，这里只负责白色裂纹能量层。</p>
 *
 * 在 ClientSetup 的 EntityRenderersEvent.RegisterRenderers 中注册给 PAMI_CRAWLER。
 */
public class PamiCrawlerRenderer extends GeoEntityRenderer<PamiCrawlerEntity> {

    public PamiCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context, new PamiCrawlerModel());
        this.shadowRadius = 0.45F;
    }

    @Override
    public RenderType getRenderType(PamiCrawlerEntity entity, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        float swelling = entity.getSwelling(partialTick);
        if (swelling > 0.0F) {
            // 与原版 CreeperRenderer 同款：UV 做时间相关的抖动，引信越足闪白越强
            float time = (float) entity.tickCount + partialTick;
            float u = Mth.sin(time * 10.0F) * 0.01F * swelling;
            float v = time * 0.01F % 1.0F;
            return RenderType.energySwirl(texture, u, v);
        }
        return super.getRenderType(entity, texture, bufferSource, partialTick);
    }
}
