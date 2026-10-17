package com.mimiworld.mimiworld.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

/**
 * 在弓的「string」骨骼上渲染拉弓时搭在弦上的箭。
 *
 * <p>贴图不是写死的：它取自持有者背包里将要射出的那支箭（{@link BowArrowHelper#getNockedArrow}），
 * 所以 8 种箭拉弓时各自显示自己的手持贴图。箭只在「有人正在拉这把弓」时出现，
 * 物品栏图标、掉落物、展示框里的弓不会冒出一支箭。</p>
 *
 * <p>位置由骨骼决定：{@code string} 骨就是弦中点（拉弓动画里它会跟着弦往后退），
 * 因此箭自然随拉弓动作后移。箭是 16×16 的平面贴图（原版 item/generated 模型），
 * 贴图里的箭身约呈 45° 斜向右上，所以绕 Z 轴转 {@value #SPRITE_TILT_DEGREES}° 让它指向模型正前方（-X，
 * 也就是弓的握把在 +X 一侧、箭朝 -X 射出去的方向）。</p>
 */
public class NockedArrowLayer<T extends Item & GeoAnimatable> extends BlockAndItemGeoLayer<T> {

    /** 弦上的搭箭点骨骼名（mubangong.geo.json / fanjinggong.geo.json 里的弦中点）。 */
    private static final String NOCK_BONE = "string";
    /** 贴图里箭身斜向右上约 45°；绕 Z 轴转这个角度后指向模型正前方 -X（即箭射出去的方向）。 */
    private static final float SPRITE_TILT_DEGREES = 135.0f;
    /** 箭尾到贴图中心的 X 距离（块），用于把箭尾对齐到弦上。 */
    private static final float TAIL_OFFSET_X = -0.53f;

    private final GeoItemRenderer<T> itemRenderer;

    public NockedArrowLayer(GeoItemRenderer<T> itemRenderer) {
        super(itemRenderer);
        this.itemRenderer = itemRenderer;
    }

    @Override
    protected ItemStack getStackForBone(GeoBone bone, T animatable) {
        if (!NOCK_BONE.equals(bone.getName())) {
            return null;
        }
        ItemStack arrow = BowArrowHelper.getNockedArrow(this.itemRenderer.getCurrentItemStack());
        return arrow.isEmpty() ? null : arrow;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable,
                                      MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        // 先对齐箭尾、再摆正朝向（PoseStack 后调用的先作用到几何体上）
        poseStack.translate(TAIL_OFFSET_X, 0.0f, 0.0f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(SPRITE_TILT_DEGREES));
        super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
