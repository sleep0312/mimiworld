package com.mimiworld.mimiworld.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

/**
 * 化灵米（Hualing Rice，注册 id = hualing_rice）——大米成精的空中敌对飞灵。
 *
 * <p>GeckoLib 资源（Blockbench 导出）：
 * geo/hualingmi.geo.json（geometry.hualingmi，16 骨骼 38 方块：米白长袍小人身 + 稻穗耳/额发 +
 * 稻叶双翼与翼尖 + 幽灵尾尖，模型直立约 26px 高）、textures/entity/hualingmi.png（64×64，
 * 米胎白脸+深色怒眉+橙红发光怒目+稻叶绿羽片+胸口灵青魂晶+米汤雾蓝尾尖）、
 * animations/hualingmi.animation.json（animation.hualingmi.idle / .fly / .attack）。碰撞箱 1.0×2.0。</p>
 *
 * <p>行为近似原版恼鬼但去掉穿墙：常驻无重力悬浮飞行（{@link FlyingMoveControl} +
 * {@link FlyingPathNavigation}，寻路节点只走空气格、被实体方块阻挡，因此不能穿墙），
 * 永不落地、不做任何地面漫步；主动索敌玩家、受击反击，贴近后近战扑击；
 * 无目标时在高空随机悬停漂浮。5000 心（10000 血，沿用占位数值）、近战 10、飞行速度 1.2、追击 40；
 * 音效沿用原版恼鬼。</p>
 */
public class HualingmiEntity extends Monster implements GeoEntity {

    /** 同步数据：攻击动画剩余 tick（服务端命中时置 24 并逐 tick 递减，自动同步到客户端）。 */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(HualingmiEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.hualingmi.idle");
    private static final RawAnimation ANIM_FLY =
            RawAnimation.begin().thenLoop("animation.hualingmi.fly");
    private static final RawAnimation ANIM_ATTACK =
            RawAnimation.begin().thenPlay("animation.hualingmi.attack");

    /** attack 动画时长 1.1667 秒 ≈ 24 tick */
    private static final int ATTACK_ANIMATION_TICKS = 24;

    /** 位移速度平方阈值：高于它视为空中追击播 fly，否则悬停播 idle */
    private static final double MOVE_ANIM_SPEED_SQR = 0.0025D;

    public HualingmiEntity(EntityType<? extends HualingmiEntity> type, Level level) {
        super(type, level);
        // 常驻飞行：永不落地 + 急停悬停式飞行控制器（同恼鬼/烈焰人/米魂）
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
    }

    /** 属性：5000 心 = 10000 血、近战 10（均沿用占位数值）、移速 0.25、飞行 1.2、追击 40。 */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FLYING_SPEED, 1.2D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    /**
     * 飞行生物使用空中直线寻路（节点允许悬空点位，同恼鬼/烈焰人）。
     * 不允许穿墙/穿门：节点评估只接受空气格，实体方块直接阻断寻路与移动碰撞。
     */
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(false);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // 飞行追击近战：长须 mustSee=true，被墙挡住视线时绕墙寻路而不是穿墙硬挤
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        // 无目标时的高空悬停漂浮（不注册任何地面漫步 Goal，保证永不落地）
        this.goalSelector.addGoal(3, new HoverWanderGoal());
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 过渡时间 5 tick（与项目内其他 GeckoLib 实体一致）
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    /**
     * 动画优先级：扑击（约 24 tick 单次）＞ 空中追击 fly ＞ 悬停 idle。
     * setAndContinue 保证同名动画不被逐帧重启，attack 单次播完自然切回。
     */
    private PlayState handleAnimState(AnimationState<HualingmiEntity> state) {
        if (this.entityData.get(DATA_ATTACK_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_ATTACK);
            return PlayState.CONTINUE;
        }
        if (this.getDeltaMovement().lengthSqr() > MOVE_ANIM_SPEED_SQR) {
            state.setAndContinue(ANIM_FLY);
        } else {
            state.setAndContinue(ANIM_IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** 命中目标时触发一次攻击动画（doHurtTarget 在服务端结算伤害成功时调用一次，数据自动同步） */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
            return true;
        }
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // 仅服务端倒计时并驱动同步，客户端每 tick 收到 24→0 的新值
        if (!this.level().isClientSide) {
            int ticks = this.entityData.get(DATA_ATTACK_ANIM_TICKS);
            if (ticks > 0) {
                this.entityData.set(DATA_ATTACK_ANIM_TICKS, ticks - 1);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
    }

    // -------------------- 杂项：摔落免疫/音效 --------------------

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        // 飞行生物，永不承受摔落伤害
        return false;
    }

    // -------------------- 音效：沿用原版恼鬼 --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    /**
     * 无目标时的空中漂浮：随机选取身体周围 ±12 水平、±8 高度（整体偏上）的空中点飘过去，
     * 移动控制器空闲时才有几率改点，避免原地乱转。全程悬空，永不落地。
     */
    private class HoverWanderGoal extends Goal {
        HoverWanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return HualingmiEntity.this.getTarget() == null
                    && !HualingmiEntity.this.getMoveControl().hasWanted()
                    && HualingmiEntity.this.random.nextInt(40) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void tick() {
            if (HualingmiEntity.this.random.nextInt(20) == 0) {
                HualingmiEntity rice = HualingmiEntity.this;
                double tx = rice.getX() + (rice.random.nextDouble() * 2.0D - 1.0D) * 12.0D;
                double ty = rice.getY() + (rice.random.nextDouble() * 2.0D - 1.0D) * 8.0D + 2.0D;
                double tz = rice.getZ() + (rice.random.nextDouble() * 2.0D - 1.0D) * 12.0D;
                ty = Math.max(ty, rice.level().getMinBuildHeight() + 2.0D);
                rice.getMoveControl().setWantedPosition(tx, ty, tz, 0.85D);
            }
        }
    }
}
