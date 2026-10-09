package com.mimiworld.mimiworld.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * 盗米者（Rice Stealer，注册 id = rice_stealer）——狼人意象的陆地敌对生物。
 *
 * <p>GeckoLib 资源（Blockbench 导出）：geo/daomizhe.geo.json（geometry.daomizhe）、
 * textures/entity/daomizhe.png（米白绒毛+稻叶绿背脊+稻穗金鬃毛尾尖的大米主题）、
 * animations/daomizhe.animation.json（animation.daomizhe.idle / .walk / .attack）。
 * 碰撞箱 0.8×2.1。</p>
 *
 * <p>核心机制——偷米扑击（{@link StealFoodGoal}）：每只盗米者每 60 秒（1200 tick）最多发动一次。
 * 看到敌对范围内、快捷栏带食物的生存模式玩家后，先小跑逼近，约 3.5 格处腾空飞扑
 * （同时播放 1.2 秒 attack 动画并给实体一个前冲量，命中帧为动画 0.45s≈第 9 tick），
 * 命中时结算一次近战 6 点伤害（走 ATTACK_DAMAGE 属性）并从玩家快捷栏 0~8 格随机取走一整格食物
 * （物品被它带走、直接消失），随后立即放弃目标高速逃窜 5 秒并进入冷却。
 * 冷却期内不再主动攻击，只在野外游荡；被玩家攻击时惊慌逃跑（贼性，不缠斗）。</p>
 */
public class DaomizheEntity extends Monster implements GeoEntity {

    /** 同步数据：attack 动画剩余 tick（服务端启动扑击时置 24 并逐 tick 递减，自动同步到客户端）。 */
    private static final EntityDataAccessor<Integer> DATA_ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(DaomizheEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_IDLE =
            RawAnimation.begin().thenLoop("animation.daomizhe.idle");
    private static final RawAnimation ANIM_WALK =
            RawAnimation.begin().thenLoop("animation.daomizhe.walk");
    private static final RawAnimation ANIM_ATTACK =
            RawAnimation.begin().thenPlay("animation.daomizhe.attack");

    /** attack 动画时长 1.2 秒 = 24 tick */
    private static final int ATTACK_ANIMATION_TICKS = 24;
    /** 动画命中帧 0.45 秒 = 9 tick：此刻结算近战伤害与偷取 */
    private static final int HIT_FRAME_TICK = 9;
    /** 扑击最多等待 16 tick，玩家提前逃开则扑空，但照样逃走并进入冷却 */
    private static final int LUNGE_TIMEOUT_TICK = 16;
    /** 偷米扑击冷却：60 秒 = 1200 tick */
    private static final int STEAL_COOLDOWN_TICKS = 1200;
    /** 得手/扑空后的逃窜时长：5 秒 = 100 tick */
    private static final int FLEE_TICKS = 100;
    /** 进入飞扑的距离平方（3.5 格） */
    private static final double START_LUNGE_DIST_SQR = 3.5D * 3.5D;
    /** 命中帧允许结算的最大距离平方（3.2 格，飞扑有距离补偿） */
    private static final double HIT_DIST_SQR = 3.2D * 3.2D;
    /** 索敌/发动机制的最大范围（20 格） */
    private static final double SENSE_DIST_SQR = 20.0D * 20.0D;

    /** 偷米扑击冷却剩余 tick（仅服务端逻辑，存入 NBT） */
    private int stealCooldown;
    /** 剩余逃窜 tick */
    private int fleeTicks;
    /** 逃窜时远离的参照点（得手瞬间玩家的位置） */
    private Vec3 fleeFrom = Vec3.ZERO;
    /** 逃窜寻路节流 */
    private int fleeRepathCooldown;

    public DaomizheEntity(EntityType<? extends DaomizheEntity> type, Level level) {
        super(type, level);
    }

    /** 属性：20 心、移速 0.3（对齐原版狼）、近战 6 点伤害、索敌 20 格、少量击退。 */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // 偷米扑击连携：逼近→飞扑（6 伤+偷快捷栏食物）→逃走，60s 一次
        this.goalSelector.addGoal(1, new StealFoodGoal());
        // 挨打就慌，不缠斗
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // 只索敌玩家；mustSee=true，看到在范围内的玩家才锁定
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 过渡时间 5 tick（与项目内其他 GeckoLib 实体一致）
        controllers.add(new AnimationController<>(this, "main", 5, this::handleAnimState));
    }

    /**
     * 动画优先级：扑击（24 tick 单次）＞ 行走 walk ＞ 待机 idle。
     * setAndContinue 保证同名动画不被逐帧重启，attack 单次播完自然切回。
     */
    private PlayState handleAnimState(AnimationState<DaomizheEntity> state) {
        if (this.entityData.get(DATA_ATTACK_ANIM_TICKS) > 0) {
            state.setAndContinue(ANIM_ATTACK);
            return PlayState.CONTINUE;
        }
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

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            int ticks = this.entityData.get(DATA_ATTACK_ANIM_TICKS);
            if (ticks > 0) {
                this.entityData.set(DATA_ATTACK_ANIM_TICKS, ticks - 1);
            }
            if (this.stealCooldown > 0) {
                this.stealCooldown--;
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACK_ANIM_TICKS, 0);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("StealCooldown", this.stealCooldown);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.stealCooldown = compound.getInt("StealCooldown");
    }

