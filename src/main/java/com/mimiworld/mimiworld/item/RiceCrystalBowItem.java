package com.mimiworld.mimiworld.item;

import com.mimiworld.mimiworld.client.model.RiceCrystalBowModel;
import com.mimiworld.mimiworld.client.renderer.BowArrowHelper;
import com.mimiworld.mimiworld.client.renderer.NockedArrowLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

/**
 * 饭晶弓：GeckoLib 3D 模型的弓。
 *
 * <p>继承 {@link MimiBowItem}，因此保留「伤害倍率 ×1.2」的注入逻辑；
 * 额外实现 {@link GeoItem}，用 IClientItemExtensions#getCustomRenderer 挂载
 * {@link GeoItemRenderer}，各视角变换读取 models/item/rice_crystal_bow.json 的 display 节点。</p>
 *
 * <p>动画（资源在 animations/fanjinggong.animation.json，内部名为 animation.fanjinggong.*）：
 * 平时循环 idle；正在拉这把弓时播放 draw（hold_on_last_frame，拉满保持）；松弓后短暂播放一次 shoot。
 * 判断依据是「正在渲染的这个 ItemStack 的持有者是否正在使用它」——持有者的使用状态由原版
 * 实体数据同步，因此别的玩家看你的弓同样能看到拉弓动作。</p>
 */
public class RiceCrystalBowItem extends MimiBowItem implements GeoItem {

    /** 静止：轻微形变循环。 */
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.fanjinggong.idle");
    /** 拉弓：文件中 loop 为 hold_on_last_frame，播完保持末帧。 */
    private static final RawAnimation DRAW = RawAnimation.begin().thenPlay("animation.fanjinggong.draw");
    /** 射击：单次 0.3 秒。 */
    private static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("animation.fanjinggong.shoot");

    /** 射击动画持续 tick 数（0.3 秒 = 6 tick）。 */
    private static final long SHOOT_ANIM_TICKS = 6L;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** 本地玩家最近一次松弓时的客户端游戏时间，用于播放一次射击动画。 */
    private long lastLocalReleaseTick = -1000L;

    public RiceCrystalBowItem(double damageMultiplier, Properties properties) {
        super(damageMultiplier, properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide) {
            this.lastLocalReleaseTick = level.getGameTime();
        }
        super.releaseUsing(stack, level, entity, timeLeft);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<RiceCrystalBowItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    GeoItemRenderer<RiceCrystalBowItem> renderer = new GeoItemRenderer<RiceCrystalBowItem>(new RiceCrystalBowModel()) {
                        /**
                         * 改用「当前渲染的这个 ItemStack 对象」作为实例 id。
                         * GeckoLib 默认取 stack 的 GeckoLibID，而没走服务端派发 id 的普通物品
                         * 一律是 Long.MAX_VALUE，会让同类物品的所有 stack 共用同一份动画状态——
                         * 那样玩家 A 拉弓会把玩家 B（以及物品栏图标）上同一类弓的动作一起带成拉弓。
                         * 按 stack 分开后，每个 stack 各自持有独立的动画控制器。
                         */
                        @Override
                        public long getInstanceId(RiceCrystalBowItem animatable) {
                            return System.identityHashCode(this.getCurrentItemStack());
                        }
                    };
                    // 拉弓时在 string 骨骼上渲染搭在弦上的箭（贴图取自持有者将射出的那支箭）
                    renderer.addRenderLayer(new NockedArrowLayer<>(renderer));
                    this.renderer = renderer;
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // registerControllers 对每个 AnimatableManager（也就是每个 stack）都会调用一次，
        // 所以这里新建的 drawState 天然是「每个 stack 一份」的拉弓跟踪状态。
        DrawState drawState = new DrawState();
        controllers.add(new AnimationController<>(this, "bow", 2, state -> {
            state.setAndContinue(this.getCurrentAnimation(state, drawState));
            return PlayState.CONTINUE;
        }));
    }

    /**
     * 依据「当前渲染的 stack 的持有者状态」选择 idle / draw / shoot。
     * 仅在客户端由 {@link GeoItemRenderer} 渲染时调用。
     */
    private RawAnimation getCurrentAnimation(AnimationState<RiceCrystalBowItem> state, DrawState drawState) {
        ClientLevel level = Minecraft.getInstance().level;
        long now = level == null ? 0L : level.getGameTime();

        if (BowArrowHelper.findHolder(state.getData(DataTickets.ITEMSTACK)) != null) {
            drawState.drawing = true;
            drawState.lastSeenTick = now;
            return DRAW;
        }
        // 这一帧起没人拉弓了；若上一帧还在拉、中间也没有被跳过渲染，说明刚松手
        if (drawState.drawing && now - drawState.lastSeenTick <= 2L) {
            drawState.drawing = false;
            drawState.releaseTick = now;
        }
        if (now - drawState.releaseTick < SHOOT_ANIM_TICKS
                || now - this.lastLocalReleaseTick < SHOOT_ANIM_TICKS) {
            return SHOOT;
        }
        return IDLE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** 每个 stack 一份的拉弓/松弓跟踪状态。 */
    private static final class DrawState {
        /** 上一帧是否处于拉弓状态。 */
        private boolean drawing;
        /** 最近一次被渲染时的游戏时间，用于忽略长时间未渲染造成的假松弓。 */
        private long lastSeenTick;
        /** 最近一次松弓时的游戏时间，用于播放一次射击动画。 */
        private long releaseTick = -1000L;
    }
}
