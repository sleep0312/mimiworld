package com.mimiworld.mimiworld.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 微型鸭（MicroDuck）——盗米者生物蛋（物品编码 257）召唤的真实实体。
 *
 * <p>基于 GeckoLib 的动画实体：模型/贴图/动画均来自资源文件
 * （geo/microduck.geo.json、animations/microduck.animation.json、textures/entity/microduck.png），
 * 动画文件中包含两个循环动画：待机 {@code idle} 与行走 {@code walk}。</p>
 *
 * <p>当前是中立无害的游荡型小生物（PathfinderMob），后续要做盗米者专属 AI
 * （偷米/逃跑/仇恨等）时直接在本类 registerGoals 里追加目标即可。</p>
 */
public class MicroDuckEntity extends PathfinderMob implements GeoEntity {

    // GeckoLib 要求每个可动画对象持有一个动画实例缓存（按实体实例保存动画进度/状态）
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 待机动画：对应 microduck.animation.json 里的 "idle"（loop=true）
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
    // 行走动画：对应 microduck.animation.json 里的 "walk"（loop=true）
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("walk");

    public MicroDuckEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    /**
     * 实体属性：盗米者（用户实体编号 19）750 心 = 1500 点血、0.25 基础移速。
     * 在 ModEntityAttributes 的 EntityAttributeCreationEvent 中注册。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    /**
     * 基础 AI：水上漂浮 + 陆地随机游荡（避水）+ 看向玩家 + 四处张望。
     * 有移动时 GeckoLib 控制器会自动切到 walk，静止时播 idle。
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    /**
     * 注册 GeckoLib 动画控制器。
     * 控制器名 "main"，过渡时间 5 tick：每帧根据实体是否在移动选择 walk/idle。
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    /**
     * 动画状态判断：在地面移动 → walk，其余情况（站立/游泳/滑翔等）→ idle。
     */
    private PlayState handleAnimState(AnimationState<MicroDuckEntity> state) {
        if (state.isMoving()) {
            state.setAndContinue(ANIM_WALK);
        } else {
            state.setAndContinue(ANIM_IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