    // -------------------- 音效：沿用原版狐狸（咬/叫，贴合偷米贼的习性） --------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.FOX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    /**
     * 从玩家快捷栏 0~8 格随机取走一整格可食用物品，物品随盗米者逃走而消失。
     *
     * @return 是否成功得手（无食物则返回 false）
     */
    private boolean stealFood(Player player) {
        Inventory inventory = player.getInventory();
        List<Integer> foodSlots = new ArrayList<>();
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && stack.isEdible()) {
                foodSlots.add(slot);
            }
        }
        if (foodSlots.isEmpty()) {
            return false;
        }
        int slot = foodSlots.get(this.random.nextInt(foodSlots.size()));
        inventory.removeItem(slot, inventory.getItem(slot).getCount());
        // 立刻向客户端刷新背包，避免被偷走的格子在玩家界面残留残影
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.broadcastChanges();
        }
        return true;
    }

    /**
     * 偷米扑击 Goal：小跑逼近 → 腾空飞扑（第 9 tick 命中帧结算 6 伤+偷食）→ 放弃目标高速逃窜 5 秒。
     * 得手或扑空都会让本次行为结束并由盗米者进入 60 秒冷却（冷却由 {@link DaomizheEntity#aiStep} 递减）。
     */
    private class StealFoodGoal extends Goal {

        private boolean running;
        private boolean lunging;
        private int lungeTick;

        StealFoodGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (DaomizheEntity.this.stealCooldown > 0 || DaomizheEntity.this.fleeTicks > 0) {
                return false;
            }
            LivingEntity target = DaomizheEntity.this.getTarget();
            if (!(target instanceof Player player) || player.isCreative() || player.isSpectator()) {
                return false;
            }
            if (DaomizheEntity.this.distanceToSqr(player) > SENSE_DIST_SQR) {
                return false;
            }
            // 快捷栏没有食物就不发动（扑空没有意义）
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty() && stack.isEdible()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.running;
        }

        @Override
        public void start() {
            this.running = true;
            this.lunging = false;
            this.lungeTick = 0;
        }

        @Override
        public void stop() {
            this.running = false;
            this.lunging = false;
            this.lungeTick = 0;
            DaomizheEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            // 逃窜阶段：记住了得手点，只管往反方向跑
            if (DaomizheEntity.this.fleeTicks > 0) {
                DaomizheEntity.this.fleeTicks--;
                if (DaomizheEntity.this.fleeRepathCooldown-- <= 0) {
                    DaomizheEntity.this.fleeRepathCooldown = 8;
                    Vec3 away = DaomizheEntity.this.position().subtract(DaomizheEntity.this.fleeFrom);
                    if (away.lengthSqr() < 1.0E-4D) {
                        away = Vec3.directionFromRotation(0.0F, DaomizheEntity.this.getYRot());
                    } else {
                        away = away.normalize();
                    }
                    DaomizheEntity.this.getNavigation().moveTo(
                            DaomizheEntity.this.getX() + away.x * 10.0D,
                            DaomizheEntity.this.getY(),
                            DaomizheEntity.this.getZ() + away.z * 10.0D,
                            1.45D);
                }
                if (DaomizheEntity.this.fleeTicks == 0) {
                    DaomizheEntity.this.stealCooldown = STEAL_COOLDOWN_TICKS;
                    DaomizheEntity.this.setTarget(null);
                    this.stop();
                }
                return;
            }

            LivingEntity target = DaomizheEntity.this.getTarget();
            if (!(target instanceof Player player)) {
                this.stop();
                return;
            }
            DaomizheEntity.this.getLookControl().setLookAt(player, 30.0F, 30.0F);

            if (!this.lunging) {
                // 小跑逼近
                DaomizheEntity.this.getNavigation().moveTo(player, 1.25D);
                if (DaomizheEntity.this.distanceToSqr(player) <= START_LUNGE_DIST_SQR) {
                    this.lunging = true;
                    this.lungeTick = 0;
                    // 播放 attack 动画并给一个朝向玩家的腾空前冲量，模拟飞扑
                    DaomizheEntity.this.entityData.set(DATA_ATTACK_ANIM_TICKS, ATTACK_ANIMATION_TICKS);
                    Vec3 dir = player.position().subtract(DaomizheEntity.this.position());
                    dir = new Vec3(dir.x, 0.0D, dir.z);
                    if (dir.lengthSqr() > 1.0E-4D) {
                        dir = dir.normalize();
                        DaomizheEntity.this.setDeltaMovement(
                                DaomizheEntity.this.getDeltaMovement().add(dir.x * 0.42D, 0.14D, dir.z * 0.42D));
                    }
                }
            } else {
                this.lungeTick++;
                DaomizheEntity.this.getNavigation().moveTo(player, 1.6D);
                if (this.lungeTick == HIT_FRAME_TICK) {
                    boolean connected = DaomizheEntity.this.distanceToSqr(player) <= HIT_DIST_SQR;
                    if (connected) {
                        // 近战 6 点伤害走属性结算；同时取走快捷栏一格食物
                        DaomizheEntity.this.doHurtTarget(player);
                        DaomizheEntity.this.stealFood(player);
                        DaomizheEntity.this.level().playSound(null,
                                DaomizheEntity.this.getX(), DaomizheEntity.this.getY(), DaomizheEntity.this.getZ(),
                                SoundEvents.FOX_BITE, SoundSource.HOSTILE, 1.0F, 1.0F);
                    }
                    this.beginFlee(player);
                } else if (this.lungeTick > LUNGE_TIMEOUT_TICK) {
                    // 玩家提前逃开，扑空，照样逃走（本次机制已消耗）
                    this.beginFlee(player);
                }
            }
        }

        /** 进入逃窜：记录玩家位置、放弃目标 */
        private void beginFlee(Player player) {
            DaomizheEntity.this.fleeFrom = player.position();
            DaomizheEntity.this.fleeTicks = FLEE_TICKS;
            DaomizheEntity.this.fleeRepathCooldown = 0;
            DaomizheEntity.this.setTarget(null);
            DaomizheEntity.this.getNavigation().stop();
        }
    }
}
