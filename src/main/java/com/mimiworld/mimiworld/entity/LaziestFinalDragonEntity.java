package com.mimiworld.mimiworld.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
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
 * 157 最懒终末龙（Laziest Final Dragon，注册 id = laziest_final_dragon）。
 *
 * <p>GeckoLib 模型/贴图/动画：geo/laziest_final_dragon.geo.json（超巨型飞龙骨架）、
 * textures/entity/laziest_final_dragon.png（256×256 暗紫鳞片）、
 * animations/laziest_final_dragon.animation.json。动画文件当前只有两个循环动画：
 * {@code animation.idle}（待机，悬停/慢盘旋时播）与 {@code animation.flying}
 * （飞行/追击/俯冲时播），<b>没有攻击动画</b>，因此俯冲撞击时仍播 flying。</p>
 *
 * <p>行为向原版末影龙看齐但不依赖末地（原版 EnderDragon 的阶段 AI 与末地水晶/返回传送门
 * 强绑定，主世界召唤会绕固定坐标 (0,?) 盘旋）：</p>
 * <ul>
 *   <li>常驻无重力飞行（{@link FlyingMoveControl} + {@link FlyingPathNavigation}，同 Ghast 方案）、火焰免疫；</li>
 *   <li>全天敌对玩家：受击反击 + 主动索敌；</li>
 *   <li>战斗时在目标头顶约 10 格绕圈盘旋（CIRCLE），周期性高速俯冲撞目标（SWOOP，近战伤害+击退），
 *       命中或冲过头后向上拉起恢复（RECOVER），然后重新盘旋——对应末影龙的盘旋/俯冲节奏；</li>
 *   <li>无目标时在出生区域高空漫游；</li>
 *   <li>紫色 Boss 血条、末影龙音效（吼叫/受伤/死亡）、免疫击退（撞击玩家时自己不被顶退）。</li>
 * </ul>
 *
 * <p>属性沿用该实体原 PendingBoss 占位数值（100 血 / 8 攻击 / 4 甲 / 32 追击），
 * 另加飞行必需的 FLYING_SPEED 与满击退抗性。</p>
 */
public class LaziestFinalDragonEntity extends PathfinderMob implements GeoEntity, net.minecraft.world.entity.monster.Enemy {

    // GeckoLib：每个实体实例持有一个动画状态缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 待机：laziest_final_dragon.animation.json 中的 "animation.idle"（loop=true，4 秒）
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.idle");
    // 飞行："animation.flying"（loop=true）
    private static final RawAnimation ANIM_FLYING = RawAnimation.begin().thenLoop("animation.flying");

    /** 水平速度平方阈值：高于它视为飞行中（俯冲/追击/转移），低于则悬停播待机 */
    private static final double FLYING_ANIM_SPEED_SQR = 2.5E-3D;

    /** 紫色 Boss 血条（服务端维护，自动同步给看到它的玩家） */
    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    public LaziestFinalDragonEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        // 常驻飞行：无重力 + 飞行移动控制器（转向灵活度 10，同 Ghast；不做悬停时的急停）
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        // 飞龙可穿过水以外的非固体方块，不应被危险方块（仙人掌/浆果/岩浆块等）卡住或惩罚
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, -1.0F);
    }

    /**
     * 属性：沿用原占位 Boss 数值（100 血/0.3 移速/8 攻击/32 追击/4 甲），
     * FLYING_SPEED 是 FlyingMoveControl 的实际加速系数（1.5=俯冲迅猛），
     * KNOCKBACK_RESISTANCE 满值=末影龙式不受击退。
     */
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FLYING_SPEED, 1.5D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    /** 飞行生物使用空中直线寻路（节点评估器允许悬空点位） */
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        // 优先级 2：有目标时全程盘旋/俯冲；优先级 4：无目标高空漫游
        this.goalSelector.addGoal(2, new DragonSwoopAttackGoal());
        this.goalSelector.addGoal(4, new DragonAirWanderGoal());
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // 全天敌对：受击必反击 + 主动索敌玩家
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------- GeckoLib 动画 --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 过渡时间 8 tick：盘旋与扑翼之间平滑混合
        controllers.add(new AnimationController<>(this, "main", 8, this::handleAnimState));
    }

    /**
     * 动画状态：有水平速度（盘旋位移/追击/俯冲）→ flying；
     * 几乎悬停（无目标待机或空中定点）→ idle。没有攻击动画可切。
     */
    private PlayState handleAnimState(AnimationState<LaziestFinalDragonEntity> state) {
        if (this.getDeltaMovement().horizontalDistanceSqr() > FLYING_ANIM_SPEED_SQR) {
            state.setAndContinue(ANIM_FLYING);
        } else {
            state.setAndContinue(ANIM_IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // -------------------- Boss 血条 --------------------

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        // 每服务端 tick 同步血条进度（末影龙同款 BossBar 行为）
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        super.customServerAiStep();
    }

    // -------------------- 音效：末影龙 --------------------

    /** 末影龙吼叫不适合太频繁，拉长环境音间隔（默认 80 tick） */
    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_GROWL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        // 超巨型 Boss，龙吼全场可闻
        return 2.5F;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        // 飞行生物，永不承受摔落伤害
        return false;
    }

    @Override
    public boolean ignoreExplosion() {
        // 末影龙级别的大体型 Boss，爆炸不产生击退（与满击退抗性双保险）
        return true;
    }

    // -------------------- 自定义飞行 AI --------------------

    /**
     * 末影龙式「盘旋 → 俯冲 → 拉起」攻击循环（单一 Goal 持有三阶段状态机）。
     *
     * <p>CIRCLE：在目标头顶约 {@value #CIRCLE_RADIUS} 格绕圈、高度约 {@value #CIRCLE_HEIGHT} 格，
     * 持续盯住目标；冷却结束且距离合适时进入 SWOOP。<br>
     * SWOOP：用最高速移动控制点直扑目标当前位置，接触（中心距离≤{@value #HIT_RADIUS} 格）
     * 且无遮挡时结算一次近战伤害+原版击退，随后进入 RECOVER；超时或冲到目标下方也强制拉起。<br>
     * RECOVER：朝当前盘旋角方向斜上拉起，结束后回到 CIRCLE。</p>
     */
    private class DragonSwoopAttackGoal extends Goal {
        private static final int CIRCLE = 0;
        private static final int SWOOP = 1;
        private static final int RECOVER = 2;

        private static final double CIRCLE_RADIUS = 10.0D;
        private static final double CIRCLE_HEIGHT = 9.0D;
        private static final double HIT_RADIUS = 3.2D;
        private static final int ATTACK_COOLDOWN_MIN = 140;
        private static final int ATTACK_COOLDOWN_ADD = 100;
        private static final int SWOOP_TIMEOUT = 70;
        private static final int RECOVER_TIME = 55;

        private int phase = CIRCLE;
        private int phaseTime;
        private int attackCooldown = 80;
        private double circleAngle;

        DragonSwoopAttackGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return LaziestFinalDragonEntity.this.getTarget() != null;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = LaziestFinalDragonEntity.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void start() {
            this.phase = CIRCLE;
            this.phaseTime = 0;
            this.circleAngle = LaziestFinalDragonEntity.this.random.nextDouble() * Math.PI * 2.0D;
        }

        @Override
        public void stop() {
            this.phase = CIRCLE;
            this.phaseTime = 0;
            LaziestFinalDragonEntity.this.navigation.stop();
        }

        @Override
        public void tick() {
            LaziestFinalDragonEntity dragon = LaziestFinalDragonEntity.this;
            LivingEntity target = dragon.getTarget();
            if (target == null) {
                return;
            }
            this.phaseTime++;
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }
            // 注视目标由 FlyingMoveControl 的自动朝向完成（朝当前移动点转头），
            // 盘旋点绕目标转、俯冲点即目标本身，视角自然跟踪无需显式 LookControl

            switch (this.phase) {
                case SWOOP -> this.tickSwoop(dragon, target);
                case RECOVER -> this.tickRecover(dragon);
                default -> this.tickCircle(dragon, target);
            }
        }

        /** 盘旋：绕目标头顶缓慢画圈，冷却结束即俯冲 */
        private void tickCircle(LaziestFinalDragonEntity dragon, LivingEntity target) {
            // 每 tick 推进一点角度（约 23 秒绕一圈，配合扑翼动画像在观察猎物）
            this.circleAngle += 0.027D;
            double cx = target.getX() + Math.cos(this.circleAngle) * CIRCLE_RADIUS;
            double cz = target.getZ() + Math.sin(this.circleAngle) * CIRCLE_RADIUS;
            double cy = Math.max(target.getY() + CIRCLE_HEIGHT, dragon.level().getMinBuildHeight() + 4);
            dragon.getMoveControl().setWantedPosition(cx, cy, cz, 1.05D);

            double distSqr = dragon.distanceToSqr(target);
            if (this.attackCooldown <= 0 && distSqr < 30.0D * 30.0D && dragon.hasLineOfSight(target)) {
                this.phase = SWOOP;
                this.phaseTime = 0;
                dragon.navigation.stop();
            }
        }

        /** 俯冲：全速直取目标，接触结算近战 */
        private void tickSwoop(LaziestFinalDragonEntity dragon, LivingEntity target) {
            // 速度系数 2.6（再乘 FLYING_SPEED 1.5），末影龙冲撞的迅猛感
            dragon.getMoveControl().setWantedPosition(target.getX(), target.getY(0.5D), target.getZ(), 2.6D);

            boolean inRange = dragon.distanceToSqr(target) <= HIT_RADIUS * HIT_RADIUS;
            if (inRange && dragon.hasLineOfSight(target) && dragon.doHurtTarget(target)) {
                // 命中：长冷却后再来一轮
                this.attackCooldown = ATTACK_COOLDOWN_MIN + dragon.random.nextInt(ATTACK_COOLDOWN_ADD);
                this.phase = RECOVER;
                this.phaseTime = 0;
                return;
            }
            // 冲过头（落到目标以下）或超时：即使没撞到也拉起，避免钻地/贴地乱撞
            boolean overshot = dragon.getY() < target.getY() - 0.5D
                    && dragon.getDeltaMovement().horizontalDistanceSqr() < 0.04D;
            if (this.phaseTime >= SWOOP_TIMEOUT || overshot) {
                this.attackCooldown = Math.max(this.attackCooldown, 50);
                this.phase = RECOVER;
                this.phaseTime = 0;
            }
        }

        /** 拉起：沿盘旋角外沿斜上脱离，重新获得高度 */
        private void tickRecover(LaziestFinalDragonEntity dragon) {
            double tx = dragon.getX() + Math.cos(this.circleAngle) * 7.0D;
            double tz = dragon.getZ() + Math.sin(this.circleAngle) * 7.0D;
            double ty = dragon.getY() + 7.0D;
            dragon.getMoveControl().setWantedPosition(tx, ty, tz, 1.2D);
            if (this.phaseTime >= RECOVER_TIME) {
                this.phase = CIRCLE;
                this.phaseTime = 0;
            }
        }
    }

    /**
     * 无目标时的高空漫游：在当前位置附近（水平 ±18、垂直 -4~+8）选一个悬空点飞过去，
     * 最低高度限制保证不会贴着地形找路。参考 Ghast 的随机漂浮但活动范围更大。
     */
    private class DragonAirWanderGoal extends Goal {
        private int timeout;

        DragonAirWanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LaziestFinalDragonEntity dragon = LaziestFinalDragonEntity.this;
            if (dragon.getTarget() != null || !dragon.navigation.isDone()) {
                return false;
            }
            return dragon.random.nextInt(35) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.timeout > 0 && !LaziestFinalDragonEntity.this.navigation.isDone();
        }

        @Override
        public void start() {
            LaziestFinalDragonEntity dragon = LaziestFinalDragonEntity.this;
            Level level = dragon.level();
            double tx = dragon.getX() + (dragon.random.nextDouble() * 2.0D - 1.0D) * 18.0D;
            double tz = dragon.getZ() + (dragon.random.nextDouble() * 2.0D - 1.0D) * 18.0D;
            double ty = dragon.getY() + (dragon.random.nextDouble() * 12.0D - 4.0D);
            ty = Mth.clamp(ty, level.getMinBuildHeight() + 5, level.getMaxBuildHeight() - 6);
            dragon.navigation.moveTo(tx, ty, tz, 0.85D);
            this.timeout = 120;
        }

        @Override
        public void tick() {
            this.timeout--;
        }

        @Override
        public void stop() {
            this.timeout = 0;
        }
    }
}
